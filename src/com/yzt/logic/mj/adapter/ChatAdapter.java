package com.yzt.logic.mj.adapter;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSONObject;
import com.yzt.logic.util.Cnst;
import com.yzt.netty.adapter.KeepAliveHandlerAdapter;
import com.yzt.netty.annotation.WSRequestMapping;
import com.yzt.netty.client.WSClient;
import com.yzt.netty.client.WSClientManager;
import com.yzt.netty.util.MessageUtils;

@WSRequestMapping(uri = "/chat" )
public class ChatAdapter extends KeepAliveHandlerAdapter<TextWebSocketFrame> {
	private static Log log = LogFactory.getLog(ChatAdapter.class);

	@Override
	public void handleResponse(Map<String, Object> params) {
		WSClientManager wsClientManager = applicationContext.getBean(WSClientManager.class);
        //聊天通道
		Collection<WSClient> clients = wsClientManager.getAllWSClients();
        if (clients != null) {
            for (WSClient client : clients) {
                Channel channel = client.getChannelHandlerContext().channel();
                String id = channel.id().asShortText();
                JSONObject json = new JSONObject();
                json.put("id" , id);
                json.put("type" , 0);
                TextWebSocketFrame textFrame = new TextWebSocketFrame(json.toJSONString());
                channel.writeAndFlush(textFrame);
            }
        }
	}

	@Override
	public void onUpgradeCompleted(ChannelHandlerContext ctx, WSClient wsClient) {
		String id = ctx.channel().id().asShortText();
        JSONObject json = new JSONObject();
        json.put("id" , id);
        json.put("type" , 0);
        TextWebSocketFrame textFrame = new TextWebSocketFrame(json.toJSONString());
        ctx.writeAndFlush(textFrame);
	}

	@SuppressWarnings("unused")
	@Override
	public void handlerWebSocketFrameData(WSClient wsClient,TextWebSocketFrame webSocketFrame) {
        String content = webSocketFrame.text();
        if (!Cnst.PING_MESSAGE.equals(content)) {
        	log.debug("ChatHandlerAdapter ....content : " + content);
            JSONObject chatContent = JSONObject.parseObject(content);
            String contentText = chatContent.getString("content");
            String targetId = chatContent.getString("targetId");

    		WSClientManager wsClientManager = applicationContext.getBean(WSClientManager.class);

            JSONObject sendContent = new JSONObject();
            sendContent.put("content" ,  "test content");
            sendContent.put("type" , 1);
            sendContent.put("sendId" , wsClient.getId());
            MessageUtils.sendMessage(wsClient , sendContent.toJSONString() );
        }
	}

	@Override
	public void playerOut(WSClient wsClient) {
		System.out.println("ChatAdapter out");
	}
	
}
