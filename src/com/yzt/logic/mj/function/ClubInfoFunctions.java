package com.yzt.logic.mj.function;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yzt.logic.mj.dao.ClubMapper;
import com.yzt.logic.mj.domain.ClubInfo;
import com.yzt.logic.mj.domain.ClubUser;
import com.yzt.logic.mj.domain.Player;
import com.yzt.logic.mj.domain.RoomResp;
import com.yzt.logic.util.Cnst;
import com.yzt.logic.util.CommonUtil;
import com.yzt.logic.util.RoomUtil;
import com.yzt.logic.util.GameUtil.StringUtils;
import com.yzt.logic.util.redis.RedisUtil;
import com.yzt.netty.client.WSClient;
import com.yzt.netty.util.MessageUtils;

/**
 * 俱乐部
 */

public class ClubInfoFunctions extends TCPGameFunctions {

	/**
	 * 扫描二维码查询俱乐部 "clubId":"俱乐部id", userId：玩家id
	 */
	public static void interface_500001(WSClient channel,
			Map<String, Object> readData) throws Exception {
		logger.info("查询俱乐部,interfaceId -> 500001");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		// Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer clubId = StringUtils.parseInt(readData.get("clubId"));
		Integer userId = StringUtils.parseInt(readData.get("userId"));

		Map<String, Object> info = new HashMap<>();
		// 通过clubId从redis中获取俱乐部信息
		ClubInfo redisClub = RedisUtil.getClubInfoByClubId(clubId.toString());
		if (null == redisClub) {// 如果为空 从数据库查询
			redisClub = ClubMapper.selectByClubId(StringUtils.parseInt(clubId));// 根据俱乐部id查询
			// 保存到redis
			RedisUtil.setClubInfoByClubId(clubId.toString(), redisClub);
		}
		if (redisClub == null) {
			info.put("reqState", Cnst.REQ_STATE_23);
		}

		// 根据俱乐部id和userid查询当前状态
		Integer exState = ClubMapper.selectUserState(
				StringUtils.parseInt(clubId), StringUtils.parseLong(userId));
		// 俱乐部页面刷新 此时管理员已同意退出
		if (exState == null) {
			info.put("reqState", Cnst.REQ_STATE_17);
		} else if (exState == 1 || exState == 2 || exState == 5) {// 在俱乐部里面
			info.put("reqState", Cnst.REQ_STATE_15);
		} else if (exState == 0) {
			info.put("reqState", Cnst.REQ_STATE_18);
		}
		if (null != redisClub) {
			info.put("clubName", redisClub.getClubName());
			info.put("clubUserName", ClubMapper.selectCreateName(StringUtils
					.parseInt(redisClub.getCreateId())));
			info.put("allNums", ClubMapper.allUsers(clubId));
		}
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 查询我加入的所有俱乐部
	 */
	public static void interface_500002(WSClient channel,
			Map<String, Object> readData) throws Exception {
		logger.info("查询我的俱乐部,interfaceId -> 500002");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));

