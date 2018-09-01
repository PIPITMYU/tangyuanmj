package com.yzt.logic.mj.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yzt.logic.mj.adapter.MJLogicAdapter;
import com.yzt.logic.mj.domain.Player;
import com.yzt.logic.mj.domain.RoomResp;
import com.yzt.logic.util.Cnst;
import com.yzt.logic.util.RoomUtil;
import com.yzt.logic.util.GameUtil.StringUtils;
import com.yzt.logic.util.redis.RedisUtil;
import com.yzt.netty.client.WSClient;
import com.yzt.netty.client.WSClientManager;
import com.yzt.netty.util.MessageUtils;

public class TCPGameFunctions extends MJLogicAdapter {

	public static final Log logger = LogFactory.getLog(TCPGameFunctions.class);

	public static WSClientManager getWSClientManager() {
		return mApplicationContext.getBean(WSClientManager.class);
	}

	/**
	 * 玩家加入房间封装的json
	 * 
	 * @return
	 */
	public static JSONObject getUserInfoJSON(Player p) {
		JSONObject obj = new JSONObject();
		obj.put("userId", p.getUserId());
		obj.put("position", p.getPosition());
		obj.put("score", p.getScore());
		obj.put("money", p.getMoney());
		obj.put("playStatus", p.getPlayStatus());
		obj.put("userName", p.getUserName());
		obj.put("userImg", p.getUserImg());
		obj.put("ip", p.getIp());
		obj.put("gender", p.getGender());
		WSClient wsClient = TCPGameFunctions.getWSClientManager().getWSClient(
				p.getChannelId());
		if (wsClient == null) {
			obj.put("state", 2);
		} else {
			obj.put("state", 1);
		}
		return obj;
	}

	/**
	 * 获取统一格式的返回obj
	 * 
	 * @param interfaceId
	 * @param state
	 * @param object
	 * @return
	 */
	public static JSONObject getJSONObj(Integer interfaceId, Integer state,
			Object object) {
		logger.info("["+interfaceId+"-SEND_DATA]:"+object);
		JSONObject obj = new JSONObject();
		obj.put("interfaceId", interfaceId);
		obj.put("state", state);
		obj.put("message", "");
		obj.put("info", object);
		obj.put("others", "");
		obj = getNewObj(obj);
		return obj;
	}

	public static JSONObject getError(Integer interfaceId) {
		JSONObject obj = new JSONObject();
		obj.put("interfaceId", interfaceId);
		obj.put("state", 2);
		obj.put("message", "服务器异常");
		obj.put("info", "");
		obj.put("others", "");
		obj = getNewObj(obj);
		return obj;
	}

	public static JSONObject getInfoJSON(Integer action, Integer extra,
			Long toUserId) {
		JSONObject info = new JSONObject();
		info.put("action", action);
		info.put("extra", extra);
		info.put("toUserId", toUserId);
		return info;
	}

	/**
	 * 返回统一的 info jsonObject.put("action",-1);//行为编码 jsonObject.put("extra",0);
	 * //要发的牌 jsonObject.put("toUserId",currentPlayer.getUserId());//下一个.
	 * jsonObject.put("nextAction",0); //可能有多个操作 参数是list
	 * jsonObject.put("nextActionUserId",room.getNextActionUserId());//下一个操作的操作人
	 * 
	 * @return
	 */
	public static JSONObject getInfoJSON(Integer action, Integer extra,
			Long toUserId, List<Integer> nextAction, Long nextActionUserId) {
		JSONObject info = new JSONObject();
		info.put("action", action);
		info.put("extra", extra);
		info.put("toUserId", toUserId);
		info.put("nextAction", nextAction);
		info.put("nextActionUserId", nextActionUserId);
		return info;
	}

