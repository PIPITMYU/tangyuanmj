package com.yzt.logic.mj.executer;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSONObject;
import com.yzt.logic.mj.function.ClubInfoFunctions;
import com.yzt.logic.mj.function.GameFunctions;
import com.yzt.logic.mj.function.HallFunctions;
import com.yzt.logic.mj.function.MessageFunctions;
import com.yzt.logic.mj.function.TCPGameFunctions;
import com.yzt.netty.client.WSClient;
import com.yzt.netty.util.MessageUtils;

public class Executer {
	private static Log log = LogFactory.getLog(Executer.class);
	
	private static ExecutorService executeThread = Executors.newFixedThreadPool(16);
	
	//存放所有的interfaceId，所有请求的方法必须在这里注册,value的值为1，是正常请求，为0，是测试请求
	public static Map<Integer,Integer> interfaceIds = new ConcurrentHashMap<Integer, Integer>();
	static{
		interfaceIds.put(100100, 1);
		interfaceIds.put(100102, 1);
		interfaceIds.put(100103, 1);
		interfaceIds.put(100002, 1);
		interfaceIds.put(100003, 1);
		interfaceIds.put(100004, 1);
		interfaceIds.put(100005, 1);
		interfaceIds.put(100006, 1);
		interfaceIds.put(100007, 1);
		interfaceIds.put(100008, 1);
		interfaceIds.put(100009, 1);
		interfaceIds.put(100010, 1);
		interfaceIds.put(100011, 1);
		interfaceIds.put(100012, 1);
		interfaceIds.put(100013, 1);
		interfaceIds.put(100015, 1);
		interfaceIds.put(100016, 1);
		interfaceIds.put(100200, 1);
		interfaceIds.put(100201, 1);
		interfaceIds.put(100203, 1);
		interfaceIds.put(100204, 1);
		interfaceIds.put(100205, 1);
		interfaceIds.put(100206, 1);
		interfaceIds.put(100207, 1);
		interfaceIds.put(100999, 0);
		//俱乐部
		interfaceIds.put(500000, 1);
		interfaceIds.put(500001, 1);
		interfaceIds.put(500002, 1);
		interfaceIds.put(500003, 1);
		interfaceIds.put(500004, 1);
		interfaceIds.put(500005, 1);
		interfaceIds.put(500006, 1);
		interfaceIds.put(500007, 1);
		
		interfaceIds.put(999997, 0);
		interfaceIds.put(999998, 0);
		interfaceIds.put(999999, 0);
		interfaceIds.put(10086, 0);
	}
	
	public static void execute(WSClient wsClient,Map<String,Object> readData){
		executeThread.execute(new ReceiveTask(wsClient, readData));
	}
	
	static class ReceiveTask implements Runnable{
		Map<String,Object> readData;
		WSClient wsClient;
		public ReceiveTask(WSClient wsClient,Map<String,Object> readData) {
            this.readData = readData;
            this.wsClient = wsClient;
		}
		@Override
		public void run() {
			Long t1 = System.currentTimeMillis();
			int interfaceId = Integer.parseInt(readData.get("interfaceId").toString());
			try {
				log.info("["+interfaceId+"-RECEIVE_DATE]:"+readData);
				logic(wsClient, readData,interfaceId);
			} catch (Exception e) {
				String errStr = "服务器异常"+interfaceId;
				log.error(errStr, e);
				JSONObject result = TCPGameFunctions.getError(interfaceId);
				MessageUtils.sendMessage(wsClient, result.toJSONString());
//				wsClient.getChannelHandlerContext().close();
			}finally{
    			log.info("请求处理时间："+(System.currentTimeMillis()-t1));
			}
		}
	}
	