		List<Map<String, Object>> listInfo = new ArrayList<Map<String, Object>>();
		List<ClubUser> list = ClubMapper.selectClubByUserId(userId);// 查询我加入的俱乐部信息
		if (list != null && list.size() > 0) {
			for (int a = 0; a < list.size(); a++) {
				Map<String, Object> info = new HashMap<>();
				// 通过clubId从redis中获取俱乐部信息
				Integer exState = ClubMapper.selectUserState(
						StringUtils.parseInt(list.get(a).getClubId()),
						StringUtils.parseLong(userId));
				ClubInfo redisClub = RedisUtil.getClubInfoByClubId(list.get(a)
						.getClubId().toString());
				if (null == redisClub) {// 如果为空 从数据库查询
					redisClub = ClubMapper.selectByClubId(list.get(a)
							.getClubId());// 根据俱乐部id查询
					// 保存到redis
					RedisUtil.setClubInfoByClubId(list.get(a).getClubId()
							.toString(), redisClub);
					
				}
				if (null != redisClub) {
					info.put("exState", exState);
					info.put("clubId", redisClub.getClubId());
					info.put("clubUserName", ClubMapper
							.selectCreateName(StringUtils.parseInt(redisClub
									.getCreateId())));
					info.put("clubName", redisClub.getClubName());
					// info.put("maxNums", redisClub.getPersonQuota());//--不用
					info.put("allNums",
							ClubMapper.allUsers(redisClub.getClubId()));
					// 限免时间
					info.put("freeStart", redisClub.getFreeStart());
					info.put("freeEnd", redisClub.getFreeEnd());
					listInfo.add(info);
				}
			}
		}
		JSONObject result = getJSONObj(interfaceId, 1, listInfo);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 申请加入俱乐部
	 */
	public static void interface_500000(WSClient channel,
			Map<String, Object> readData) throws Exception {
		logger.info("申请加入俱乐部,interfaceId -> 500000");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer clubId = StringUtils.parseInt(readData.get("clubId"));
		System.out.println("clubId" + clubId);
		Map<String, Object> info = new HashMap<>();
		ClubUser user = ClubMapper.selectUserByUserIdAndClubId(userId, clubId);
		if (null != user) {//
			info.put("reqState", Cnst.REQ_STATE_15);
		} else {
			Integer count = ClubMapper.countByUserId(StringUtils
					.parseLong(userId));// 查询我加入的俱乐部
			if (null != count && count >= 3) {// 如果加入的大于3个
				info.put("reqState", Cnst.REQ_STATE_16);
			} else {
				ClubUser clubUser = new ClubUser();
				clubUser.setUserId(StringUtils.parseLong(userId));
				clubUser.setClubId(StringUtils.parseInt(clubId));
				clubUser.setStatus(0);// 默认申请中
				clubUser.setCreateTime(new Date().getTime());// 申请时间
				ClubMapper.insert(clubUser);// 保存
				info.put("reqState", Cnst.REQ_STATE_17);
			}
		}

		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 申请离开俱乐部
	 */
	public static void interface_500007(WSClient channel,
			Map<String, Object> readData) throws Exception {
		logger.info("准备,interfaceId -> 500007");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer clubId = StringUtils.parseInt(readData.get("clubId"));

		Map<String, Object> info = new HashMap<>();
		// 根据用户id 和通过状态 查询
		ClubUser clubUser = ClubMapper.selectUserByUserIdAndClubId(userId,
				clubId);
		if (null != clubUser) {
			ClubInfo redisClub = RedisUtil.getClubInfoByClubId(clubId
					.toString());
			if (null == redisClub) {// 如果为空 从数据库查询
				redisClub = ClubMapper.selectByClubId(StringUtils
						.parseInt(clubId));// 根据俱乐部id查询
				// 保存到redis
				RedisUtil.setClubInfoByClubId(clubId.toString(), redisClub);
			}
			if (null != redisClub) {
				// 创建者不能离开俱乐部
				if (redisClub.getCreateId().equals(userId)) {
					return;
				}
			}
			// 说明不是创建者离开
			if (clubUser.getStatus() == 1) {
				info.put("reqState", Cnst.REQ_STATE_17);
				clubUser.setStatus(2);// 状态 状态 0申请加入 1已通过 2申请退出
				ClubMapper.updateById(clubUser);// 修改保存记录
			} else if (clubUser.getStatus() == 2) {
				info.put("reqState", Cnst.REQ_STATE_18);
			}
		}
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 查询俱乐部详情
	 */
	public static void interface_500003(WSClient channel,
			Map<String, Object> readData) throws Exception {
		logger.info("查询俱乐部详情,interfaceId -> 500003");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer clubId = StringUtils.parseInt(readData.get("clubId"));

		Map<String, Object> info = new HashMap<>();
		// 根据用户id 和通过状态 查询
		ClubInfo clubInfo = RedisUtil.getClubInfoByClubId(clubId.toString());
		if (null == clubInfo) {// 如果为空 从数据库查询
			clubInfo = ClubMapper.selectByClubId(clubId);// 根据俱乐部id查询
			// 保存到redis
			RedisUtil.setClubInfoByClubId(clubId.toString(), clubInfo);
		}
		if (null != clubInfo) {
			info.put("clubMoney", clubInfo.getRoomCardNum());
			info.put("cardQuota", clubInfo.getRoomCardQuota());
			// 限免时间
		}
		// 俱乐部不存在
		if (clubInfo == null) {
			JSONObject result = getJSONObj(interfaceId, 1, "");
			MessageUtils.sendMessage(channel, result.toJSONString());
			return;
		}
		// 根据俱乐部id和时间 查询消费房卡数
		Integer used = ClubMapper.sumMoneyByClubIdAndDate(clubId);
		info.put("used", used == null ? 0 : used);
		// 1:当天的活跃人数 -- clubId 为key+今天时间
		// Integer actNum =
		// ClubMapper.todayPerson(Integer.valueOf(clubId.toString())).size();
		Long actNum = RedisUtil.scard(clubId
				+ "_".concat(StringUtils.getTimesmorning() + ""));
		if (actNum == null || actNum == 0l) {
			actNum = 0l;
		} else {
			// 因为设置过期时间时多了条假数据，需要删除
			actNum = actNum - 1;
		}
		info.put("actNum", actNum == null ? 0 : actNum);

		// 走缓存
		Integer juNum = RedisUtil.getTodayJuNum(clubId
				+ "".concat(StringUtils.getTimesmorning() + ""));
		info.put("juNum", juNum == null ? 0 : juNum);

		// 通过该玩家id查找他所在的俱乐部
		List<Integer> clubIds = ClubMapper.selectClubIdsByUserId(userId);
		if (!clubIds.contains(clubId)) {
			JSONObject result = getJSONObj(interfaceId, 1, info);
			MessageUtils.sendMessage(channel, result.toJSONString());
			return;
		}

		/************************** 未开局的房间数 **********************************/

		JSONArray jsonArrayInfo = new JSONArray();

		Map<String, String> roomMap = RedisUtil
				.hgetAll(Cnst.REDIS_CLUB_ROOM_LIST.concat(String
						.valueOf(clubId)));
		if (roomMap.isEmpty()) {
			// 社么也不用做处理 似乎
		} else {
			for (String roomId : roomMap.keySet()) {
				RoomResp room = RedisUtil.getRoomRespByRoomId(roomId);
				if (room == null || room.getState() != Cnst.ROOM_STATE_CREATED) {
					// 房间已解散
					RedisUtil.hdel(Cnst.REDIS_CLUB_ROOM_LIST.concat(String
							.valueOf(clubId)), roomId);
				} else {
					JSONObject jsobj = new JSONObject();
					JSONObject roomobj = new JSONObject();
					jsobj.put("roomId", room.getRoomId());
					Long[] playerIds = room.getPlayerIds();
					List<JSONObject> players = new ArrayList<JSONObject>();
					Player player;
					int num = 0;
					for (Long uid : playerIds) {
						if (uid == null) {
							continue;
						}
						num++;
						JSONObject jb = new JSONObject();
						player = RedisUtil.getPlayerByUserId(String
								.valueOf(uid));
						// jb.put("userName", player.getUserName());
						jb.put("userImg", player.getUserImg());
						WSClient wsClient = TCPGameFunctions
								.getWSClientManager().getWSClient(
										player.getChannelId());
						if (wsClient == null) {
							jb.put("state", 2);
						} else {
							jb.put("state", 1);
						}
						players.add(jb);
					}
					jsobj.put("userInfo", players);
					// Player roomCreater =
					// RedisUtil.getPlayerByUserId(String.valueOf(room.getCreateId()));
					jsobj.put("allNums", num);// 当前加入房间人数
					// 开房选项
					jsobj.put("circleNum", room.getCircleNum());
					jsobj.put("scoreType", room.getScoreType());
					jsobj.put("shenFeng", room.getShenFeng());
					jsobj.put("daiTe", room.getDaiTe());
					jsobj.put("gangZhuang", room.getGangZhuang());
					jsobj.put("gangFanBei", room.getGangFanBei());
					jsobj.put("qiDui", room.getQiDui());
					jsobj.put("diaoYu", room.getDiaoYu());

					jsonArrayInfo.add(jsobj);
				}

			}
		}
		info.put("roomInfo", jsonArrayInfo);
		/************************** 未开局的房间数结束 **********************************/

		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());

	}

	/**
	 * 查询我的战绩
	 */
	public static void interface_500006(WSClient channel,
			Map<String, Object> readData) throws Exception {
		logger.info("查询我的战绩,interfaceId -> 500006");

		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer clubId = StringUtils.parseInt(readData.get("clubId"));
		Integer date = StringUtils.parseInt(readData.get("date"));// 1 今日 0 昨日
		Integer page = Integer.parseInt(String.valueOf(readData.get("page")));

		// 玩家今日局数
		Long timesmorning = StringUtils.getTimesmorning();

		// juNum = ClubMapper.userTodayGames(clubId, userId);
		Integer juNum = RedisUtil.getObject(
				Cnst.REDIS_CLUB_TODAYJUNUM_ROE_USER.concat(clubId + "_")
						.concat(userId + "").concat(timesmorning + ""),
				Integer.class);
		if (juNum == null || juNum == 0) {
			juNum = 0;
		}
		String redisDate = null;
		if (date == 1) {
			// 今天
			redisDate = StringUtils.toString(StringUtils.getTimesmorning());
		} else {
			redisDate = StringUtils.toString(StringUtils.getYesMoring());
		}
		// 根据俱乐部id，人员id和时间查询 其参与的 战绩 分页
		String userKey = Cnst.REDIS_CLUB_PLAY_RECORD_PREFIX_ROE_USER + clubId
				+ "_" + userId + "_" + redisDate;

		// Long pageSize = RedisUtil.llen(userKey)*100;
		Long pageSize = RedisUtil.llen(userKey);
		int start = (page - 1) * Cnst.PAGE_SIZE;
		int end = start + Cnst.PAGE_SIZE - 1;
		List<String> keys = RedisUtil.lrange(userKey, start, end);
		JSONObject info = new JSONObject();
		List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
		for (String roomKey : keys) {
			// for (int i=0;i<100;i++) {
			Map<String, String> roomInfos = RedisUtil
					.hgetAll(Cnst.REDIS_CLUB_PLAY_RECORD_PREFIX.concat(roomKey));
			maps.add(roomInfos);
			// }

		}
		info.put("juNum", juNum);
		info.put("infos", maps);
		info.put("pages", pageSize == null ? 0
				: pageSize % Cnst.PAGE_SIZE == 0 ? pageSize / Cnst.PAGE_SIZE
						: (pageSize / Cnst.PAGE_SIZE + 1));
		// 玩家每天的分数
		String key2 = Cnst.REDIS_CLUB_TODAYSCORE_ROE_USER + clubId + "_"
				+ userId + "_" + StringUtils.getTimesmorning();
		if (RedisUtil.exists(key2)) {
			Integer score = RedisUtil.getObject(key2, Integer.class);
			info.put("score", score);
		} else {
			info.put("score", 0);
		}
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 俱乐部创建房间
	 */
	public synchronized static void interface_500004(WSClient channel,
			Map<String, Object> readData) {
		logger.info("创建房间,interfaceId -> 500004");

		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));// 接口Id
		Long userId = StringUtils.parseLong(readData.get("userId"));// 用户id
		Integer circleNum = StringUtils.parseInt(readData.get("circleNum"));// 圈数
																			// 2(默认)/4/8
		Integer roomType = 1;
		Integer scoreType = StringUtils.parseInt(readData.get("scoreType"));// 计分方式：1点炮三家付；2点炮包三家
		Integer shenFeng = StringUtils.parseInt(readData.get("shenFeng"));// 玩法选项
		Integer daiTe = StringUtils.parseInt(readData.get("daiTe"));// 玩法选项
		Integer qiDui = StringUtils.parseInt(readData.get("qiDui"));// 玩法选项
		Integer gangZhuang = StringUtils.parseInt(readData.get("gangZhuang"));// 玩法选项
		Integer gangFanBei = StringUtils.parseInt(readData.get("gangFanBei"));// 玩法选项
		Integer diaoYu = StringUtils.parseInt(readData.get("diaoYu"));// 玩法选项
		Integer clubId = StringUtils.parseInt(readData.get("clubId"));// 玩法选项
		Player p = RedisUtil.getPlayerByUserId(String.valueOf(userId));

		RoomResp room = new RoomResp();
		Integer needMoney = Cnst.moneyMap.get(circleNum);
		Map<String, String> roomMap = RedisUtil
				.hgetAll(Cnst.REDIS_CLUB_ROOM_LIST.concat(String
						.valueOf(clubId)));
		if (roomMap.isEmpty()) {
			// 似乎也不用做处理
		} else {
			if (roomMap.keySet().size() >= 5) {
				JSONObject error = new JSONObject();
				error.put("reqState", Cnst.REQ_STATE_20);
				JSONObject result = getJSONObj(interfaceId, 1, error);
				MessageUtils.sendMessage(channel, result.toJSONString());
				return;
			}
		}

		// 通过clubId从redis中获取俱乐部信息
		ClubInfo redisClub = RedisUtil.getClubInfoByClubId(clubId.toString());
		if (redisClub == null) {
			redisClub = ClubMapper.selectByClubId(StringUtils.parseInt(clubId));// 根据俱乐部id查询
		}
		// 俱乐部房卡不足
		if (redisClub.getRoomCardNum() <= 0) {
			JSONObject error = new JSONObject();
			error.put("reqState", Cnst.REQ_STATE_21);
			JSONObject result = getJSONObj(interfaceId, 1, error);
			MessageUtils.sendMessage(channel, result.toJSONString());
			return;
		}
		// 超过玩家每日限额
		int max = redisClub.getRoomCardQuota();
		// 获取当前玩家一天的房卡数
		Integer todayUse = ClubMapper.todayUse(clubId,
				StringUtils.parseInt(userId));

		// 房卡不能超过限制(此处需要根据人数进行修改)
		if ((todayUse + needMoney / 4) > max) {
			JSONObject error = new JSONObject();
			error.put("reqState", Cnst.REQ_STATE_22);
			JSONObject result = getJSONObj(interfaceId, 1, error);
			MessageUtils.sendMessage(channel, result.toJSONString());
			return;
		}

		Long freeStart = redisClub.getFreeStart();
		Long freeEnd = redisClub.getFreeEnd();
		long currentTimeMillis = System.currentTimeMillis();
		Boolean isFree = false;
		if (currentTimeMillis >= freeStart && currentTimeMillis <= freeEnd) {// 限免时间满足
			// 不用做判断
			isFree = true;
		}
		if (!isFree) {// 如果不是限免时间。
			if (redisClub.getRoomCardNum() < needMoney) {// 俱乐部房卡不足
				playerMoneyNotEnough(interfaceId, channel,
						StringUtils.parseInt(roomType));
				return;
			}
		}

		if (p.getRoomId() != null) {// 已存在其他房间
			playerExistOtherRoom(interfaceId, channel);
			return;
		}
		String createTime = String.valueOf(new Date().getTime());
		room.setCreateId(userId);// 创建人
		room.setState(Cnst.ROOM_STATE_CREATED);// 房间状态为等待玩家入坐
		room.setCircleNum(circleNum);// 总局数
		room.setLastNum(circleNum);// 剩余局数
		room.setCircleWind(Cnst.WIND_EAST);// 圈风为东风
		room.setRoomType(1);// 房间类型：房主模式
		room.setCreateTime(createTime);// 创建时间，long型数据
		room.setOpenName(p.getUserName());
		room.setScoreType(scoreType);
		// 房间规则
		room.setShenFeng(shenFeng);
		room.setDaiTe(daiTe);
		room.setQiDui(qiDui);
		room.setGangZhuang(gangZhuang);
		room.setGangFanBei(gangFanBei);
		room.setDiaoYu(diaoYu);
		room.setStartPosition(null);
		// 俱乐部的需要给房间加入俱乐部id属性
		room.setClubId(clubId);
		room.setWsw_sole_action_id(1);
		// 初始化大接口的id

		// toEdit 需要去数据库匹配，查看房间号是否存在，如果存在，则重新生成
		while (true) {
			room.setRoomId(CommonUtil.getGivenRamdonNum(7));// 设置随机房间密码
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
			p.initPlayer(p.getRoomId(), Cnst.PLAYER_STATE_IN, 0);
			userIds[p.getPosition() - 1] = p.getUserId();
			info.put("reqState", Cnst.REQ_STATE_1);
			// info.put("playerNum", 1);
			// userInfos.put("playerNum", 1);
			// userInfos.put("money", p.getMoney());
			// userInfos.put("playStatus",
			// String.valueOf(Cnst.PLAYER_STATE_IN));
			userInfos.put("position", p.getPosition());
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
		MessageUtils.sendMessage(channel, result.toJSONString());
		// 扣除俱乐部房卡保存到redis
		if (!isFree) {// 如果不是限免时间。
			redisClub.setRoomCardNum(redisClub.getRoomCardNum() - needMoney);
			RedisUtil.setClubInfoByClubId(clubId.toString(), redisClub);
		}
		// 更新redis数据 player roomMap club
		RedisUtil.updateRedisData(null, p);
		RedisUtil.setObject(Cnst.REDIS_PREFIX_ROOMMAP.concat(String
				.valueOf(room.getRoomId())), room, Cnst.ROOM_LIFE_TIME_CREAT);
		RedisUtil.setObject(
				Cnst.REDIS_CLUB_CLUBMAP.concat(String.valueOf(clubId)),
				redisClub, null);
		// 解散房间命令
		RoomUtil.addFreeRoomTask(StringUtils.parseLong(room.getRoomId()),
				System.currentTimeMillis() + Cnst.ROOM_CREATE_DIS_TIME);

		RedisUtil.hset(
				Cnst.REDIS_CLUB_ROOM_LIST.concat(String.valueOf(clubId)),
				String.valueOf(room.getRoomId()), "1",
				Cnst.ROOM_LIFE_TIME_CREAT);
	}

	/**
	 * 加入房间
	 * 
	 * @param session
	 * @param readData
	 */
	public synchronized static void interface_500005(WSClient channel,
			Map<String, Object> readData) throws Exception {
		logger.info("加入房间,interfaceId -> 500005");
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
		// 通过该玩家id查找他所在的俱乐部
		List<Integer> clubIds = ClubMapper.selectClubIdsByUserId(userId);
		if (!clubIds.contains(room.getClubId())) {
			JSONObject error = new JSONObject();
			error.put("message", "你不在此俱乐部内,无权加入");
			JSONObject result = getJSONObj(interfaceId, 0, error);
			MessageUtils.sendMessage(channel, result.toJSONString());
			return;

		}
		Integer needMoney = Cnst.moneyMap.get(room.getCircleNum());
		Integer clubId = room.getClubId();
		ClubInfo redisClub = RedisUtil.getClubInfoByClubId(clubId.toString());
		// 超过每日限额
		int max = redisClub.getRoomCardQuota();
		// 获取当前玩家一天的房卡数
		Integer todayUse = ClubMapper.todayUse(clubId,
				StringUtils.parseInt(userId));

		// 房卡不能超过限制(此处需要根据人数进行修改)
		if ((todayUse + needMoney / 4) > max) {
			JSONObject error = new JSONObject();
			error.put("reqState", Cnst.REQ_STATE_22);
			JSONObject result = getJSONObj(interfaceId, 1, error);
			MessageUtils.sendMessage(channel, result.toJSONString());
			return;
		}
		// 房间人满
		Long[] userIds = room.getPlayerIds();
		boolean hasNull = false;
		for (Long uId : userIds) {
			if (uId == null) {
				hasNull = true;
				break;
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

		// 设置用户信息
		p.setRoomId(roomId);
		p.setPosition(getWind(userIds));
		if (p.getPosition().equals(Cnst.WIND_EAST)) {
			room.setZhuangId(userId);
		}

		userIds[p.getPosition() - 1] = p.getUserId();
		p.initPlayer(p.getRoomId(), Cnst.PLAYER_STATE_IN, 0);

		Map<String, Object> info = new HashMap<>();
		info.put("reqState", Cnst.REQ_STATE_1);
		info.put("reqState", Cnst.REQ_STATE_1);
		info.put("userId", room.getCreateId());
		info.put("roomId", roomId);
		info.put("circleNum", room.getCircleNum());
		info.put("roomType", room.getRoomType());
		info.put("scoreType", room.getScoreType());
		// 规则
		info.put("shenFeng", room.getShenFeng());
		info.put("daiTe", room.getDaiTe());
		info.put("gangZhuang", room.getGangZhuang());
		info.put("gangFanBei", room.getGangFanBei());
		info.put("qiDui", room.getQiDui());
		info.put("diaoYu", room.getDiaoYu());
		// info.put("roomInfo", MessageFunctions.getRoomInfo(room,null));
		JSONArray allUserInfos = new JSONArray();
		for (Long ids : userIds) {
			if (ids == null) {
				continue;
			}
			if (ids.equals(userId)) {
				JSONObject infos = TCPGameFunctions.getUserInfoJSON(p);// 当前用户的信息
				allUserInfos.add(infos);
				continue;
			}
			Player player = RedisUtil.getPlayerByUserId(String.valueOf(ids));
			allUserInfos.add(TCPGameFunctions.getUserInfoJSON(player));
		}

		info.put("userInfo", allUserInfos);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());

		// 更新redis数据
		RedisUtil.updateRedisData(room, p);

		// 通知另外几个人
		JSONObject userInfos = TCPGameFunctions.getUserInfoJSON(p);
		userInfos.remove("state");
		for (Long ids : userIds) {
			if (ids == null) {
				continue;
			}
			if (ids.equals(userId)) {
				continue;
			}
			Player pp = RedisUtil.getPlayerByUserId(String.valueOf(ids));
			WSClient ws = getWSClientManager().getWSClient(pp.getChannelId());
			if (ws != null) {
				JSONObject result1 = getJSONObj(interfaceId, 1, userInfos);
				MessageUtils.sendMessage(ws, result1.toJSONString());
			}
		}

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
}
