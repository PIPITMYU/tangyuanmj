package com.yzt.netty.adapter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.yzt.netty.client.WSClient;
import com.yzt.netty.client.WSClientManager;
import com.yzt.netty.util.MessageUtils;

/**
 * 
 * 服务端保活清除过期或者失联连接 处理器
 * 策略是通过定时器
 * 1.遍历客户端发送ping消息 ，然后在缓存中 pingPongMap  标记 1已发送 (或者n 表示发送次数)
 * 2.客户端接收ping 消息会返回pong 消息 (websocket协议标准 见RFC6455)
 * 3.在接收端 doHandleRequest() 方法中(接收到非close frame )说明客户端有反馈，表示存活，删除 pingPongMap
 * 4.接着下一个周期定时器去遍历 pingPongMap 查出已经发送ping 无响应超过 MAX_RE_PING 次数
 * 5.删除超过 MAX_RE_PING 无响应的客户端
 * 6.每次接收到客户端的消息，都会调用 doHandleRequest() 方法清除 pingPongMap 表示客户端存活
 * @Title:  KeepAliveHandlerAdapter     
 * @Description:    TODO  
 * @author: zc    
 * @date:   2018年3月9日 下午2:42:09   
 * @version V1.0 
 * @Copyright: 2018 云智通 All rights reserved.
 */
public abstract class KeepAliveHandlerAdapter<T extends WebSocketFrame> extends AbstractFrameHandlerAdapter<T>
        implements ApplicationContextAware , ApplicationListener<ContextRefreshedEvent> {
	
	private static Log log = LogFactory.getLog(KeepAliveHandlerAdapter.class);

    public static ApplicationContext mApplicationContext;

    //轮训时间 检测过期连接 定时器定时时间
    private final static int SCHEDULE_SECONDS = 10;
    private static ScheduledExecutorService scheduleService = Executors.newScheduledThreadPool(1);

    /*标记状态*/
    private static volatile boolean isSent = true;

    /** 允许保活次数， 超过这个数值认为失联，清理连接，第二次没有回应，即代表断连，直接清理**/
    private static volatile int MAX_RE_PING = 2;




    /*
    * 给所有客户端发送ping 消息
    *
    * */
    public static void sendPingMessageToAll(){
    	WSClientManager wsClientManager = mApplicationContext.getBean(WSClientManager.class);
        Collection<WSClient> clients = wsClientManager.getAllWSClients();
        if (clients != null) {
            for (WSClient client : clients) {
                MessageUtils.sendPingMessage(client);
                wsClientManager.putPingClient(client.getId());
            }
        }else{
        	wsClientManager.removeAllPingClient();
        }
    }


    /*
    * 清理上次保活操作发送ping 消息得不到反馈的连接
    *
    *
    * */
    public static void clearNotPingPongMessage(){
    	WSClientManager wsClientManager = mApplicationContext.getBean(WSClientManager.class);
        Collection<WSClient> clients = wsClientManager.getPingClients(MAX_RE_PING);
        if (clients != null) {
            for (WSClient client : clients) {
//            	log.info("clearNotPingPongMessage============》客户端断开，id为"+client.getId()+"客户端userId"+client.getUserId()+",当前连接数为："+wsClientManager.getAllWSClients().size());
                wsClientManager.removeWSClient(client.getId());
            }
        }
    }


    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        super.setApplicationContext(context);

        mApplicationContext = context;
    }



    /*
    *
    * 收到关闭请求删除相应客户端
    * */
    @Override
    public void doHandleRequest(ChannelHandlerContext ctx, Object msg, WSClient wsClient) {
        super.doHandleRequest(ctx, msg, wsClient);
        WSClientManager wsClientManager = applicationContext.getBean(WSClientManager.class);
        //每次接收到前端数据
        if (!CloseWebSocketFrame.class.equals(wsClient.getClass())) {
        	wsClientManager.removePingClient(ctx.channel().id().asShortText());
        } else {
        	wsClientManager.removeWSClient(ctx.channel().id().asShortText());
        }
    }


    //每次走两遍……，所以一次清理，一次心跳
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
//        if (event.getApplicationContext().getParent() == null) {
//            scheduleService.scheduleAtFixedRate(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        if (mApplicationContext != null) {
//                            if (isSent) {
//                                isSent = false;
//                                //定时发送心跳
//                                sendPingMessageToAll();
//                            } else {
//                                isSent = true;
//                                clearNotPingPongMessage();
//                            }
//                        }
//                    } catch (Exception e) {
//                    	log.error("KeepAliveHandlerAdapter.onApplicationEventError" , e);
//                    }
//                }
//            } , 1L , SCHEDULE_SECONDS , TimeUnit.SECONDS);
//        }
    }
}
