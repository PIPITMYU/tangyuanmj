package com.yzt.netty.handler;

import com.yzt.netty.client.WSClientManager;
import com.yzt.netty.resolver.UpgradeResolver;

/**
 * 
 * @Title:  WSHandlerFactory     
 * @Description:    TODO  
 * @author: zc    
 * @date:   2018年3月9日 下午2:43:49   
 * @version V1.0 
 * @Copyright: 2018 云智通 All rights reserved.
 */
public class WSHandlerFactory  {

    private UpgradeResolver upgradeResolver;
    private WSClientManager wsClientManager;
    private WSAdapterMapping wsAdapterMapping;

    public WSHandler newWSHandler() {
    	WSHandler wsHandler = new WSHandler();
        // websocket 升级请求处理器
        wsHandler.setUpgradeResolver(upgradeResolver);
        // webscoket 客户端存储器
        wsHandler.setWsClientManager(wsClientManager);
        wsHandler.setWsAdapterMapping(wsAdapterMapping);
        return wsHandler;
    }

    public WSOutHandler newWSOutHandler() {
    	WSOutHandler wsOutHanler = new WSOutHandler();
    	wsOutHanler.setWSClientManager(wsClientManager);
        return wsOutHanler;
    }

	public UpgradeResolver getUpgradeResolver() {
		return upgradeResolver;
	}

	public void setUpgradeResolver(UpgradeResolver upgradeResolver) {
		this.upgradeResolver = upgradeResolver;
	}

	public WSClientManager getWsClientManager() {
		return wsClientManager;
	}

	public void setWsClientManager(WSClientManager wsClientManager) {
		this.wsClientManager = wsClientManager;
	}

	public WSAdapterMapping getWsAdapterMapping() {
		return wsAdapterMapping;
	}

	public void setWsAdapterMapping(WSAdapterMapping wsAdapterMapping) {
		this.wsAdapterMapping = wsAdapterMapping;
	}


  
}
