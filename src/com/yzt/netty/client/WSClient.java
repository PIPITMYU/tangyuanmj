package com.yzt.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.yzt.netty.adapter.WSAdapter;

/**
 * 每一个连接实体
 * @Title:  NettyClient     
 * @Description:    TODO  
 * @author: zc    
 * @date:   2018年1月16日 下午3:37:20   
 * @version V1.0 
 * @Copyright: 2018 云智通 All rights reserved.
 */
public class WSClient {

	private WebSocketServerHandshaker handshaker;//握手处理
	private ChannelHandlerContext channelHandlerContext;
	private String id;//clientId，由netty分配
	private Long userId;
	private String ip;
	private Map<String, Object> reqParam;
    /** 请求处理器 **/
    private WSAdapter wsAdapter;

	public void setAttribute(String key, Object value) {
		if (this.reqParam == null) {
			this.reqParam = new ConcurrentHashMap<String, Object>();
		}
		this.reqParam.put(key, value);
	}

	public Object getAttribute(String key) {
		if (this.reqParam == null) {
			return null;
		}
		return this.reqParam.get(key);
	}

	public WebSocketServerHandshaker getHandshaker() {
		return handshaker;
	}

	public void setHandshaker(WebSocketServerHandshaker handshaker) {
		this.handshaker = handshaker;
	}

	public ChannelHandlerContext getChannelHandlerContext() {
		return channelHandlerContext;
	}

	public void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
		this.channelHandlerContext = channelHandlerContext;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public WSAdapter getWsAdapter() {
		return wsAdapter;
	}

	public void setWsAdapter(WSAdapter wsAdapter) {
		this.wsAdapter = wsAdapter;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	

}
