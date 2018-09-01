package com.yzt.netty.resolver;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import com.yzt.netty.client.WSClient;

public interface DataFrameResolver<T extends WebSocketFrame> {


    /**
     * 处理数据帧接口
     * @Title: handlerWebSocketFrameData   
     * @Description: TODO(这里用一句话描述这个方法的作用)   
     * @param: @param wsClient
     * @param: @param webSocketFrame      
     * @return: void      
     * @throws
     */
    public void handlerWebSocketFrameData(WSClient wsClient, T webSocketFrame);


}
