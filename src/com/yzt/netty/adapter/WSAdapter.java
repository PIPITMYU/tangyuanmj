package com.yzt.netty.adapter;

import io.netty.channel.ChannelHandlerContext;

import java.util.Map;

import com.yzt.netty.client.WSClient;

/**
 * 根handler类
 * @Title:  WSAdapter     
 * @Description:    TODO  
 * @author: zc    
 * @date:   2018年3月9日 下午2:42:43   
 * @version V1.0 
 * @Copyright: 2018 云智通 All rights reserved.
 */
public interface WSAdapter {

    /*
    *
    * 处理客户端请求，处理请求的
    * */
    public void handleRequest(ChannelHandlerContext ctx, Object msg , WSClient wsClient);
    /*
    *
    * 服务端处理(或者是推送处理)，处理回应的
    * */
    public void handleResponse(Map<String , Object> params);

    /*
    * 连接完成时调用
    * */
    public void onUpgradeCompleted(ChannelHandlerContext ctx,WSClient wsClient);
    
    /*
     * 玩家掉线
     * */
    public void playerOut(WSClient wsClient);
    
}
