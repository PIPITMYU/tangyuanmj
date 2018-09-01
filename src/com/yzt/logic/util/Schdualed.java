package com.yzt.logic.util;


import java.util.Calendar;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.yzt.logic.util.redis.RedisUtil;
import com.yzt.netty.adapter.WSAdapter;
import com.yzt.netty.client.WSClientManager;

public class Schdualed implements InitializingBean , ApplicationContextAware{

    @SuppressWarnings("unused")
	private static ApplicationContext applicationContext;

    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    
    private WSClientManager wsClientManager;

	private static Log log = LogFactory.getLog(Schdualed.class);

    public void pushFrame(){}
    public void pushLocation(){}

    @Override
    public void afterPropertiesSet() throws Exception {

    	/**
    	 * 以固定的频率执行任务，不管上个任务是否执行完，到时间直接执行下一个任务，第三个参数为时间
    	 */
//        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    WSAdapter wsAdapter = applicationContext.getBean("mjLogicAdapter" , WSAdapter.class);
//                    wsAdapter.handleResponse(null);
//                	log.info("总人数："+wsClientManager.getAllWSClients().size());
//                	log.info("pingpongsize："+wsClientManager.getAllPingClient());
//                } catch (Exception e) {
//                	 log.error("ERROR", e);
//                }
//            }
//        } , 1L , 10 , TimeUnit.SECONDS);
        
        /**
         * 以固定延迟执行任务，在上个任务执行完成之后的固定时间之后，再执行下一个任务,第三个参数为时间
         */
//        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                	WSAdapter wsAdapter = applicationContext.getBean("mjLogicAdapter" , WSAdapter.class);
//                	wsAdapter.handleResponse(null);
//                	log.info("总人数："+wsClientManager.getAllWSClients().size());
//                	log.info("pingpongsize："+wsClientManager.getAllPingClient());
//                } catch (Exception e) {
//                	 log.error("ERROR", e);
//                }
//            }
//        } , 1L , 10 , TimeUnit.SECONDS);
        
        
        /**
         * 以固定延迟执行任务，在上个任务执行完成之后的固定时间之后，再执行下一个任务,第三个参数为时间
         */
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                	cleanPlayRecord();
                	cleanPlayDaiKaiRecord();
                } catch (Exception e) {
                    log.error("ERROR", e);
                }
            }
        } , 100L , 3600l , TimeUnit.SECONDS);
        
        
        /**
    	 * 以固定的频率执行任务，不管上个任务是否执行完，到时间直接执行下一个任务，第三个参数为时间
    	 */
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                	BackFileUtil.deletePlayRecord();
                } catch (Exception e) {
                	log.error("ERROR", e);
                }
            }
        } , getDelay(3, 0) , 3600l * 24l , TimeUnit.SECONDS);
        
        /**
         * 以固定延迟执行任务，在上个任务执行完成之后的固定时间之后，再执行下一个任务,第三个参数为时间
         */
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                	RoomUtil.checkFreeRoomTask();
                } catch (Exception e) {
                    log.error("ERROR", e);
                }
            }
        } , 60l, 5l , TimeUnit.SECONDS);
    }
    
    //到最近的每一天的 hour小时  second分钟要多少秒
    public static long getDelay(int hour,int second){
    	long now = System.currentTimeMillis();
    	Calendar instance = Calendar.getInstance();
    	instance.set(Calendar.HOUR_OF_DAY, hour);
    	instance.set(Calendar.SECOND, second);
    	
    	long delay = 0l;
    	if(now < instance.getTimeInMillis())
    		delay = instance.getTimeInMillis() - now;
    	else{
    		instance.add(Calendar.DAY_OF_YEAR, 1);
    		delay = instance.getTimeInMillis() - now;
    	}
    	return delay < 0l ? 0l : (delay / 1000l);
    }
    
    public static void cleanPlayRecord(){
    	try {
    		int cleanNum = 0;
    		Set<String> recordKeys = RedisUtil.getKeys(Cnst.REDIS_PLAY_RECORD_PREFIX_ROE_USER.concat("*"));
    		if (recordKeys!=null&&recordKeys.size()>0) {
    			long ct = System.currentTimeMillis();
    			boolean go = true;
    			for(String key:recordKeys){
    				go = true;
    				while(go){
    					go = false;
    					String record = RedisUtil.rpop(key);//这个只是战绩表hash的field
    		    		if (record!=null) {
    						try {
    							Long createTime = Long.parseLong(record.split("-")[1]);
    							if ((ct-createTime)<Cnst.BACKFILE_STORE_TIME) {//如果没有过期，需要把记录放回原位
    								RedisUtil.rpush(key, null, record);
    							}else{//继续删除
    								record = null;
    								go = true;
    								cleanNum++;
    							}
    						} catch (Exception e) {
    							log.error("ERROR", e);
    						}
    					}
    				}
    			}
    		}
        	log.info("每小时清理战绩完成，共清理过期记录"+cleanNum+"条");
		} catch (Exception e) {
			log.error("ERROR", e);
		}
    	
    }

    public static void cleanPlayDaiKaiRecord(){
    	try {
    		int cleanNum = 0;
    		Set<String> recordKeys = RedisUtil.getKeys(Cnst.REDIS_PLAY_RECORD_PREFIX_ROE_DAIKAI.concat("*"));
    		if (recordKeys!=null&&recordKeys.size()>0) {
    			long ct = System.currentTimeMillis();
    			boolean go = true;
    			for(String key:recordKeys){
    				go = true;
    				while(go){
    					go = false;
    					String record = RedisUtil.rpop(key);//这个只是战绩表hash的field
    		    		if (record!=null) {
    						try {
    							Long createTime = Long.parseLong(record.split("-")[1]);
    							if ((ct-createTime)<Cnst.BACKFILE_STORE_TIME) {//如果没有过期，需要把记录放回原位
    								RedisUtil.rpush(key, null, record);
    							}else{//继续删除
    								record = null;
    								go = true;
    								cleanNum++;
    							}
    						} catch (Exception e) {
    							log.error("ERROR", e);
    						}
    					}
    				}
    			}
    		}
        	log.info("每小时清理代开战绩完成，共清理过期记录"+cleanNum+"条");
		} catch (Exception e) {
			log.error("ERROR", e);
		}
    	
    }
    
    public WSClientManager getWsClientManager() {
		return wsClientManager;
	}
	public void setWsClientManager(WSClientManager wsClientManager) {
		this.wsClientManager = wsClientManager;
	}
	@Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

}
