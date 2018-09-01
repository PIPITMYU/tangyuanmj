package com.yzt.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ResourceLeakDetector;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.yzt.netty.handler.WSHandlerFactory;
import com.yzt.netty.util.Cnst;

/**
 * 
 * @Title:  NettyServer     
 * @Description:  netty ws server 
 * @author: zc    
 * @date:   2018年1月29日 下午2:12:03   
 * @version V1.0 
 * @Copyright: 2018 云智通 All rights reserved.
 */
public class NettyServer extends Cnst{

	private static Log log = LogFactory.getLog(ChannelInboundHandlerAdapter.class);

    private WSHandlerFactory wsHandlerFactory;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public NettyServer() {
        super();
        // TODO Auto-generated constructor stub
    }


    public void startNettyServer() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                EventLoopGroup boss = new NioEventLoopGroup();
                EventLoopGroup worker = new NioEventLoopGroup();
                try {
                    ServerBootstrap bootstrap = new ServerBootstrap();
                    bootstrap.group(boss, worker);
                    bootstrap.channel(NioServerSocketChannel.class);
                    bootstrap.option(ChannelOption.SO_BACKLOG, 2000); //连接数
                    bootstrap.option(ChannelOption.TCP_NODELAY, true);  //不延迟，消息立即发送
//		            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000);  //超时时间
                    bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true); //长连接
                    bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel)
                                throws Exception {
                            ChannelPipeline p = socketChannel.pipeline();

                            p.addLast("http-codec", new HttpServerCodec());
                            p.addLast("aggregator", new HttpObjectAggregator(65536));
                            p.addLast("http-chunked", new ChunkedWriteHandler());
                            p.addLast(new IdleStateHandler(11, 5, 13, TimeUnit.SECONDS));
                            //请求处理
                            p.addLast("inboundHandler", wsHandlerFactory.newWSHandler());
                            //关闭处理
                            p.addLast("outboundHandler", wsHandlerFactory.newWSOutHandler());
                        }
                    });
                    ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);//可以打印内存泄漏的错误信息
                    ChannelFuture f = bootstrap.bind(Cnst.NETTY_PORT).sync();
                    if (f.isSuccess()) {
                    	log.info("webSocket server startup with port " + Cnst.NETTY_PORT);
                    }
                    f.channel().closeFuture().sync();
                } catch (Exception e) {
                	log.error("netty start error", e );
                } finally {
                    boss.shutdownGracefully();
		            worker.shutdownGracefully();
                }
            }
        });


    }

	public WSHandlerFactory getWsHandlerFactory() {
		return wsHandlerFactory;
	}


	public void setWsHandlerFactory(WSHandlerFactory wsHandlerFactory) {
		this.wsHandlerFactory = wsHandlerFactory;
	}


}
