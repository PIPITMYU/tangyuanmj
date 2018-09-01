package com.yzt.netty.util;

import com.yzt.logic.util.ProjectInfoPropertyUtil;

/**
 * 
 * @Title:  Cnst     
 * @Description:    常量类，仅供netty包用  
 * @author: zc    
 * @date:   2018年1月16日 下午3:45:12   
 * @version V1.0 
 * @Copyright: 2018 云智通 All rights reserved.
 */
public class Cnst {
	
	/**WShandler用*/
	protected static final int NETTY_PORT = Integer.parseInt(ProjectInfoPropertyUtil.getProperty("port", ""));
	
	/**协议处理类用*/
	protected final static String WEBSOCKET= "websocket";
	protected final static String Upgrade= "Upgrade";
	protected final static String SEC_WEBSOCKET_PROTOCOL= "Sec-WebSocket-Protocol";
	protected final static String DEFAULT_WEBSOCKET_ADDRESS_FORMAT="ws://%s";
}
