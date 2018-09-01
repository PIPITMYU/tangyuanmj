package com.yzt.logic.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSONObject;
import com.yzt.logic.mj.domain.Action;
import com.yzt.logic.mj.domain.Player;
import com.yzt.logic.mj.domain.RoomResp;
import com.yzt.logic.mj.function.TCPGameFunctions;
import com.yzt.logic.util.redis.RedisUtil;

public class BackFileUtil {

	public static final Log logger = LogFactory.getLog(BackFileUtil.class);

	public static void save(Integer interfaceId, RoomResp room, List<Player> players, Object infos, Action action) {
		// 俱乐部房间 不写回放
//		if (String.valueOf(room.getRoomId()).length() > 6) {
//			return;
//		}
		Integer xjn = room.getXiaoJuNum() == null ? 1 : room.getXiaoJuNum();
		String redisKey = Cnst.REDIS_HUIFANG_MAP.concat(room.getRoomId() + "_" + room.getCreateTime() + "_" + xjn);
		Map<String, Object> map = new HashMap<String, Object>();
		switch (interfaceId) {
		case 100200:
			if (room.getState() == Cnst.ROOM_STATE_GAMIING) {
				Map<String, Object> info = new HashMap<String, Object>();
				List<Map<String, Object>> userInfo = new ArrayList<Map<String, Object>>();
				for (Player p : players) {
					Map<String, Object> users = new HashMap<String, Object>();
					users.put("userId", p.getUserId());
					users.put("gender", p.getGender());
					users.put("userName", p.getUserName());
					users.put("userImg", p.getUserImg());
					users.put("position", p.getPosition());
					users.put("score", p.getScore());
					users.put("playStatus", Cnst.PLAYER_STATE_GAME);
					users.put("pais", p.getCurrentMjList());
					userInfo.add(users);
				}
				map.put("interfaceId", interfaceId); 
				info.put("userInfo", userInfo);
				JSONObject roomInfo = new JSONObject();
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
				roomInfo.put("currAction", room.getNextAction());
				roomInfo.put("currActionUserId", room.getNextActionUserId());
				info.put("roomInfo", roomInfo);
				map.put("infos", info);
				RedisUtil.rpush(redisKey, Cnst.HUIFANG_LIFE_TIME, JSONObject.toJSONString(TCPGameFunctions.getNewMap(map)));
			}
			break;
		case 100201:
			map.put("interfaceId", 100104);
			JSONObject actions = new JSONObject();
			actions.put("action", action.getActionId());
			actions.put("userId", action.getUserId());
			actions.put("toUserId", action.getToUserId());
			actions.put("currAction", room.getNextAction());
			actions.put("currActionUserId", room.getNextActionUserId());
			actions.put("extra",action.getExtra());
			map.put("infos", actions);
			RedisUtil.rpush(redisKey, null, JSONObject.toJSONString(TCPGameFunctions.getNewMap(map)));
			break;

		case 100102:
			map.put("interfaceId", interfaceId);
			map.put("infos", infos);
			RedisUtil.rpush(redisKey, null, JSONObject.toJSONString(TCPGameFunctions.getNewMap(map)));
			break;
		case 100103:
			if (room.getDissolveRoom() != null) {
				map.put("interfaceId", interfaceId);
				map.put("infos", infos);
				RedisUtil.rpush(redisKey, null, JSONObject.toJSONString(TCPGameFunctions.getNewMap(map)));
			}
			break;
		}
	}

	public static boolean write(RoomResp room) {
		// 俱乐部房间 不写回放
//		if (String.valueOf(room.getRoomId()).length() > 6) {
//			return false;
//		}
		boolean result = true;
		FileWriter fw = null;
		BufferedWriter w = null;
		Integer xjn = room.getXiaoJuNum() == null ? 1 : room.getXiaoJuNum();
		String redisKey = Cnst.REDIS_HUIFANG_MAP.concat(room.getRoomId() + "_" + room.getCreateTime() + "_" + xjn);
		try {
			File parent = new File(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH));
			if (!parent.exists()) {
				parent.mkdirs();
			}
			if (!RedisUtil.exists(redisKey)) {
				return false;
			}
			Date d = new Date(Long.valueOf(room.getCreateTime()));
			String time_prefix = new SimpleDateFormat("yyyyMMddHHmmss").format(d);
			String fineName = new StringBuffer().append(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH)).append(time_prefix).append("-").append(room.getRoomId()).append("-").append(xjn).append(".txt").toString();
			File file = new File(fineName);
			if (!file.exists()) {// 说明是新开的小局
				file.createNewFile();
			} else {
				return false;
			}
			fw = new FileWriter(file, true);
			w = new BufferedWriter(fw);

			List<String> info = RedisUtil.lrange(redisKey, 0, -1);
			w.write("{\"state\":1,\"info\":");
			w.newLine();
			w.write(info.toString());
			w.newLine();
			w.write("}");
			w.flush();
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			try {
				if (w != null) {
					w.close();
				}
				if (fw != null) {
					fw.close();
				}
				// FIXME 删除redis记录
				RedisUtil.deleteByKey(redisKey);
			} catch (Exception e2) {

			}
		}

		return result;
	}

	/**
	 * 每次启动服务，清零所有回放文件
	 */
	public static void deleteAllRecord() {
		try {
			File path = new File(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH));
			File[] files = path.listFiles();

			if (files != null && files.length > 0) {
				for (int i = 0; i < files.length; i++) {
					File f = files[i];
					f.delete();
				}
			}
			path.delete();
			logger.info("回放文件清理完成！");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// 文件格式为yyyyMMddHHmmss-roomId-xiaoJuNum.txt
	public static void deletePlayRecord() {
		try {
			File path = new File(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH));
			File[] files = path.listFiles();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			long currentDate = new Date().getTime();
			if (files != null && files.length > 0) {
				for (int i = 0; i < files.length; i++) {
					File f = files[i];
					String name = f.getName();
					String dateStr = name.split("_")[0];
					Date createDate = sdf.parse(dateStr);
					if ((currentDate - createDate.getTime()) >= Cnst.BACKFILE_STORE_TIME) {
						f.delete();
					} else {
						break;
					}
				}
			}
			logger.info("回放文件清理完成！");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
