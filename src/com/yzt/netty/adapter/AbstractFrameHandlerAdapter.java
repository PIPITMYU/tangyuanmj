package com.yzt.netty.adapter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.yzt.netty.annotation.WSRequestMapping;
import com.yzt.netty.client.WSClient;
import com.yzt.netty.resolver.AbstractControlFrameResolver;
import com.yzt.netty.resolver.DataFrameResolver;



/**
 * 
 * 父类处理器
 * 封装了 control frame 的默认处理逻辑若需要拓展可以自己重写方法
 * 提供了 applicationContext 用于获取spring bean 逻辑处理业务
 * 也可以通过重写 doHandleRequest() 来添加逻辑
 * @Title:  AbstractFrameHandlerAdapter     
 * @Description:    TODO  
 * @author: zc    
 * @date:   2018年3月9日 下午2:41:36   
 * @version V1.0 
 * @Copyright: 2018 云智通 All rights reserved.
 */
public abstract class AbstractFrameHandlerAdapter<T extends WebSocketFrame> extends AbstractControlFrameResolver
        implements WSAdapter, DataFrameResolver<T>, ApplicationContextAware {

    public ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog(AbstractFrameHandlerAdapter.class);


    static int num = 0;
    
    @SuppressWarnings("unchecked")
	public void handleRequest(ChannelHandlerContext ctx, Object msg , WSClient wsClient){
    	
        if (wsClient != null && msg instanceof WebSocketFrame ) {
            WebSocketFrame frame = (WebSocketFrame) msg;

            doHandleRequest(ctx , frame , wsClient );

            // 判断是否关闭链路的指令
            if ((frame instanceof CloseWebSocketFrame)) {
                if (wsClient.getHandshaker()!=null) {
                	wsClient.getHandshaker().close(ctx.channel(), ((CloseWebSocketFrame)frame).retain());
				}
                return ;
            } else if (frame instanceof PingWebSocketFrame) {
                ctx.channel().write(new PongWebSocketFrame(((PingWebSocketFrame)frame).content().retain()));
                return;
            } else if (frame instanceof PongWebSocketFrame) {
                onWebSocketFramePong(ctx, (PongWebSocketFrame)frame);
                return;
            } else  if (frame instanceof TextWebSocketFrame) {
            	//TextWebSocketFrame 消息处理
                // 本例程仅支持文本消息，不支持二进制消息
            	handlerWebSocketFrameData(wsClient , (T) frame);
            } else  if (frame instanceof BinaryWebSocketFrame) {
            	//BinaryWebSocketFrame 消息处理
                handlerWebSocketFrameData(wsClient , (T) frame);
            } else {
                num++;
                log.error("当前错误数："+num);
                throw new UnsupportedOperationException(String.format( "%s frame types not supported", frame.getClass().getName()));
                
            }
        }

    }

    /*
    * 后置请求处理器
    *
    *
    * */
    public void doHandleRequest(ChannelHandlerContext ctx, Object msg , WSClient wsClient) {
    	
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.applicationContext = context;
    }

    public String getUri(){
        String uri = null;
        WSRequestMapping requestMapping = this.getClass().getAnnotation(WSRequestMapping.class);
        if (requestMapping != null) {
            uri = requestMapping.uri();
        }
        return uri;
    }



}
