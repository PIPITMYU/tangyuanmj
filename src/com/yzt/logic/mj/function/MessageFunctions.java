package com.yzt.logic.mj.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.yzt.logic.mj.domain.Action;
import com.yzt.logic.mj.domain.Player;
import com.yzt.logic.mj.domain.RoomResp;
import com.yzt.logic.util.Cnst;
import com.yzt.logic.util.GameUtil.StringUtils;
import com.yzt.logic.util.redis.RedisUtil;
import com.yzt.netty.client.WSClient;
import com.yzt.netty.util.MessageUtils;

/**
 * Created by Administrator on 2017/7/10. 推送消息类
 */
public class MessageFunctions extends TCPGameFunctions {

	/**
	 * 发送玩家信息 (断线重连)
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100100(WSClient channel,
			Map<String, Object> readData) throws Exception {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Map<String, Object> info = new HashMap<>();
		if (interfaceId.equals(100100)) {// 刚进入游戏主动请求
			String openId = String.valueOf(readData.get("openId"));
			Player currentPlayer = null;
			String cid = null;
			if (openId == null) {
				illegalRequest(interfaceId, channel);
				return;
			} else {
				String ip = String.valueOf(channel.getIp());
				cid = String.valueOf(readData.get("cId"));
				currentPlayer = HallFunctions.getPlayerInfos(openId, ip, cid,
						channel);// 断线重连后的数据
			}
			if (currentPlayer == null) {
				illegalRequest(interfaceId, channel);
				return;
			}

			// 更新心跳为最新上线时间
			if (cid != null) {
				currentPlayer.setCid(cid);
			}
			currentPlayer.setChannelId(channel.getId());// 更新channelId
			channel.setUserId(currentPlayer.getUserId());
			currentPlayer.setState(Cnst.PLAYER_LINE_STATE_INLINE);
			if (openId != null) {
				RedisUtil.setObject(
						Cnst.REDIS_PREFIX_OPENIDUSERMAP.concat(openId),
						currentPlayer.getUserId(), null);
			}

			RoomResp room = null;
			List<Player> players = null;

			if (currentPlayer.getRoomId() != null) {// 玩家下有roomId，证明在房间中
				room = RedisUtil.getRoomRespByRoomId(String
						.valueOf(currentPlayer.getRoomId()));
				if (room != null && room.getState() != Cnst.ROOM_STATE_YJS
						&& roomExit(room, currentPlayer)) {
					if (room.getState() == Cnst.ROOM_STATE_XJS) {
						currentPlayer.initPlayer(room.getRoomId(),
								Cnst.PLAYER_STATE_IN, currentPlayer.getScore());
					}
					info.put("roomInfo", getRoomInfo(room, currentPlayer));
					players = RedisUtil.getPlayerList(room);
					for (int m = 0; m < players.size(); m++) {
						Player p = players.get(m);
						if (p.getUserId().equals(currentPlayer.getUserId())) {
							players.remove(m);
							break;
						}
					}

					info.put("anotherUsers", getAnotherUserInfo(players, room));
					MessageFunctions.interface_100109(players,
							Cnst.PLAYER_LINE_STATE_INLINE,
							currentPlayer.getUserId());
					if (room.getRoomType() == Cnst.ROOM_TYPE_2
							&& !currentPlayer.getUserId().equals(
									room.getCreateId())) {
						MessageFunctions.interface_100112(currentPlayer, room,
								Cnst.PLAYER_EXTRATYPE_SHANGXIAN);
					}
				} else {
					// TODO
					currentPlayer.initPlayer(null, Cnst.PLAYER_STATE_DATING,
							null);
				}

			} else {
				// TODO
				currentPlayer.initPlayer(null, Cnst.PLAYER_STATE_DATING, null);
			}

			RedisUtil.updateRedisData(room, currentPlayer);
			info.put("currentUser", getCurrentUserInfo(currentPlayer, room));

			if (room != null) {
				info.put("wsw_sole_action_id", room.getWsw_sole_action_id());
				Map<String, Object> roomInfo = (Map<String, Object>) info
						.get("roomInfo");
				List<Map<String, Object>> anotherUsers = (List<Map<String, Object>>) info
						.get("anotherUsers");

				info.remove("roomInfo");
				info.remove("anotherUsers");

				JSONObject result = getJSONObj(interfaceId, 1, info);
				MessageUtils.sendMessage(channel, result.toJSONString());

				info.remove("currentUser");
				info.put("roomInfo", roomInfo);
				result = getJSONObj(interfaceId, 1, info);
				MessageUtils.sendMessage(channel, result.toJSONString());

				info.remove("roomInfo");
				info.put("anotherUsers", anotherUsers);
				result = getJSONObj(interfaceId, 1, info);
				MessageUtils.sendMessage(channel, result.toJSONString());

			} else {
				JSONObject result = getJSONObj(interfaceId, 1, info);
				MessageUtils.sendMessage(channel, result.toJSONString());
			}

		} else {
			channel.getChannelHandlerContext().close();
		}

	}

	private static boolean roomExit(RoomResp room, Player currentPlayer) {
		Long[] players = room.getPlayerIds();
		for (int i = 0; i < players.length; i++) {
			if (players[i] == null) {
				continue;
			}
			if (players[i].equals(currentPlayer.getUserId())) {
				return true;
			}
		}
		return false;
	}

	// 封装currentUser
	public static Map<String, Object> getCurrentUserInfo(Player player,
			RoomResp room) {
		Map<String, Object> currentUserInfo = new HashMap<String, Object>();
		currentUserInfo.put("userId", player.getUserId());
		currentUserInfo.put("position", player.getPosition());
		currentUserInfo.put("playStatus", player.getPlayStatus());
		currentUserInfo.put("userName", player.getUserName());
		currentUserInfo.put("userImg", player.getUserImg());
		currentUserInfo.put("gender", player.getGender());
		currentUserInfo.put("ip", player.getIp());
		currentUserInfo.put("userAgree", player.getUserAgree());
		currentUserInfo.put("money", player.getMoney());
		currentUserInfo.put("score", player.getScore());
		currentUserInfo.put("notice", player.getNotice());
		currentUserInfo.put("state", player.getState());
		if (room != null && player.getPlayStatus() == Cnst.PLAYER_STATE_GAME) {
			currentUserInfo.put("pais", player.getCurrentMjList());
			currentUserInfo.put("chuList", player.getChuList());
			if (player.getActionList() != null
					&& player.getActionList().size() > 0) {
				List<Object> actionList = new ArrayList<Object>();
				for (Action action : player.getActionList()) {
					if (action.getType() == Cnst.ACTION_TYPE_CHI) {
						Map<String, Integer> map = new HashMap<String, Integer>();
						map.put("action", action.getActionId());
						map.put("extra", action.getExtra());
						actionList.add(map);

					} else if (action.getType() == Cnst.ACTION_TYPE_ANGANG) {
						Map<String, Integer> map = new HashMap<String, Integer>();
						map.put("action", -2);
						map.put("extra", action.getActionId());
						actionList.add(map);
					} else {
						actionList.add(action.getActionId());
					}
				}
				currentUserInfo.put("actionList", actionList);
			}
			if (player.getActionList() != null
					&& player.getActionList().size() == 0) {
				List<Object> actionList = new ArrayList<Object>();
				currentUserInfo.put("actionList", actionList);
			}
		}

		return currentUserInfo;
	}

	// 封装anotherUsers
	public static List<Map<String, Object>> getAnotherUserInfo(
			List<Player> players, RoomResp room) {
		List<Map<String, Object>> anotherUserInfos = new ArrayList<Map<String, Object>>();
		for (Player player : players) {
			Map<String, Object> currentUserInfo = new HashMap<String, Object>();
			currentUserInfo.put("userId", player.getUserId());
			currentUserInfo.put("position", player.getPosition());
			currentUserInfo.put("playStatus", player.getPlayStatus());
			currentUserInfo.put("userName", player.getUserName());
			currentUserInfo.put("userImg", player.getUserImg());
			currentUserInfo.put("gender", player.getGender());
			currentUserInfo.put("ip", player.getIp());
			currentUserInfo.put("userAgree", player.getUserAgree());
			currentUserInfo.put("money", player.getMoney());
			currentUserInfo.put("score", player.getScore());
			WSClient wsClient = TCPGameFunctions.getWSClientManager()
					.getWSClient(player.getChannelId());
			if (wsClient == null) {
				currentUserInfo.put("state", 2);
			} else {
				currentUserInfo.put("state", 1);
			}
			if (room != null && room.getState() != Cnst.ROOM_STATE_XJS
					&& player.getPlayStatus() == Cnst.PLAYER_STATE_GAME) {
				currentUserInfo.put("pais", player.getCurrentMjList().size());
				currentUserInfo.put("chuList", player.getChuList());
				if (player.getActionList() != null
						&& player.getActionList().size() > 0) {
					List<Object> actionList = new ArrayList<Object>();
					for (Action action : player.getActionList()) {
						if (action.getType() == Cnst.ACTION_TYPE_CHI) {
							Map<String, Integer> map = new HashMap<String, Integer>();
							map.put("action", action.getActionId());
							map.put("extra", action.getExtra());
							actionList.add(map);

						} else if (action.getType() == Cnst.ACTION_TYPE_ANGANG) {
							Map<String, Integer> map = new HashMap<String, Integer>();
							map.put("action", -2);
							map.put("extra",action.getActionId());
							actionList.add(map);
						} else {
							actionList.add(action.getActionId());
						}
					}
					currentUserInfo.put("actionList", actionList);
				}
				if (player.getActionList() != null
						&& player.getActionList().size() == 0) {
					List<Object> actionList = new ArrayList<Object>();
					currentUserInfo.put("actionList", actionList);
				}
			}

			anotherUserInfos.add(currentUserInfo);
		}
		return anotherUserInfos;
	}

	// 封装房间信息
	public static Map<String, Object> getRoomInfo(RoomResp room, Player p) {
		Map<String, Object> roomInfo = new HashMap<String, Object>();
		roomInfo.put("userId", room.getCreateId());
		roomInfo.put("userName", room.getOpenName());
		roomInfo.put("createTime", room.getCreateTime());
		roomInfo.put("roomId", room.getRoomId());
		roomInfo.put("state", room.getState());
		roomInfo.put("lastNum", room.getLastNum());
		roomInfo.put("circleNum", room.getCircleNum());// 总局数
		roomInfo.put("roomType", room.getRoomType());
		roomInfo.put("xjst", room.getXjst());
		roomInfo.put("scoreType", room.getScoreType());
		roomInfo.put("shenFeng", room.getShenFeng());
		roomInfo.put("daiTe", room.getDaiTe());
		roomInfo.put("gangZhuang", room.getGangZhuang());
		roomInfo.put("gangFanBei", room.getGangFanBei());
		roomInfo.put("qiDui", room.getQiDui());
		roomInfo.put("diaoYu", room.getDiaoYu());
		roomInfo.put("startPosition", room.getStartPosition());
		if (room.getState() == Cnst.ROOM_STATE_GAMIING) {
			roomInfo.put("currMJNum", room.getCurrentMjList().size());
			roomInfo.put("zhuangPlayer", room.getZhuangId());
			if (room.getNextAction() != null
					&& room.getNextAction().size() > 0
					&& (room.getNextAction().contains(-1) || room
							.getNextAction().contains(501))) {
				roomInfo.put("currAction", room.getNextAction());
				roomInfo.put("currActionUserId", room.getNextActionUserId());
			} else {
				if (p.getUserId().equals(room.getNextActionUserId())) {
					roomInfo.put("currAction", room.getNextAction());
					roomInfo.put("currActionUserId", room.getNextActionUserId());
				}
			}
			roomInfo.put("lastFaUserId", room.getLastFaPaiUserId());
			roomInfo.put("lastChuUserId", room.getXiaoShuiDi());
			roomInfo.put("position", room.getWindPosition());
		}
		if (room.getDissolveRoom() != null) {
			Map<String, Object> dissolveRoom = new HashMap<String, Object>();
			dissolveRoom.put("dissolveTime", room.getDissolveRoom()
					.getDissolveTime());
			dissolveRoom.put("userId", room.getDissolveRoom().getUserId());
			dissolveRoom.put("othersAgree", room.getDissolveRoom()
					.getOthersAgree());
			roomInfo.put("dissolveRoom", dissolveRoom);
		}
		return roomInfo;
	}

	/**
	 * 小结算
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100102(WSClient channel,
			Map<String, Object> readData) {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomId"));
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId));
		List<Player> players = RedisUtil.getPlayerList(room);
		List<Map<String, Object>> userInfos = new ArrayList<Map<String, Object>>();
		for (Player p : players) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("userId", p.getUserId());
			map.put("yuFen", p.getYuFen());
			map.put("gangScore", p.getGangScore());
			map.put("huScore", p.getThisScore());
			map.put("pais", p.getCurrentMjList());
			if (p.getIsHu()) {
				map.put("isWin", 1);
				map.put("winInfo", p.getFanShu());
			} else {
				map.put("isWin", 0);
			}
			if (p.getIsDian()) {
				map.put("isDian", 1);
			} else {
				map.put("isDian", 0);
			}
			if (p.getActionList() != null && p.getActionList().size() > 0) {
				List<Object> actionList = new ArrayList<Object>();
				for (Action action : p.getActionList()) {
					if (action.getType() == Cnst.ACTION_TYPE_CHI) {
						Map<String, Integer> actionMap = new HashMap<String, Integer>();
						actionMap.put("action", action.getActionId());
						actionMap.put("extra", action.getExtra());
						actionList.add(actionMap);

					} else if (action.getType() == Cnst.ACTION_TYPE_ANGANG) {
						Map<String, Integer> actionMap = new HashMap<String, Integer>();
						actionMap.put("action", -2);
						actionMap.put("extra", action.getActionId());
						actionList.add(actionMap);
					} else {
						actionList.add(action.getActionId());
					}
				}
				map.put("actionList", actionList);
			}
			userInfos.add(map);
		}

		JSONObject info = new JSONObject();
		info.put("lastNum", room.getLastNum());
		info.put("userInfo", userInfos);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 大结算
	 * 
	 * @param session
	 * @param readData
	 */
	public synchronized static void interface_100103(WSClient channel,
			Map<String, Object> readData) {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomId"));
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId));
		String key = roomId + "-" + room.getCreateTime();
		List<Map> info = new ArrayList<Map>();
		if (!RedisUtil.exists(Cnst.REDIS_PLAY_RECORD_PREFIX_OVERINFO
				.concat(key))) {
			List<Player> players = RedisUtil.getPlayerList(room);
			for (Player p : players) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("userId", p.getUserId());
				map.put("score", 0);
				map.put("position", p.getPosition());
				map.put("userName", p.getUserName());
				map.put("userImg", p.getUserImg());
				info.add(map);
			}
		} else {
			info = RedisUtil
					.getPlayRecord(Cnst.REDIS_PLAY_RECORD_PREFIX_OVERINFO
							.concat(key));
			;
		}

		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());

		// 更新 player
		Player p = RedisUtil.getPlayerByUserId(String.valueOf(userId));
		// TODO
		p.initPlayer(null, Cnst.PLAYER_STATE_DATING, null);

		Integer outNum = room.getOutNum() == null ? 1 : room.getOutNum() + 1;
		if (outNum == room.getPlayerIds().length) {
			RedisUtil.deleteByKey(Cnst.REDIS_PREFIX_ROOMMAP.concat(String
					.valueOf(roomId)));
		} else {
			// 更新outNum
			room.setOutNum(outNum);
			RedisUtil.updateRedisData(room, p);
		}
	}

	/**
	 * 发牌 打牌 动作推送
	 * 
	 * @param room
	 * @param players
	 * @param info
	 * @param action
	 *            动作实体
	 */
	public static void interface_100104(RoomResp room, List<Player> players,
			Integer interfaceId, Action action) {
		for (Player p : players) {
			Map<String, Object> info = new HashMap<String, Object>();
			info.put("state", room.getState());
			info.put("userId", action.getUserId());
			if (action.getType() != Cnst.ACTION_TYPE_ANGANG) {
				if (action.getType() == Cnst.ACTION_TYPE_FAPAI) {
					if (action.getUserId().equals(p.getUserId())) {
						info.put("extra", action.getExtra());
					}
					info.put("action", action.getActionId());
				} else {
					info.put("extra", action.getExtra());
					info.put("toUserId", action.getToUserId());
					info.put("action", action.getActionId());
				}
			}
			if (action.getType() == Cnst.ACTION_TYPE_ANGANG) {
				info.put("action", -2);
				info.put("extra", action.getActionId());
			}
			if (room.getState() != Cnst.ROOM_STATE_XJS) {
				if (room.getNextAction().contains(501)
						|| room.getNextAction().contains(-1)) {
					info.put("nextAction", room.getNextAction());
					info.put("nextActionUserId", room.getNextActionUserId());
				} else {
					if (p.getUserId().equals(room.getNextActionUserId())) {
						info.put("nextAction", room.getNextAction());
						info.put("nextActionUserId", room.getNextActionUserId());
					}
				}
			}
			if (room.getDiaoYu() == 1 && room.getState() == Cnst.ROOM_STATE_XJS) {
				info.put("diaoYu", room.getYuPai());
			}
			info.put("wsw_sole_action_id", room.getWsw_sole_action_id());
			JSONObject result = getJSONObj(interfaceId, 1, info);
			WSClient ws = getWSClientManager().getWSClient(p.getChannelId());
			if (ws != null) {
				MessageUtils.sendMessage(ws, result.toJSONString());
			}
		}
	}

	/**
	 * 多地登陆提示
	 * 
	 * @param session
	 */
	public static void interface_100106(WSClient channel) {
		Integer interfaceId = 100106;
		JSONObject result = getJSONObj(interfaceId, 1,
				Cnst.PLAYER_LINE_STATE_OUT);
		MessageUtils.sendMessage(channel, result.toJSONString());
		channel.getChannelHandlerContext().close();
	}

	/**
	 * 玩家被踢/房间被解散提示
	 * 
	 * @param session
	 */
	public static void interface_100107(Long userId, Integer type,
			List<Player> players) {
		Integer interfaceId = 100107;
		Map<String, Object> info = new HashMap<String, Object>();

		if (players == null || players.size() == 0) {
			return;
		}
		info.put("userId", userId);
		info.put("type", type);

		JSONObject result = getJSONObj(interfaceId, 1, info);
		for (Player p : players) {
			WSClient ws = getWSClientManager().getWSClient(p.getChannelId());
			if (ws != null) {
				MessageUtils.sendMessage(ws, result.toJSONString());
			}
		}
	}

	/**
	 * 方法id不符合
	 * 
	 * @param session
	 */
	public static void interface_100108(WSClient channel) {
		Integer interfaceId = 100108;
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("reqState", Cnst.REQ_STATE_9);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 用户离线/上线提示
	 * 
	 * @param state
	 */
	public static void interface_100109(List<Player> players, Integer state,
			Long userId) {
		Integer interfaceId = 100109;
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("userId", userId);
		info.put("state", state);

		JSONObject result = getJSONObj(interfaceId, 1, info);

		if (players != null && players.size() > 0) {
			for (Player p : players) {
				if (p != null && !p.getUserId().equals(userId)) {
					WSClient ws = getWSClientManager().getWSClient(
							p.getChannelId());

					if (ws != null) {
						MessageUtils.sendMessage(ws, result.toJSONString());
					}
				}

			}
		}
	}

	/**
	 * 后端主动解散房间推送
	 * 
	 * @param reqState
	 * @param players
	 */
	public static void interface_100111(int reqState, List<Player> players,
			Integer roomId) {
		Integer interfaceId = 100111;
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("reqState", reqState);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		if (players != null && players.size() > 0) {
			for (Player p : players) {
				if (p.getRoomId() != null && p.getRoomId().equals(roomId)) {
					WSClient ws = getWSClientManager().getWSClient(
							p.getChannelId());
					if (ws != null) {
						MessageUtils.sendMessage(ws, result.toJSONString());
					}
				}
			}
		}

	}

	/**
	 * 后端主动加入代开房间推送
	 * 
	 * @param reqState
	 * @param players
	 */
	public static void interface_100112(Player player, RoomResp room,
			Integer type) {
		Integer interfaceId = 100112;
		// 先判断房主是否在线
		Player roomCreater = RedisUtil.getPlayerByUserId(String.valueOf(room
				.getCreateId()));
		WSClient ws = getWSClientManager().getWSClient(
				roomCreater.getChannelId());
		if (ws != null) {
			Map<String, Object> info = new HashMap<String, Object>();
			info.put("roomId", room.getRoomId());
			if (player != null) {
				info.put("userId", player.getUserId());
				info.put("userName", player.getUserName());
				info.put("userImg", player.getUserImg());
				info.put("position", player.getPosition());
			}
			info.put("extraType", type);
			JSONObject result = getJSONObj(interfaceId, 1, info);
			MessageUtils.sendMessage(ws, result.toJSONString());
		} else {
			return;
		}
	}

	/***
	 * 获取字段解析
	 * 
	 * @param wsClient
	 * @param readData
	 */
	public static void interface_100999(WSClient wsClient,
			Map<String, Object> readData) {
		// TODO Auto-generated method stub
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		JSONObject obj = new JSONObject();
		obj.put("interfaceId", interfaceId);
		obj.put("state", 1);
		obj.put("message", "");
		obj.put("info", Cnst.ROUTE_MAP_SEND);
		obj.put("others", "");
		MessageUtils.sendMessage(wsClient, obj.toJSONString());
	}
}
