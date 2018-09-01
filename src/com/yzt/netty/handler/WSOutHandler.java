package com.yzt.netty.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;

import com.yzt.netty.client.WSClient;
import com.yzt.netty.client.WSClientManager;
/**
 * 
 * @Title:  WSOutHandler     
 * @Description:    TODO  
 * @author: zc    
 * @date:   2018年3月9日 下午2:43:54   
 * @version V1.0 
 * @Copyright: 2018 云智通 All rights reserved.
 */
public class WSOutHandler extends ChannelOutboundHandlerAdapter {

    private WSClientManager wsClientManger;

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.disconnect(ctx, promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        String id = ctx.channel().id().asShortText();
        WSClient webSocketClient = wsClientManger.getWSClient(id);
        if (webSocketClient != null) {
            WebSocketServerHandshaker handshaker = webSocketClient.getHandshaker();
            Channel channel = ctx.channel();
            if (channel.isOpen()) {
                handshaker.close(ctx.channel() , new CloseWebSocketFrame());
            }
            wsClientManger.removeWSClient(webSocketClient.getId());
        }
        super.close(ctx, promise);
    }


    public WSClientManager getWSManager() {
        return wsClientManger;
    }

    public void setWSClientManager(WSClientManager wsClientManager) {
        this.wsClientManger = wsClientManager;
    }
}
