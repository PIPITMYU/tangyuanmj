package com.yzt.netty.client;

import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class WSClientDao {

	// 心跳 已发送次数
	// key-value:clientId-heartNum,每发一次心跳，会在这里记录一次，收到回应，就会移除，没有移除说明可能失联，最大失联次数是10，如果超过10，说明用户下线，移除webSocket连接
	public static ConcurrentHashMap<String, Integer> pingPongMap = new ConcurrentHashMap<String, Integer>();

	// 存放所有的webSocket连接，key-value：clientId-client
	public static ConcurrentHashMap<String, WSClient> clientsMap = new ConcurrentHashMap<String, WSClient>();
	
	
	public Collection<WSClient> getAllWSClients(){
		return clientsMap.values();
	}
	
	public void putWSClient(String clientId,WSClient wsClient){
		clientsMap.put(clientId , wsClient);
		pingPongMap.remove(clientId);
		
	}
	
	public WSClient getWSClient(String clientId) {
		WSClient wsClient = clientsMap.get(clientId);
        return wsClient;
    }
	
	
	public WSClient removeWSClient(String clientId){
		WSClient wsClient = clientsMap.remove(clientId);
		if (wsClient != null) {
			pingPongMap.remove(clientId);
		}
		return wsClient;
	}
	
	public void sendPingMessageToAll(){
		pingPongMap.clear();
		if (clientsMap.isEmpty()) {
			return;
		}
		Set<String> keySet = clientsMap.keySet();
		for (String key : keySet) {
            WSClient wsClient = clientsMap.get(key);
            //往客户端发ping 客户端会返回pong 可以用来判断客户端存活
            PingWebSocketFrame pingWebSocketFrame = new PingWebSocketFrame();
            wsClient.getChannelHandlerContext().channel().writeAndFlush(pingWebSocketFrame);
            //标记为已发送
            pingPongMap.put(key ,1);
        }
	}
	
	public void clearNotPingPongMessage(){
        if (clientsMap.isEmpty()) {
            return ;
        }
        CloseWebSocketFrame closeWebSocketFrame = new CloseWebSocketFrame();
        Set<String> keySet = pingPongMap.keySet();
        
        for (String key : keySet) {
            Integer num = pingPongMap.get(key);
            if (num != null && num.intValue() == 1) {
                WSClient wsClient = clientsMap.get(key);
                //关闭websocket // 握手关闭连接
                wsClient.getHandshaker().close(wsClient.getChannelHandlerContext().channel() , closeWebSocketFrame);
            }
            pingPongMap.remove(key);
        }
        pingPongMap.clear();
	}
	
	public void sendMessageToAll(String message){
        if (clientsMap.isEmpty()) {
            return ;
        }
        Set<String> keySet = clientsMap.keySet();

        for (String key : keySet) {
        	WSClient wsClient = clientsMap.get(key);
            TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(message);
            if (wsClient.getChannelHandlerContext().channel().isOpen() && wsClient.getChannelHandlerContext().channel().isWritable()) {
                wsClient.getChannelHandlerContext().channel().writeAndFlush(textWebSocketFrame);
            } else {
                clientsMap.remove(key);
            }
        }
	}
	
}
