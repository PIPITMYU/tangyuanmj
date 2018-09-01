package com.yzt.logic.mj.function;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yzt.logic.mj.dao.UserLogin;
import com.yzt.logic.mj.dao.UserMapper;
import com.yzt.logic.mj.domain.Feedback;
import com.yzt.logic.mj.domain.Player;
import com.yzt.logic.mj.domain.RoomResp;
import com.yzt.logic.mj.domain.SystemMessage;
import com.yzt.logic.util.Cnst;
import com.yzt.logic.util.CommonUtil;
import com.yzt.logic.util.RoomUtil;
import com.yzt.logic.util.GameUtil.StringUtils;
import com.yzt.logic.util.redis.RedisUtil;
import com.yzt.netty.client.WSClient;
import com.yzt.netty.util.MessageUtils;

/**
 * Created by Administrator on 2017/7/8. 大厅方法类
 */
public class HallFunctions extends TCPGameFunctions {

	/**
	 * 大厅查询战绩
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100002(WSClient channel,
			Map<String, Object> readData) {
		logger.info("大厅查询战绩,interfaceId -> 100002");
		Integer interfaceId = Integer.parseInt(String.valueOf(readData
				.get("interfaceId")));
		String userId = String.valueOf(readData.get("userId"));
		Integer page = Integer.parseInt(String.valueOf(readData.get("page")));
		String userKey = Cnst.REDIS_PLAY_RECORD_PREFIX_ROE_USER.concat(userId);
		Long pageSize = RedisUtil.llen(userKey);
		int start = (page - 1) * Cnst.PAGE_SIZE;
		int end = start + Cnst.PAGE_SIZE - 1;
		List<String> keys = RedisUtil.lrange(userKey, start, end);
		JSONObject info = new JSONObject();
		List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
		for (String roomKey : keys) {
			String redisRecordKey = Cnst.REDIS_PLAY_RECORD_PREFIX
					.concat(roomKey);
			if (RedisUtil.exists(redisRecordKey)) {
				Map<String, String> roomInfos = RedisUtil
						.hgetAll(redisRecordKey);
				maps.add(roomInfos);
			} else {
				RedisUtil.lrem(userKey, roomKey);
			}
		}
		info.put("infos", maps);
		info.put("pages", pageSize == null ? 0
				: pageSize % Cnst.PAGE_SIZE == 0 ? pageSize / Cnst.PAGE_SIZE
						: (pageSize / Cnst.PAGE_SIZE + 1));
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 大厅查询系统消息
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100003(WSClient channel,
			Map<String, Object> readData) {
		logger.info("大厅查询系统消息,interfaceId -> 100003");
		Integer interfaceId = Integer.parseInt(String.valueOf(readData
				.get("interfaceId")));
		Integer page = Integer.parseInt(String.valueOf(readData.get("page")));
		List<SystemMessage> info = UserMapper.getSystemMessage(null, (page - 1)
				* Cnst.PAGE_SIZE, Cnst.PAGE_SIZE);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 大厅请求联系我们
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100004(WSClient channel,
			Map<String, Object> readData) {
		logger.info("大厅请求联系我们,interfaceId -> 100004");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Map<String, String> info = new HashMap<>();
		info.put("connectionInfo", UserMapper.getConectUs());
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 大厅请求帮助信息
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100005(WSClient channel,
			Map<String, Object> readData) {
		logger.info("大厅请求帮助信息,interfaceId -> 100005");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Map<String, String> info = new HashMap<>();
		info.put("help", "帮助帮助帮助帮助帮助帮助帮助帮助帮助");
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 反馈信息
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100006(WSClient channel,
			Map<String, Object> readData) {
		logger.info("反馈信息,interfaceId -> 100006");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		String content = String.valueOf(readData.get("content"));
		String tel = String.valueOf(readData.get("tel"));
		// 插入反馈信息
		Feedback back = new Feedback();
		back.setContent(content);
		back.setCreateTime(new Date().getTime());
		back.setTel(tel);
		back.setUserId(userId);
		UserMapper.userFeedback(back);
		// 返回反馈信息
		Map<String, String> info = new HashMap<>();
		info.put("content", "感谢您的反馈！");
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 创建房间-经典玩法
	 * 
	 * @param session
	 * @param readData
	 */
	public synchronized static void interface_100007(WSClient channel,
			Map<String, Object> readData) {
		logger.info("创建房间,interfaceId -> 100007");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));// 接口Id
		Long userId = StringUtils.parseLong(readData.get("userId"));// 用户id
		Integer circleNum = StringUtils.parseInt(readData.get("circleNum"));// 局数，详见说明
																			// //
																			// 10(默认)/20
		Integer roomType = StringUtils.parseInt(readData.get("roomType"));// 房间类型
		Integer scoreType = StringUtils.parseInt(readData.get("scoreType"));// 玩法选项
		Integer shenFeng = StringUtils.parseInt(readData.get("shenFeng"));// 玩法选项
		Integer daiTe = StringUtils.parseInt(readData.get("daiTe"));// 玩法选项
		Integer qiDui = StringUtils.parseInt(readData.get("qiDui"));// 玩法选项
		Integer gangZhuang = StringUtils.parseInt(readData.get("gangZhuang"));// 玩法选项
		Integer gangFanBei = StringUtils.parseInt(readData.get("gangFanBei"));// 玩法选项
		Integer diaoYu = StringUtils.parseInt(readData.get("diaoYu"));// 玩法选项
		Player p = RedisUtil.getPlayerByUserId(String.valueOf(userId));
		RoomResp room = new RoomResp();
		Integer needMoney = Cnst.moneyMap.get(circleNum);
		if (p.getMoney() < needMoney) {// 玩家房卡不足
			playerMoneyNotEnough(interfaceId, channel, roomType);
			return;
		}
		if (p.getRoomId() != null) {// 已存在其他房间
			playerExistOtherRoom(interfaceId, channel);
			return;
		}
		if (roomType != null && roomType.equals(Cnst.ROOM_TYPE_2)) {// 代开模式开房，玩家房卡必须大于等于100
			if (p.getMoney() < 100) {
				playerMoneyNotEnough(interfaceId, channel, roomType);
				return;
			}
		}
		if (roomType != null && roomType.equals(Cnst.ROOM_TYPE_2)) {
			// 从自己的代开房间列表中查找代开房间
			Map<String, String> rooms = RedisUtil
					.hgetAll(Cnst.REDIS_PREFIX_DAI_ROOM_LIST + userId);
			int size = 0;
			if (rooms != null) {
				size = rooms.size();
			}
			if (size >= 10) {
				roomEnough(interfaceId, channel);
				return;
			}
		}
		String createTime = String.valueOf(new Date().getTime());
		room.setCreateId(userId);// 创建人
		room.setState(Cnst.ROOM_STATE_CREATED);// 房间状态为等待玩家入坐
		room.setCircleNum(circleNum);// 总局数
		room.setLastNum(circleNum);// 剩余局数
		room.setCircleWind(Cnst.WIND_EAST);// 圈风为东风
		room.setRoomType(roomType);// 房间类型：房主模式或者自由模式
		room.setCreateTime(createTime);// 创建时间，long型数据
		room.setOpenName(p.getUserName());
		room.setScoreType(scoreType);
		room.setShenFeng(shenFeng);
		room.setDaiTe(daiTe);
		room.setQiDui(qiDui);
		room.setGangZhuang(gangZhuang);
		room.setGangFanBei(gangFanBei);
		room.setDiaoYu(diaoYu);
		// 初始化大接口的id
		room.setWsw_sole_action_id(1);
		room.setStartPosition(null);
		// toEdit 需要去数据库匹配，查看房间号是否存在，如果存在，则重新生成
		while (true) {
			room.setRoomId(CommonUtil.getGivenRamdonNum(6));// 设置随机房间密码
			if (RedisUtil.getRoomRespByRoomId(String.valueOf(room.getRoomId())) == null) {
				break;
			}
		}
		Long[] userIds = new Long[4];
		Map<String, Object> info = new HashMap<>();
		Map<String, Object> userInfos = new HashMap<String, Object>();
		// 处理开房模式
		if (roomType == null) {
			illegalRequest(interfaceId, channel);
			return;
		} else if (roomType.equals(Cnst.ROOM_TYPE_1)) {// 房主模式
			// 设置用户信息
			p.setPosition(getWind(null));// 设置庄家位置
			if (p.getPosition().equals(Cnst.WIND_EAST)) {
				room.setZhuangId(userId);
			}
			p.setRoomId(room.getRoomId());
			// TODO
			p.initPlayer(p.getRoomId(), Cnst.PLAYER_STATE_IN, 0);
			userIds[p.getPosition() - 1] = p.getUserId();
			info.put("reqState", Cnst.REQ_STATE_1);
			p.setMoney(p.getMoney() - needMoney);
			userInfos.put("position", p.getPosition());
		} else if (roomType.equals(Cnst.ROOM_TYPE_2)) {// 代开模式
			// 将新创建的房间加入到代开房间列表里面
			RedisUtil.hset(Cnst.REDIS_PREFIX_DAI_ROOM_LIST + userId, room
					.getRoomId().toString(), "1", null);
			p.setMoney(p.getMoney() - needMoney);
			info.put("reqState", Cnst.REQ_STATE_10);
		} else if (roomType.equals(Cnst.ROOM_TYPE_3)) {// AA
			comingSoon(interfaceId, channel);
			return;
		} else {
			illegalRequest(interfaceId, channel);
			return;
		}
		room.setPlayerIds(userIds);
		room.setIp(Cnst.SERVER_IP);
		info.put("userInfo", userInfos);
		// 直接把传来的readData处理 返回
		readData.put("roomId", room.getRoomId());
		readData.put("state", room.getState());
		readData.remove("interfaceId");
		info.put("roomInfo", readData);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
		// 更新redis数据 player roomMap
		RedisUtil.updateRedisData(null, p);
		RedisUtil.setObject(Cnst.REDIS_PREFIX_ROOMMAP.concat(String
				.valueOf(room.getRoomId())), room, Cnst.ROOM_LIFE_TIME_CREAT);
		// 解散房间命令 TODO
		RoomUtil.addFreeRoomTask(StringUtils.parseLong(room.getRoomId()),
				System.currentTimeMillis() + Cnst.ROOM_CREATE_DIS_TIME);
	}

	/**
	 * 加入房间
	 * 
	 * @param session
	 * @param readData
	 */
	public synchronized static void interface_100008(WSClient channel,
			Map<String, Object> readData) {
		logger.info("加入房间,interfaceId -> 100008");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomId"));
		Player p = RedisUtil.getPlayerByUserId(String.valueOf(channel
				.getUserId()));
		// 已经在其他房间里
		if (p.getRoomId() != null) {// 玩家已经在非当前请求进入的其他房间里
			playerExistOtherRoom(interfaceId, channel);
			return;
		}
		// 房间不存在
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId));
		if (room == null || room.getState() == Cnst.ROOM_STATE_YJS) {
			roomDoesNotExist(interfaceId, channel);
			return;
		}
		// 房间人满
		Long[] userIds = room.getPlayerIds(); // 放假所有人
		boolean hasNull = false;
		int jionIndex = 0;
		for (Long uId : userIds) {
			if (uId == null) {
				hasNull = true;
			} else {
				jionIndex++;
			}
		}
		if (!hasNull) {
			roomFully(interfaceId, channel);
			return;
		}
		// 验证ip是否一致
		if (!Cnst.SERVER_IP.equals(room.getIp())) {
			Map<String, Object> info = new HashMap<>();
			info.put("reqState", Cnst.REQ_STATE_14);
			info.put("roomId", roomId);
			info.put("roomIp", room.getIp().concat(":").concat(Cnst.MINA_PORT));
			JSONObject result = getJSONObj(interfaceId, 1, info);
			MessageUtils.sendMessage(channel, result.toJSONString());
			return;
		}
		// 加入房间成功,开始设置用户信息
		p.setPlayStatus(Cnst.PLAYER_STATE_PREPARED);// 准备状态
		p.setRoomId(roomId);
		p.setPosition(getWind(userIds));
		if (p.getPosition().equals(Cnst.WIND_EAST)) { // 东风位置为庄
			room.setZhuangId(userId);
		}
		if (room.getRoomType() == Cnst.ROOM_TYPE_2) {
			if (room.getStartPosition() == null) {
				room.setStartPosition(p.getPosition());
			}
		}
		userIds[p.getPosition() - 1] = p.getUserId();
		// TODO
		p.initPlayer(p.getRoomId(), Cnst.PLAYER_STATE_IN, 0);
		Map<String, Object> info = new HashMap<>();
		info.put("reqState", Cnst.REQ_STATE_1);
		info.put("userId", room.getCreateId());
		info.put("roomId", room.getRoomId());
		info.put("circleNum", room.getCircleNum());
		info.put("roomType", room.getRoomType());
		info.put("scoreType", room.getScoreType());
		info.put("shenFeng", room.getShenFeng());
		info.put("daiTe", room.getDaiTe());
		info.put("gangZhuang", room.getGangZhuang());
		info.put("gangFanBei", room.getGangFanBei());
		info.put("qiDui", room.getQiDui());
		info.put("diaoYu", room.getDiaoYu());
		JSONArray allUserInfos = new JSONArray();
		for (Long ids : userIds) {
			if (ids == null) {
				continue;
			}
			if (ids.equals(userId)) {
				JSONObject userInfo = new JSONObject();
				getJoinRoomUserInfo(p, userInfo, 2);
				allUserInfos.add(userInfo);
				continue;
			}

			Player player = RedisUtil.getPlayerByUserId(String.valueOf(ids));
			JSONObject userInfo = new JSONObject();
			getJoinRoomUserInfo(player, userInfo, 2);
			allUserInfos.add(userInfo);
		}
		info.put("userInfo", allUserInfos);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
		// 更新redis数据
		RedisUtil.updateRedisData(room, p);
		// 通知另外几个人
		for (Long ids : userIds) {
			if (ids == null) {
				continue;
			}
			if (ids.equals(userId)) {
				continue;
			}
			JSONObject userInfo = new JSONObject();
			getJoinRoomUserInfo(p, userInfo, 1);
			Player pp = RedisUtil.getPlayerByUserId(String.valueOf(ids));
			WSClient ws = getWSClientManager().getWSClient(pp.getChannelId());
			if (ws != null) {
				JSONObject result1 = getJSONObj(interfaceId, 1, userInfo);
				MessageUtils.sendMessage(ws, result1.toJSONString());
			}
		}
		// 如果加入的代开房间 通知房主
		if (room.getRoomType() == Cnst.ROOM_TYPE_2
				&& !userId.equals(room.getCreateId())) {
			MessageFunctions.interface_100112(p, room,
					Cnst.PLAYER_EXTRATYPE_ADDROOM);
		}
	}

	private static void getJoinRoomUserInfo(Player p, JSONObject userInfo,
			Integer type) {
		userInfo.put("userId", p.getUserId());
		userInfo.put("position", p.getPosition());
		userInfo.put("userName", p.getUserName());
		userInfo.put("userImg", p.getUserImg());
		userInfo.put("ip", p.getIp());
		userInfo.put("gender", p.getGender());
		if (type == 1) {
			// 发给自己的 或者进入房间通知别人
		} else {
			userInfo.put("playStatus", p.getPlayStatus());
			WSClient wsClient = TCPGameFunctions.getWSClientManager()
					.getWSClient(p.getChannelId());
			if (wsClient == null) {
				userInfo.put("state", 2);
			} else {
				userInfo.put("state", 1);
			}
		}

	}

	/**
	 * 用户点击同意协议
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100009(WSClient channel,
			Map<String, Object> readData) throws Exception {
		logger.info("用户点击同意协议,interfaceId -> 100009");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Player p = RedisUtil.getPlayerByUserId(String.valueOf(channel
				.getUserId()));
		if (p == null) {
			illegalRequest(interfaceId, channel);
			return;
		}
		p.setUserAgree(1);
		Map<String, Object> info = new JSONObject();
		info.put("reqState", Cnst.REQ_STATE_1);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
		// 更新redis数据
		RedisUtil.updateRedisData(null, p);
		/* 刷新数据库，用户同意协议 */
		UserMapper.updateUserAgree(p.getUserId());
	}

	/**
	 * 查看代开房间列表
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100010(WSClient channel,
			Map<String, Object> readData) throws Exception {
		logger.info("查看代开房间列表,interfaceId -> 100010");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		List<Map<String, Object>> info = new ArrayList<Map<String, Object>>();
		String daiKaiKey = Cnst.REDIS_PREFIX_DAI_ROOM_LIST
				+ String.valueOf(userId);
		Map<String, String> hgetAll = RedisUtil.hgetAll(daiKaiKey);
		if (hgetAll != null && hgetAll.size() > 0) {
			Set<String> keySet = hgetAll.keySet();
			for (String roomId : keySet) {
				// 获取每个房间的信息
				RoomResp room = RedisUtil.getRoomRespByRoomId(roomId);
				if (room != null && room.getCreateId().equals(userId)
						&& room.getState() != Cnst.ROOM_STATE_YJS
						&& room.getRoomType() == Cnst.ROOM_TYPE_2) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("roomId", room.getRoomId());
					map.put("createTime", room.getCreateTime());
					map.put("circleNum", room.getCircleNum());
					map.put("lastNum", room.getLastNum());
					map.put("state", room.getState());
					map.put("scoreType", room.getScoreType());
					map.put("shenFeng", room.getShenFeng());
					map.put("daiTe", room.getDaiTe());
					map.put("gangZhuang", room.getGangZhuang());
					map.put("gangFanBei", room.getGangFanBei());
					map.put("qiDui", room.getQiDui());
					map.put("diaoYu", room.getDiaoYu());
					List<Map<String, Object>> playerInfo = new ArrayList<Map<String, Object>>();
					List<Player> list = RedisUtil.getPlayerList(room);
					if (list != null && list.size() > 0) {
						for (Player p : list) {
							Map<String, Object> pinfo = new HashMap<String, Object>();
							pinfo.put("userId", p.getUserId());
							pinfo.put("position", p.getPosition());
							pinfo.put("userName", p.getUserName());
							pinfo.put("userImg", p.getUserImg());
							WSClient wsClient = TCPGameFunctions
									.getWSClientManager().getWSClient(
											p.getChannelId());
							if (wsClient == null) {
								pinfo.put("state", 2);
							} else {
								pinfo.put("state", 1);
							}
							playerInfo.add(pinfo);
						}
					}
					map.put("userInfo", playerInfo);
					info.add(map);
				}
				if (room == null) {
					RedisUtil.hdel(daiKaiKey, String.valueOf(roomId));
				}
			}
			// if(info != null && info.size()>0){
			// Collections.sort(info,new Comparator<Map<String,Object>>() {
			//
			// @Override
			// public int compare(Map<String, Object> o1,
			// Map<String, Object> o2) {
			// if(StringUtils.parseLong(o1.get("createTime")) >
			// StringUtils.parseLong(o2.get("createTime"))){
			// return -1;
			// }
			// return 1;
			// }
			// });
			// }
		}
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 查看历史代开房间列表
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100011(WSClient channel,
			Map<String, Object> readData) throws Exception {
		logger.info("查看历史代开房间列表,interfaceId -> 100011");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		String userId = String.valueOf(readData.get("userId"));
		Integer page = StringUtils.parseInt(readData.get("page"));
		String key = Cnst.REDIS_PLAY_RECORD_PREFIX_ROE_DAIKAI.concat(userId);
		Long pageSize = RedisUtil.llen(key);
		int start = (page - 1) * Cnst.PAGE_SIZE;
		int end = start + Cnst.PAGE_SIZE - 1;
		List<String> keys = RedisUtil.lrange(key, start, end);
		Map<String, Object> info = new HashMap<>();
		List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
		for (String roomKey : keys) {
			String redisRecordKey = Cnst.REDIS_PLAY_RECORD_PREFIX
					.concat(roomKey);
			if (RedisUtil.exists(redisRecordKey)) {
				Map<String, String> roomInfos = RedisUtil
						.hgetAll(redisRecordKey);
				maps.add(roomInfos);
			} else {
				RedisUtil.lrem(key, roomKey);
			}
		}
		info.put("infos", maps);
		info.put("pages", pageSize == null ? 0
				: pageSize % Cnst.PAGE_SIZE == 0 ? pageSize / Cnst.PAGE_SIZE
						: (pageSize / Cnst.PAGE_SIZE + 1));
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 代开模式中踢出玩家
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100012(WSClient channel,
			Map<String, Object> readData) {
		logger.info("准备,interfaceId -> 100012");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		// 房间不存在
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId));
		if (room == null) {
			roomDoesNotExist(interfaceId, channel);
			return;
		}
		try {
			// 验证解散人是否是真正的房主
			Long createId = channel.getUserId();
			if (createId == null || !createId.equals(room.getCreateId())) {
				illegalRequest(interfaceId, channel);
				return;
			}
		} catch (Exception e) {
			illegalRequest(interfaceId, channel);
			return;
		}
		// 房间已经开局
		if (room.getState() != Cnst.ROOM_STATE_CREATED) {
			roomIsGaming(interfaceId, channel);
			return;
		}
		List<Player> list = RedisUtil.getPlayerList(room);
		boolean hasPlayer = false;// 列表中有当前玩家
		for (Player p : list) {
			if (p.getUserId().equals(userId)) {
				// 初始化玩家 TODO
				p.initPlayer(null, Cnst.PLAYER_STATE_DATING, null);
				// 刷新房间用户列表
				Long[] pids = room.getPlayerIds();
				if (pids != null) {
					for (int i = 0; i < pids.length; i++) {
						if (userId.equals(pids[i])) {
							pids[i] = null;
							break;
						}
					}
				}
				// 更新redis数据
				RedisUtil.updateRedisData(room, p);
				hasPlayer = true;
				MessageFunctions.interface_100107(userId,
						Cnst.EXIST_TYPE_EXIST, list);
				break;
			}
		}
		Map<String, String> info = new HashMap<String, String>();
		info.put("reqState",
				String.valueOf(hasPlayer ? Cnst.REQ_STATE_1 : Cnst.REQ_STATE_8));
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 代开模式中房主解散房间
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100013(WSClient channel,
			Map<String, Object> readData) {
		logger.info("代开模式中踢出玩家,interfaceId -> 100013");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomId"));
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId));
		// 房间不存在
		if (room == null) {
			roomDoesNotExist(interfaceId, channel);
			return;
		}
		try {
			// 验证解散人是否是真正的房主
			Long createId = channel.getUserId();
			if (createId == null || !createId.equals(room.getCreateId())) {
				illegalRequest(interfaceId, channel);
				return;
			}
		} catch (Exception e) {
			illegalRequest(interfaceId, channel);
			return;
		}
		// 房间已经开局
		if (room.getState() != Cnst.ROOM_STATE_CREATED) {
			roomIsGaming(interfaceId, channel);
			return;
		}
		List<Player> players = RedisUtil.getPlayerList(room);
		if (players != null && players.size() > 0) {
			for (Player p : players) {
				// 初始化玩家 TODO
				p.initPlayer(null, Cnst.PLAYER_STATE_DATING, null);
			}
			RedisUtil.setPlayersList(players);
		}
		MessageFunctions.interface_100107(channel.getUserId(),
				Cnst.EXIST_TYPE_DISSOLVE, players);
		// 归还玩家房卡
		Player cp = RedisUtil.getPlayerByUserId(String.valueOf(channel
				.getUserId()));
		cp.setMoney(cp.getMoney() + Cnst.moneyMap.get(room.getCircleNum()));
		// 更新房主的redis数据
		RedisUtil.updateRedisData(null, cp);
		RedisUtil.deleteByKey(Cnst.REDIS_PREFIX_ROOMMAP.concat(String
				.valueOf(roomId)));
		String daiKaiKey = Cnst.REDIS_PREFIX_DAI_ROOM_LIST.concat(String
				.valueOf(room.getCreateId()));
		if (RedisUtil.hexists(daiKaiKey, String.valueOf(roomId))) {
			RedisUtil.hdel(daiKaiKey, String.valueOf(roomId));
		}
		Map<String, String> info = new HashMap<String, String>();
		info.put("reqState", String.valueOf(Cnst.REQ_STATE_1));
		info.put("money", String.valueOf(cp.getMoney()));
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 强制解散房间
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */
	public static void interface_100015(WSClient channel,
			Map<String, Object> readData) throws Exception {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomId"));
		logger.info("*******强制解散房间" + roomId);
		Long userId = channel.getUserId();
		if (userId == null) {
			illegalRequest(interfaceId, channel);
			return;
		}
		if (roomId != null) {
			RoomResp room = RedisUtil.getRoomRespByRoomId(String
					.valueOf(roomId));
			if (room != null && room.getCreateId().equals(userId)) {
				if (room.getState() == Cnst.ROOM_STATE_GAMIING) {
					// 中途准备阶段解散房间不计入回放中
					List<Integer> xiaoJSInfo = new ArrayList<Integer>();
					for (int i = 0; i < room.getPlayerIds().length; i++) {
						xiaoJSInfo.add(0);
					}
					room.addXiaoJuInfo(xiaoJSInfo);
					room.setState(Cnst.ROOM_STATE_YJS);
					List<Player> players = RedisUtil.getPlayerList(room);
					RoomUtil.updateDatabasePlayRecord(room);
					RedisUtil.deleteByKey(Cnst.REDIS_PREFIX_ROOMMAP
							.concat(String.valueOf(roomId)));// 删除房间
					if (players != null && players.size() > 0) {
						for (Player p : players) {
							// 初始化玩家 TODO
							p.initPlayer(null, Cnst.PLAYER_STATE_DATING, null);
							RedisUtil.updateRedisData(null, p);
						}
						for (Player p : players) {
							WSClient ws = getWSClientManager().getWSClient(
									p.getChannelId());
							if (ws != null) {
								Map<String, Object> data = new HashMap<String, Object>();
								data.put("interfaceId", 100100);
								data.put("openId", p.getOpenId());
								data.put("cId", Cnst.cid);
								MessageFunctions.interface_100100(ws, data);
							}
						}
					}
					if (room.getRoomType() == Cnst.ROOM_TYPE_2) {
						String daiKaiKey = Cnst.REDIS_PREFIX_DAI_ROOM_LIST
								.concat(String.valueOf(room.getCreateId()));
						if (RedisUtil
								.hexists(daiKaiKey, String.valueOf(roomId))) {
							RedisUtil.hdel(daiKaiKey, String.valueOf(roomId));
						}
					}
				} else {
					logger.info("*******强制解散房间" + roomId + "，房间不存在");
				}
			}
			Map<String, Object> info = new HashMap<>();
			info.put("reqState", Cnst.REQ_STATE_1);
			JSONObject result = MessageFunctions.getJSONObj(interfaceId, 1,
					info);
			MessageUtils.sendMessage(channel, result.toJSONString());
		}
	}

	/**
	 * 获取玩家坐标
	 * 
	 * @param channel
	 * @param readData
	 */
	public static void interface_100016(WSClient channel,
			Map<String, Object> readData) {
		logger.info("获取玩家坐标,interfaceId -> 100016");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Double x_index = Double.parseDouble(String.valueOf(readData
				.get("x_index")));
		Double y_index = Double.parseDouble(String.valueOf(readData
				.get("y_index")));
		Player p = RedisUtil.getPlayerByUserId(String.valueOf(userId));
		if (p != null && x_index != null && y_index != null) {
			p.setX_index(x_index);
			p.setY_index(y_index);
		}
		RedisUtil.updateRedisData(null, p);
		Map<String, Object> info = new HashMap<>();
		info.put("reqState", Cnst.REQ_STATE_1);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 产生随机的风
	 * 
	 * @param players
	 * @return
	 */
	private static Integer getWind(Long[] userIds) {
		List<Integer> ps = new ArrayList<>();
		ps.add(Cnst.WIND_EAST);
		ps.add(Cnst.WIND_SOUTH);
		ps.add(Cnst.WIND_WEST);
		ps.add(Cnst.WIND_NORTH);
		if (userIds != null) {
			for (int i = userIds.length - 1; i >= 0; i--) {
				if (userIds[i] != null) {
					ps.remove(i);
				}
			}
		}
		return ps.get(CommonUtil.getRamdonInNum(ps.size()));
	}

	/**
	 * 或得到的是一个正数，要拿当前玩家的剩余房卡，减去这个值
	 * 
	 * @param userId
	 * @return
	 */
	private static Integer getFrozenMoney(Long userId) {
		int frozenMoney = 0;
		Set<String> roomMapKeys = RedisUtil
				.getSameKeys(Cnst.REDIS_PREFIX_ROOMMAP);
		if (roomMapKeys != null && roomMapKeys.size() > 0) {
			for (String roomId : roomMapKeys) {
				RoomResp room = RedisUtil.getRoomRespByRoomId(roomId);
				if (room.getCreateId().equals(userId)
						&& room.getState() == Cnst.ROOM_STATE_CREATED) {
					frozenMoney += Cnst.moneyMap.get(room.getCircleNum());
				}
			}
		}
		return frozenMoney;
	}

	/**
	 * 返回用户
	 * 
	 * @param openId
	 * @param ip
	 * @return
	 * @throws Exception
	 */
	public static Player getPlayerInfos(String openId, String ip, String cid,
			WSClient channel) {
		if (cid == null || !cid.equals(Cnst.cid)) {
			return null;
		}
		Player p = null;
		long updateTime = 0;
		try {
			String notice = RedisUtil.getStringByKey(Cnst.NOTICE_KEY);
			if (notice == null) {
				notice = UserMapper.getNotice();
				RedisUtil.setObject(Cnst.NOTICE_KEY, notice, null);
			}
			Set<String> openIds = RedisUtil
					.getSameKeys(Cnst.REDIS_PREFIX_OPENIDUSERMAP);
			if (openIds != null && openIds.contains(openId)) {// 用户是断线重连
				Long userId = RedisUtil.getUserIdByOpenId(openId);
				p = RedisUtil.getPlayerByUserId(String.valueOf(userId));
				WSClient ws = getWSClientManager()
						.getWSClient(p.getChannelId());
				p.setNotice(notice);
				p.setState(Cnst.PLAYER_LINE_STATE_INLINE);
				updateTime = p.getUpdateTime() == null ? 0l : p.getUpdateTime();
				if (ws != null) {
					Long tempuserId = ws.getUserId();
					if (ws.getId() != channel.getId()
							&& userId.equals(tempuserId)) {
						MessageFunctions.interface_100106(ws);
					}
				}
				if (p.getPlayStatus() != null
						&& p.getPlayStatus().equals(Cnst.PLAYER_STATE_DATING)) {// 去数据库重新请求用户，//需要减去玩家开的房卡
					Player loaclMysql = UserMapper.findByOpenId(openId, cid);
					if (loaclMysql == null) {
						p = UserLogin.getUserInfoByOpenId(openId);
						if (p == null) {
							return null;
						} else {
							p.setUserAgree(0);
							p.setGender(p.getGender());
							// p.setTotalGameNum("0");
							p.setMoney(Cnst.get_MONEY_INIT(cid));
							p.setLoginStatus(1);
							p.setCid(cid);
							String time = String.valueOf(new Date().getTime());
							p.setLastLoginTime(time);
							p.setSignUpTime(time);
							p.setUpdateTime(System.currentTimeMillis());
							UserMapper.insert(p);

						}
					} else {
						// FIXME 判断是否更新昵称等数据 注意这个是从数据库读取到的数据 所以怎么判断 要注意
						// 但是这个库里面金币是正确的 UID也是正确的 只是昵称不太一样
						if (System.currentTimeMillis() - updateTime > Cnst.updateDiffTime) {
							Player updatep = UserLogin
									.getUserInfoByOpenId(openId);
							p.setUserName(updatep.getUserName());
							p.setUserImg(updatep.getUserImg());
							p.setGender(updatep.getGender());
							p.setUpdateTime(System.currentTimeMillis());
						}
					}
					p.setScore(0);
					p.setIp(ip);
					p.setNotice(notice);
					p.setState(Cnst.PLAYER_LINE_STATE_INLINE);
					p.setPlayStatus(Cnst.PLAYER_STATE_DATING);
					p.setMoney(loaclMysql.getMoney()
							- getFrozenMoney(p.getUserId()));
				}
				// 更新用户ip 最后登陆时间
				// UserMapper.updateIpAndLastTime(openId, clientIp);
				return p;
			}
			p = UserMapper.findByOpenId(openId, cid);
			if (p != null) {// 当前游戏的数据库中存在该用户
				p.setNotice(notice);
				Player redisP = RedisUtil.getPlayerByUserId(String.valueOf(p
						.getUserId()));
				updateTime = (redisP == null || redisP.getUpdateTime() == null) ? 0l
						: redisP.getUpdateTime();
				// FIXME 判断是否更新昵称等数据 注意这个是从数据库读取到的数据 所以怎么判断 要注意
				// 但是这个库里面金币是正确的 UID也是正确的
				if (System.currentTimeMillis() - updateTime > Cnst.updateDiffTime) {
					Player updatep = UserLogin.getUserInfoByOpenId(openId);
					p.setUserName(updatep.getUserName());
					p.setUserImg(updatep.getUserImg());
					p.setGender(updatep.getGender());
					p.setUpdateTime(System.currentTimeMillis());
					// TODO 更新mysql用户头像,姓名,性别
					UserMapper.updateNameImgGer(updatep);
				}
			} else {// 如果没有，需要去微信的用户里查询
				p = UserLogin.getUserInfoByOpenId(openId);
				if (p == null) {
					return null;
				} else {
					// p.setTotalGameNum("0");
					p.setMoney(Cnst.get_MONEY_INIT(cid));
					p.setLoginStatus(1);
					p.setCid(cid);
					String time = String.valueOf(new Date().getTime());
					p.setLastLoginTime(time);
					p.setSignUpTime(time);
					p.setUpdateTime(System.currentTimeMillis());
					p.setUserAgree(0);
					UserMapper.insert(p);
				}
			}
			p.setScore(0);
			p.setIp(ip);
			p.setNotice(notice);
			p.setState(Cnst.PLAYER_LINE_STATE_INLINE);
			p.setPlayStatus(Cnst.PLAYER_STATE_DATING);
			p.setMoney(p.getMoney() - getFrozenMoney(p.getUserId()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 更新用户ip 最后登陆时间
		// UserMapper.updateIpAndLastTime(openId, clientIp);
		return p;
	}

}
