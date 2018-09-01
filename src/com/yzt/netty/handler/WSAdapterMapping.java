package com.yzt.netty.handler;

import io.netty.handler.codec.http.FullHttpRequest;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.StringUtils;

import com.yzt.netty.adapter.WSAdapter;
import com.yzt.netty.annotation.WSRequestMapping;
import com.yzt.netty.client.WSClient;
import com.yzt.netty.util.MessageUtils;

/**
 * 
 * @Title:  WSAdapterMapping     
 * @Description:    TODO  
 * @author: zc    
 * @date:   2018年3月9日 下午2:43:44   
 * @version V1.0 
 * @Copyright: 2018 云智通 All rights reserved.
 */
@SuppressWarnings("unused")
public class WSAdapterMapping implements  ApplicationContextAware , ApplicationListener<ContextRefreshedEvent> ,BeanDefinitionRegistryPostProcessor {

    private static ApplicationContext applicationContext;
    private static BeanDefinitionRegistry beanRegistry;
	private static ConfigurableListableBeanFactory beanFactory;

    /**key-value : url-wsAdapter*/
    private static ConcurrentHashMap<String , WSAdapter> urlAdapterMap = new ConcurrentHashMap<>();

    private volatile boolean isInit = false;
    
    public void init() {
        if (!isInit) {
            synchronized (this) {
                if (!isInit) {
                    String[] handlerAdapterNames = applicationContext.getBeanNamesForType(WSAdapter.class);
                    if (handlerAdapterNames != null && handlerAdapterNames.length > 0) {
                        for (String handlerAdapterName : handlerAdapterNames ) {
                        	WSAdapter wsAdapter = applicationContext.getBean(handlerAdapterName ,WSAdapter.class );
                            WSRequestMapping requestMapping = wsAdapter.getClass().getAnnotation(WSRequestMapping.class);
                            if (requestMapping != null) {
                                String uri = requestMapping.uri();
                                urlAdapterMap.put(uri , wsAdapter);
                            }
                        }
                    }
                }
            }
        }
    }



    /*
    * 获取当前连接绑定的请求处理器
    *
    * */
    public WSAdapter getWSAdapter(String url) {
        return urlAdapterMap.get(url);
    }



    /*
    *
    * 为请求注册请求处理器
    *
    * */
    public void registWSAdapter(String uri,WSClient wsClient) {
        init();
        WSAdapter wsAdapter = null;
        if (StringUtils.hasLength(uri)) {
	        wsAdapter = getWSAdapter(uri);
	        if (wsAdapter == null) {
	            throw new RuntimeException("未找到合适的请求处理器wsAdapter: " + uri);
	        }
        }
        wsClient.setWsAdapter(wsAdapter);
    }
    
    public WSAdapter registWsAdapter(String uri){
    	 WSAdapter wsAdapter = null;
         if (StringUtils.hasLength(uri)) {
             wsAdapter = getWSAdapter(uri);
             if (wsAdapter == null) {
                 throw new RuntimeException("未找到合适的请求处理器wsAdapter: " + uri);
             }
         }
         return wsAdapter;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

	@SuppressWarnings("static-access")
	@Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        this.beanRegistry = registry;
    }

	@SuppressWarnings("static-access")
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory mBeanFactory) throws BeansException {
        this.beanFactory = mBeanFactory;



    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            init();
        }
    }

}