	// 路由转换
	/**
	 * 如:把interfaceId 转换成a 缩短传输的数据.
	 * 
	 * @param temp
	 * @return
	 */
	public static JSONObject getNewObj(JSONObject temp) {
		Iterator<String> iterator = temp.keySet().iterator();
		JSONObject result = new JSONObject();
		while (iterator.hasNext()) {
			String str = iterator.next();
			Object o = temp.get(str);
			if (o instanceof List) {
				result.put(Cnst.ROUTE_MAP.get(str) == null ? str
						: Cnst.ROUTE_MAP.get(str), getNewList(o));
			} else if (o instanceof Map) {
				result.put(Cnst.ROUTE_MAP.get(str) == null ? str
						: Cnst.ROUTE_MAP.get(str), getNewMap(o));
			} else {
				result.put(Cnst.ROUTE_MAP.get(str) == null ? str
						: Cnst.ROUTE_MAP.get(str), o);
			}
		}
		return result;
	}

	public static List getNewList(Object list) {
		List<Object> temp1 = (List<Object>) list;
		List<Object> temp = new ArrayList<Object>(temp1);
		if (temp != null && temp.size() > 0) {
			for (int i = 0; i < temp.size(); i++) {
				Object o = temp.get(i);
				if (o instanceof List) {
					temp.set(i, getNewList(o));
				} else if (o instanceof Map) {// 基本上全是这个类型
					temp.set(i, getNewMap(o));
				} else {// 默认为String
					try {
						JSONObject obj = JSONObject.parseObject(o.toString());
						temp.set(i, getNewObj(obj));
					} catch (Exception e) {
						// e.printStackTrace();
					}
				}
			}
		}
		return temp;
	}

	public static Map getNewMap(Object map) {
		Map<String, Object> temp1 = (Map<String, Object>) map;
		Map<String, Object> temp = new HashMap<String, Object>(temp1);
		Map<String, Object> result = new ConcurrentHashMap<String, Object>();
		if (temp != null && temp.size() > 0) {
			Iterator<String> iterator = temp.keySet().iterator();
			while (iterator.hasNext()) {
				String str = String.valueOf(iterator.next());
				Object o = temp.get(str);
				if (o instanceof List) {
					result.put(Cnst.ROUTE_MAP.get(str) == null ? str
							: Cnst.ROUTE_MAP.get(str), getNewList(o));
				} else if (o instanceof Map) {
					result.put(Cnst.ROUTE_MAP.get(str) == null ? str
							: Cnst.ROUTE_MAP.get(str), getNewMap(o));
				} else {
					try {
						try {
							JSONObject obj = JSONObject.parseObject(o
									.toString());
							result.put(Cnst.ROUTE_MAP.get(str) == null ? str
									: Cnst.ROUTE_MAP.get(str), getNewObj(obj));
						} catch (Exception e) {
							result.put(Cnst.ROUTE_MAP.get(str) == null ? str
									: Cnst.ROUTE_MAP.get(str), o);
						}
					} catch (Exception e) {

					}
				}
			}
		}
		return result;
	}

	// 转换完成

