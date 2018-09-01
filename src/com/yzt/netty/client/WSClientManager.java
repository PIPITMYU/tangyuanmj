package com.yzt.netty.client;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * 连接实体管理类
 * @Title:  WSClientManager     
 * @Description:    TODO  
 * @author: zc    
 * @date:   2018年3月9日 下午2:43:16   
 * @version V1.0 
 * @Copyright: 2018 云智通 All rights reserved.
 */
public class WSClientManager {
	
    private WSClientDao wsClientDao;
    
    @SuppressWarnings("static-access")
	public Integer getAllPingClient(){
    	return wsClientDao.pingPongMap.size();
    }
    
    
    public void putWSClient(String clientId,WSClient wsClient){
    	wsClientDao.putWSClient(clientId, wsClient);
    }
    
    public WSClient getWSClient(String clientId){
    	WSClient wsClient = null;
    	wsClient = wsClientDao.getWSClient(clientId);
    	return wsClient;
    }
    
    public Collection<WSClient> getAllWSClients(){
    	return wsClientDao.getAllWSClients();
    }
    
//    public Collection<WSClient> getClientsByUri (String uri) {
//        return wsClientDao.getClientsByUri(uri);
//	}
    
    
    public void putPingClient(String clientId){
    	Integer num = WSClientDao.pingPongMap.get(clientId);
    	if (num!=null) {
    		WSClientDao.pingPongMap.put(clientId , num.intValue() + 1);
		}else{
			WSClientDao.pingPongMap.put(clientId , 1);
		}
    }
    
    public void removeAllPingClient(){
    	WSClientDao.pingPongMap.clear();
    }

    public void removePingClient (String clientId) {
    	WSClientDao.pingPongMap.remove(clientId);
    }
    
    public Collection<WSClient> getPingClients (int pingNum) {
        if (WSClientDao.pingPongMap.isEmpty()) {
            return null;
        }
        Collection<WSClient> clients = null;
        Set<String> clientIds = WSClientDao.pingPongMap.keySet();
        if (clientIds != null) {
            clients = new ArrayList<WSClient>();
            for (String clientId : clientIds) {
                Integer mPingnum = WSClientDao.pingPongMap.get(clientId);
                //超过ping 限制次数
                if (mPingnum!=null&&mPingnum.intValue() >= pingNum) {
                	WSClient webSocketClient = wsClientDao.getWSClient(clientId);
                    clients.add(webSocketClient);
                }
            }
        }
        return clients;
    }
    

    public WSClient removeWSClient(String clientId){
    	WSClient wsClient = wsClientDao.removeWSClient(clientId);
    	if(wsClient != null){
    		wsClient.getWsAdapter().playerOut(wsClient);
        	Channel channel = wsClient.getChannelHandlerContext().channel();
            if (channel.isOpen()) {
            	wsClient.getHandshaker().close(channel , new CloseWebSocketFrame());
            }
            return wsClient;
    	}
    	return null;
    }
    
    

	public WSClientDao getWsClientDao() {
		return wsClientDao;
	}

	public void setWsClientDao(WSClientDao wsClientDao) {
		this.wsClientDao = wsClientDao;
	}
    
    
}