	private static void logic(WSClient wsClient,Map<String,Object> readData,int interfaceId) throws Exception{
        switch (interfaceId) {
        	//FIXME 所有新加的协议都放这个处理
        case 100002:
			HallFunctions.interface_100002(wsClient, readData);
			break;
		case 100003:
			HallFunctions.interface_100003(wsClient, readData);
			break;
		case 100004:
			HallFunctions.interface_100004(wsClient, readData);
			break;
		case 100005:
			HallFunctions.interface_100005(wsClient, readData);
			break;
		case 100006:
			HallFunctions.interface_100006(wsClient, readData);
			break;
		case 100007:
			HallFunctions.interface_100007(wsClient, readData);
			break;// 经典玩法创建房间
		case 100008:
			HallFunctions.interface_100008(wsClient, readData);
			break;
		case 100009:
			HallFunctions.interface_100009(wsClient, readData);
			break;
		case 100010:
			HallFunctions.interface_100010(wsClient, readData);
			break;
		case 100011:
			HallFunctions.interface_100011(wsClient, readData);
			break;
		case 100012:
			HallFunctions.interface_100012(wsClient, readData);
			break;
		case 100013:
			HallFunctions.interface_100013(wsClient, readData);
			break;
		case 100015:
			HallFunctions.interface_100015(wsClient, readData);
			break;
		case 100016:
			HallFunctions.interface_100016(wsClient, readData);
			break;

		// 推送消息段
		case 100100:
			MessageFunctions.interface_100100(wsClient, readData);
			break;// 大接口
		case 100102:
			MessageFunctions.interface_100102(wsClient, readData);
			break;// 小结算
		case 100103:
			MessageFunctions.interface_100103(wsClient, readData);
			break;// 大结算
		case 100999:
			MessageFunctions.interface_100999(wsClient, readData);
			break;// 大结算

		// 游戏中消息段
		case 100200:
			GameFunctions.interface_100200(wsClient, readData);
			break;
		case 100201:
			GameFunctions.interface_100201(wsClient, readData);
			break;
		case 100203:
			GameFunctions.interface_100203(wsClient, readData);
			break;
		case 100204:
			GameFunctions.interface_100204(wsClient, readData);
			break;
		case 100205:
			GameFunctions.interface_100205(wsClient, readData);
			break;
		case 100206:
			GameFunctions.interface_100206(wsClient, readData);
			break;
		case 100207:
			GameFunctions.interface_100207(wsClient, readData);
			break;
		//俱乐部
		case 500000:
			ClubInfoFunctions.interface_500000(wsClient, readData);
			break;
		case 500001:
			ClubInfoFunctions.interface_500001(wsClient, readData);
			break;
		case 500002:
			ClubInfoFunctions.interface_500002(wsClient, readData);
			break;
		case 500003:
			ClubInfoFunctions.interface_500003(wsClient, readData);
			break;
		case 500004:
			ClubInfoFunctions.interface_500004(wsClient, readData);
			break;
		case 500005:
			ClubInfoFunctions.interface_500005(wsClient, readData);
			break;
		case 500006:
			ClubInfoFunctions.interface_500006(wsClient, readData);
			break;
		case 500007:
			ClubInfoFunctions.interface_500007(wsClient, readData);
			break;
			
			
			
		case 999997:
			TCPGameFunctions.changeUserMj(wsClient, readData);
			break;
		case 999998:
			TCPGameFunctions.seeRoomMj(wsClient, readData);
			break;
		case 999999:
			TCPGameFunctions.setRoomMj(wsClient, readData);
			break;
		case 10086:
			TCPGameFunctions.testJieSan(wsClient, readData);
			break;
        	//FIXME  如果牵涉到登录模块  验证player.updateTime 是否为null  或者时间已经过去好几天(天数根据具体情况改动) 
        	//如果满足上面任何一个情况  调用UserLogin.getUserInfoByOpenId 获取最新的玩家数据 更新到redis
        	//如果是新玩家 必须调用UserLogin.getUserInfoByOpenId 获取玩家全部数据 
        	
        	//1.redis取数据
        	//2.如果取不到从本地DB取  如果有取出的updateTime= null
        	//3.如果现在还没有 或者player.updateTime = null 或者updateTime显示很久没更新 就从UserLogin.getUserInfoByOpenId 获取
        	// 根据获取的根据具体业务处理 新增 更新等
        	
        	//最后无论如何 往redis存一份player对象
        	//大接口如果新登录而且有房间 记得通知其他玩家自己上线
        	
		}
	}
	
}
