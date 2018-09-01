package com.yzt.logic.mj.adapter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSONObject;
import com.yzt.logic.mj.domain.Player;
import com.yzt.logic.mj.domain.RoomResp;
import com.yzt.logic.mj.executer.Executer;
import com.yzt.logic.mj.function.MessageFunctions;
import com.yzt.logic.util.Cnst;
import com.yzt.logic.util.GameUtil.StringUtils;
import com.yzt.logic.util.redis.RedisUtil;
import com.yzt.netty.adapter.KeepAliveHandlerAdapter;
import com.yzt.netty.annotation.WSRequestMapping;
import com.yzt.netty.client.WSClient;
import com.yzt.netty.client.WSClientManager;

@WSRequestMapping(uri = "/mj" )
public class MJLogicAdapter extends KeepAliveHandlerAdapter<TextWebSocketFrame> {
	
	private static Log log = LogFactory.getLog(MJLogicAdapter.class);

	@Override
	public void handleResponse(Map<String, Object> params) {
//		WSClientManager wsClientManager = applicationContext.getBean(WSClientManager.class);
//        //聊天通道
//		 Collection<WSClient> clients = wsClientManager.getAllWSClients();
//        if (clients != null) {
//            for (WSClient client : clients) {
//                Channel channel = client.getChannelHandlerContext().channel();
//                String id = channel.id().asShortText();
//                JSONObject json = new JSONObject();
//                json.put("id" , id);
//                json.put("type" , 0);
//                TextWebSocketFrame textFrame = new TextWebSocketFrame(json.toJSONString());
//                channel.writeAndFlush(textFrame);
//            }
//        }
	}

	@Override
	public void onUpgradeCompleted(ChannelHandlerContext ctx, WSClient wsClient) {
//		String id = ctx.channel().id().asShortText();
//        JSONObject json = new JSONObject();
//        json.put("id" , id);
//        json.put("type" , 0);
//        TextWebSocketFrame textFrame = new TextWebSocketFrame(json.toJSONString());
//        ctx.writeAndFlush(textFrame);
		WSClientManager wsClientManager = applicationContext.getBean(WSClientManager.class);
		log.info("新客户端接入，id为"+wsClient.getId()+",当前连接数为："+wsClientManager.getAllWSClients().size());
	}

	@Override
	public void handlerWebSocketFrameData(WSClient wsClient,TextWebSocketFrame webSocketFrame) {
        String content = webSocketFrame.text();
        if (!Cnst.PING_MESSAGE.equals(content)) {
        	try {
        		JSONObject obj = null;
            	try {
            		obj = JSONObject.parseObject(content);
    			} catch (Exception e) {
    				log.error("收到无效数据-->"+content, e);
    				//toAdd
    				return;
    			}
                //路由转换
                Map<String,Object> readData = new ConcurrentHashMap<String, Object>();
                Iterator<String> iterator = obj.keySet().iterator();
                while(iterator.hasNext()) {  
                    String str = iterator.next();  
                    readData.put(Cnst.ROUTE_MAP_SEND.get(str), obj.get(str));
                }
                //转换完成
                Integer interfaceId = Integer.parseInt(readData.get("interfaceId").toString());
                
                if (interfaceId==null||!Executer.interfaceIds.containsKey(interfaceId)) {//不是注册的方法
                	//toAdd
                	MessageFunctions.illegalRequest(interfaceId, wsClient);
                	return;
        		}else{
        			if (!Cnst.isTest&&Executer.interfaceIds.get(interfaceId)==0) {//在非测试环境下，请求测试接口
        				//toAdd
        				MessageFunctions.illegalRequest(interfaceId, wsClient);
                    	return;
        			}
//        			if(interfaceId != 100100 && readData.get("userId") != null){
//        				if(wsClient.getUserId() != null && !wsClient.getUserId().equals(StringUtils.parseLong(readData.get("userId")))){
//        					MessageFunctions.illegalRequest(interfaceId, wsClient);
//                        	return;
//        				}
//        			}
                	Executer.execute(wsClient, readData);
        		}
			} catch (Exception e) {
				log.error("MJLogicAdapter.handlerWebSocketFrameData", e);
				//toAdd
			}
        }
	}
	
	/**
	 * 玩家掉线的逻辑
	 */
	@Override
	public void playerOut(WSClient wsClient) {
		//toAdd
		WSClientManager wsClientManager = applicationContext.getBean(WSClientManager.class);
		log.info("playerOut============》客户端断开，id为"+wsClient.getId()+"客户端userId"+wsClient.getUserId()+",当前连接数为："+wsClientManager.getAllWSClients().size());
		//FIXME 玩家离开房间后  通知房间内其他玩家  另外把玩家标记设置为不在线
		Player player = RedisUtil.getPlayerByUserId(String.valueOf(wsClient.getUserId()));
		if(player == null)
			return;
//		log.info("oldClientId"+wsClient.getId()+"newClientId"+player.getChannelId());		
		if(player.getRoomId()!=null && player.getChannelId().equals(wsClient.getId())){
			RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(player.getRoomId()));
			if (room!=null&&!room.getState().equals(Cnst.ROOM_STATE_YJS)) {
				List<Player> players = RedisUtil.getPlayerList(room);
				if (players!=null&&players.size()>0) {
					MessageFunctions.interface_100109(players, Cnst.PLAYER_LINE_STATE_OUT, player.getUserId());
				}
				if (room.getRoomType() == Cnst.ROOM_TYPE_2 && !player.getUserId().equals(room.getCreateId())) {
					MessageFunctions.interface_100112(player, room, Cnst.PLAYER_EXTRATYPE_LIXIAN);
				}
			}else{
				player.setState(Cnst.PLAYER_LINE_STATE_OUT);
				//TODO
				
			}
		}else{
			player.setState(Cnst.PLAYER_LINE_STATE_OUT);
			//TODO
			
		}
	}

}
