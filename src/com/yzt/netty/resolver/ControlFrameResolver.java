package com.yzt.netty.resolver;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.*;

/**
 * 处理控制帧接口
 * @Title:  ControlFrameResolver     
 * @Description:    TODO  
 * @author: zc    
 * @date:   2018年3月9日 下午2:44:16   
 * @version V1.0 
 * @Copyright: 2018 云智通 All rights reserved.
 */
public interface ControlFrameResolver {


    public void onWebSocketFrameClosed(ChannelHandlerContext ctx, CloseWebSocketFrame closeFrame, WebSocketServerHandshaker handshaker);

    public void onWebSocketFramePing(ChannelHandlerContext ctx, PingWebSocketFrame pingFrame);

    public void onWebSocketFramePong(ChannelHandlerContext ctx, PongWebSocketFrame pongFrame);


}
