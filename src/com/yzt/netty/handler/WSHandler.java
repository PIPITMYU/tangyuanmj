package com.yzt.netty.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.yzt.netty.adapter.KeepAliveHandlerAdapter;
import com.yzt.netty.client.WSClient;
import com.yzt.netty.client.WSClientManager;
import com.yzt.netty.resolver.UpgradeResolver;
import com.yzt.netty.util.MessageUtils;

/**
 * 
 * @Title:  WSHandler     
 * @Description:    TODO  
 * @author: zc    
 * @date:   2018年3月9日 下午2:43:37   
 * @version V1.0 
 * @Copyright: 2018 云智通 All rights reserved.
 */
public class WSHandler  extends ChannelInboundHandlerAdapter {

    private UpgradeResolver upgradeResolver;

    private WSAdapterMapping wsAdapterMapping;

    private WSClientManager wsClientManager;
    
	private static Log log = LogFactory.getLog(WSHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    	try {
            if (msg instanceof FullHttpRequest) {
                //处理http请求
                FullHttpRequest request = (FullHttpRequest) msg;
                String uri = request.uri();
                if (uri.startsWith("//")) {
                    request.setUri(uri.substring(1));
                }
                String[] temps = getTemps(uri);
                String tempUri = temps[0];
                WSClient wsClient = new WSClient();
                String clientId = getChannelCtxId(ctx);
                //先注册请求处理器
                try {
                	wsAdapterMapping.registWSAdapter(tempUri,wsClient);
                } catch (Exception e) {
                	log.error("WSHandler.channelReadError：没有具体映射的请求处理器" , e);
                    //处理异常 没有具体映射的请求处理器
                    upgradeResolver.handleRequestError(ctx, request , e);
                    return ;
                }
                
                WebSocketServerHandshaker handshaker = null;
                // upgrade 与 websocket 握手过程
                if ((handshaker = upgradeResolver.handleRequest(ctx, request )) != null) {
                    //设置请求参数
                	wsClient.setId(clientId);
                    wsClient.setChannelHandlerContext(ctx);
                    wsClient.setHandshaker(handshaker);
                    //注册连接管理器
                    wsClientManager.putWSClient(clientId, wsClient);
                    wsClient.setIp(getRealIp(ctx.channel().remoteAddress().toString()));
                    //完成后调用
                    wsClient.getWsAdapter().onUpgradeCompleted(ctx , wsClient);
                }
            } else if (msg instanceof WebSocketFrame) {
                //处理websocket请求
                String clientId = getChannelCtxId(ctx);
                //获取请求处理器
                WebSocketFrame webSocketFrame = (WebSocketFrame) msg;
                WSClient wsClient = wsClientManager.getWSClient(clientId);
                //处理请求
                if (wsClient!=null) {
                    wsClient.getWsAdapter().handleRequest(ctx,webSocketFrame,wsClient);
				}
            } else {
                throw new RuntimeException("无法处理的请求");
            }
		} catch (Exception e) {
			log.error("channelRead error", e);
		}finally{
			ReferenceCountUtil.release(msg);//释放消息，防止内存溢出吧
		}
    }
    public String getRealIp(String ip) {
    	if (ip!=null) {
        	String[] temp = ip.split("(\\d{1,3}\\.){3}\\d{1,3}");
        	if (temp!=null&&temp.length>0) {
				for(String t:temp){
					ip = ip.replace(t, "");
				}
			}
		}
    	return ip;
	}
    
    public static String[] getTemps(String uri){
    	StringBuffer sb = new StringBuffer("/");
    	if (uri!=null) {
    		for(int i=1;i<uri.length();i++){
        		if('/'==uri.charAt(i)){
        			sb.append("=");
        		}else{
        			sb.append(uri.charAt(i));
        		}
        	}
		}
    	return sb.toString().split("=");
    }
    
    
    public String getChannelCtxId(ChannelHandlerContext ctx){
        String id = ctx.channel().id().asShortText();
        return id;
    }
    
    public static synchronized void heartToClient(){
		KeepAliveHandlerAdapter.sendPingMessageToAll();
		sended = true;
    }
    static boolean sended = false;
    public static synchronized boolean isSendHeart(){
    	return sended;
    }
    
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
    		throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent event = (IdleStateEvent) evt;
			if (event.state() == IdleState.READER_IDLE) {
//				log.error(ctx.channel().isWritable());
				KeepAliveHandlerAdapter.clearNotPingPongMessage();
//				log.error("read idle!clearNotPingPongMessage OVER    "+ctx.channel().id().asShortText()+"\r\n");
			}else if(event.state() == IdleState.WRITER_IDLE){
//				log.error("write idle");
				
//				log.error(ctx.channel().isActive());
//				log.error(ctx.channel().isOpen());
//				log.error(ctx.channel().isRegistered());
//				log.error(ctx.channel().isWritable());
//				MessageUtils.sendPingMessage(wsClientManager.getWSClient(ctx.channel().id().asShortText()));
				
				if (isSendHeart()) {
//					log.error("sended:"+sended);
				}else{
					heartToClient();
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								Thread.sleep(5000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							sended = false;
						}
					},"changeSendedThread").start();
//					log.error("write idle !sendPingMessageToAll OVER    "+ctx.channel().id().asShortText()+"\r\n");
				}
				
				
//				wsClientManager.removeWSClient(ctx.channel().id().asShortText());
			}
			else if(event.state() == IdleState.ALL_IDLE){
				KeepAliveHandlerAdapter.clearNotPingPongMessage();
//				log.error("all idle!clearNotPingPongMessage OVER    "+ctx.channel().id().asShortText()+"\r\n");
//				log.error(ctx.channel().isActive());
			}
		} else {
			super.userEventTriggered(ctx, evt);
		}
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	
		wsClientManager.removeWSClient(ctx.channel().id().asShortText());
		ctx.channel().close();
    	super.channelInactive(ctx);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String id = ctx.channel().id().asShortText();
        WSClient webSocketClient = wsClientManager.getWSClient(id);
        if (webSocketClient != null) {
            WebSocketServerHandshaker handshaker = webSocketClient.getHandshaker();
            Channel channel = ctx.channel();
            if (channel.isOpen()) {
                handshaker.close(ctx.channel() , new CloseWebSocketFrame());
            }
            wsClientManager.removeWSClient(webSocketClient.getId());
        }
        // TODO Auto-generated method stub
        log.error("WSHandler.exceptionCaught", cause);
    }


	public UpgradeResolver getUpgradeResolver() {
		return upgradeResolver;
	}

	public void setUpgradeResolver(UpgradeResolver upgradeResolver) {
		this.upgradeResolver = upgradeResolver;
	}

	public WSClientManager getWsClientManager() {
		return wsClientManager;
	}

	public void setWsClientManager(WSClientManager wsClientManager) {
		this.wsClientManager = wsClientManager;
	}


	public WSAdapterMapping getWsAdapterMapping() {
		return wsAdapterMapping;
	}


	public void setWsAdapterMapping(WSAdapterMapping wsAdapterMapping) {
		this.wsAdapterMapping = wsAdapterMapping;
	}
	

}