	/**
	 * 房间不存在
	 * 
	 * @param interfaceId
	 * @param session
	 */
	public static void roomDoesNotExist(Integer interfaceId, WSClient channel) {
		Map<String, Object> info = new HashMap<>();
		info.put("reqState", Cnst.REQ_STATE_4);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 玩家在其他房间
	 * 
	 * @param interfaceId
	 * @param session
	 */
	public static void playerExistOtherRoom(Integer interfaceId,
			WSClient channel) {
		Map<String, Object> info = new HashMap<>();
		info.put("reqState", Cnst.REQ_STATE_3);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 房间已满
	 * 
	 * @param interfaceId
	 * @param session
	 */
	public static void roomFully(Integer interfaceId, WSClient channel) {
		Map<String, Object> info = new HashMap<>();
		info.put("reqState", Cnst.REQ_STATE_5);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 玩家房卡不足
	 * 
	 * @param interfaceId
	 * @param session
	 */
	public static void playerMoneyNotEnough(Integer interfaceId,
			WSClient channel, Integer roomType) {
		Map<String, Object> info = new HashMap<>();
		info.put("reqState", Cnst.REQ_STATE_2);// 余额不足，请及时充值
		info.put("roomType", roomType);// 余额不足，请及时充值
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 代开房间不能超过10个
	 * 
	 * @param interfaceId
	 * @param session
	 */
	public static void roomEnough(Integer interfaceId, WSClient channel) {
		Map<String, Object> info = new HashMap<>();
		info.put("reqState", Cnst.REQ_STATE_11);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 非法请求
	 * 
	 * @param session
	 * @param interfaceId
	 */
	public static void illegalRequest(Integer interfaceId, WSClient channel) {
		Map<String, Object> info = new HashMap<>();
		JSONObject result = getJSONObj(interfaceId, 0, info);
		result.put("c", "非法请求！");
		MessageUtils.sendMessage(channel, result.toJSONString());
		channel.getChannelHandlerContext().close();
	}

	/**
	 * 参数错误
	 */
	public static void parameterError(Integer interfaceId, WSClient channel) {
		Map<String, Object> info = new HashMap<>();
		JSONObject result = getJSONObj(interfaceId, 0, info);
		result.put("c", "参数错误！");
		MessageUtils.sendMessage(channel, result.toJSONString());
		channel.getChannelHandlerContext().close();
	}

	/**
	 * 敬请期待
	 * 
	 * @param interfaceId
	 * @param session
	 */
	public static void comingSoon(Integer interfaceId, WSClient channel) {
		Map<String, Object> info = new HashMap<>();
		info.put("reqState", Cnst.REQ_STATE_FUYI);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 游戏中，不能退出房间
	 * 
	 * @param interfaceId
	 * @param session
	 */
	public static void roomIsGaming(Integer interfaceId, WSClient channel) {
		Map<String, Object> info = new HashMap<>();
		info.put("reqState", Cnst.REQ_STATE_6);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/***
	 * 修改房间手牌 慎用
	 */
	public static void changeUserMj(WSClient channel,
			Map<String, Object> readData) {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomId"));
		String str = String.valueOf(readData.get("pais"));
		List<Integer> pais = JSONArray.parseArray(str, Integer.class);
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId));
		if (room != null && room.getState() == Cnst.ROOM_STATE_GAMIING) {
			Player p = RedisUtil.getPlayerByUserId(String.valueOf(userId));
			if (p != null) {
				p.setCurrentMjList(pais);
				RedisUtil.updateRedisData(null, p);
			}
		}
	}

	public static void seeRoomMj(WSClient channel, Map<String, Object> readData) {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomId"));
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId));
		if (room != null && room.getState() == Cnst.ROOM_STATE_GAMIING) {
			JSONObject info = new JSONObject();
			info.put("pais", room.getCurrentMjList());
			JSONObject result = getJSONObj(interfaceId, 1, info);
			MessageUtils.sendMessage(channel, result.toJSONString());
		}
	}

	public static void setRoomMj(WSClient channel, Map<String, Object> readData) {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomId"));
		String str = String.valueOf(readData.get("pais"));
		List<Integer> pais = JSONArray.parseArray(str, Integer.class);
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId));
		if (room != null && room.getState() == Cnst.ROOM_STATE_GAMIING) {
			room.setCurrentMjList(pais);
			RedisUtil.updateRedisData(room, null);
		}
	}

	public static void testJieSan(WSClient channel, Map<String, Object> readData) {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));// 10086
		Integer roomId = StringUtils.parseInt(readData.get("roomId"));
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId));
		if (room != null && room.getState() == Cnst.ROOM_STATE_GAMIING) {
			// 中途准备阶段解散房间不计入回放中
			List<Integer> xiaoJSInfo = new ArrayList<Integer>();
			for (int i = 0; i < room.getPlayerIds().length; i++) {
				xiaoJSInfo.add(0);
			}
			room.addXiaoJuInfo(xiaoJSInfo);
			room.setState(Cnst.ROOM_STATE_YJS);
			List<Player> players = RedisUtil.getPlayerList(room);

			RoomUtil.updateDatabasePlayRecord(room);

			RedisUtil.deleteByKey(Cnst.REDIS_PREFIX_ROOMMAP.concat(String
					.valueOf(roomId)));// 删除房间
			if (players != null && players.size() > 0) {
				for (Player p : players) {
					// 初始化玩家 TODO
					p.initPlayer(null, Cnst.PLAYER_STATE_DATING, null);
					RedisUtil.updateRedisData(null, p);
				}
			}
		} else {
			System.out.println("*******强制解散房间" + roomId + "，房间不存在");
		}
	}

}