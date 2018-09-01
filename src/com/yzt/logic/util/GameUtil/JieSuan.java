package com.yzt.logic.util.GameUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.yzt.logic.mj.domain.Action;
import com.yzt.logic.mj.domain.Player;
import com.yzt.logic.mj.domain.RoomResp;
import com.yzt.logic.util.BackFileUtil;
import com.yzt.logic.util.Cnst;
import com.yzt.logic.util.MahjongUtils;
import com.yzt.logic.util.RoomUtil;
import com.yzt.logic.util.redis.RedisUtil;

/**
 * 玩家分的统计
 * 
 * @author wsw_007
 *
 */
public class JieSuan {
	public static void xiaoJieSuan(String roomId) {
		RoomResp room = RedisUtil.getRoomRespByRoomId(roomId);
		List<Player> players = RedisUtil.getPlayerList(room);
		// 需要做以下统计
		// 以及大结算校验 这里会写小结算文件 并对房间进行初始化
		boolean ziMo = false;// 赢家是否自摸
		for (Player other : players) {
			if (other.getIsZiMo()) {
				ziMo = true;
			}
		}
		// 杠分单算,先取到每隔玩家的杠分.
		for (Player player : players) {
			List<Action> actionList = player.getActionList();
			if (actionList != null && actionList.size() != 0) {
				for (Action action : actionList) {
					if (action.getType() == Cnst.ACTION_TYPE_DIANGANG
							|| action.getType() == Cnst.ACTION_TYPE_PENGGANG) { // 明杠1分
						changeGangFen(action, players, player, room, 1);
					} else if (action.getType() == Cnst.ACTION_TYPE_ANGANG) { // 暗杠2分
						changeGangFen(action, players, player, room, 2);
					}
				}
			}
		}

		// FIXME
		// 统计玩家各项数据 庄次数 胡的次数 特殊胡的次数 自摸次数 点炮次数 胡牌类型 具体番数 各个分数统计
		if (room.getHuangZhuang() != null && room.getHuangZhuang() == true) {
			// 荒庄不荒杠
			for (Player p : players) {
				p.setScore(p.getScore() + p.getGangScore());
			}
		} else { // 正常结算
			int yuFen = 0;
			if (room.getDiaoYu() == 1) {
				yuFen = room.getYuPai() == 32 ? 10
						: room.getYuPai() % 9 == 0 ? 9 : room.getYuPai() % 9;
			}
			// 天胡 地胡
			if (room.getLastAction() == null) {
				TianHu(players, yuFen);
			} else if (room.getCurrentMjList().size() == 59) {
				DiHu(players, room, yuFen);
			} else {
				int fen = MahjongUtils.checkHuFenInfo(players, room); // 检查胡牌玩家的分数
				// 计分方式：1,大包 2 出冲包三家 3陪冲 4不出冲
				if (room.getScoreType() == Cnst.BAOSANJIA) { // 包三家,但是不包杠分.
					if (room.getWinPlayerId().equals(room.getZhuangId())) {
						if (ziMo) {
							// 包三家庄自摸
							BaoSanJiaZhuangZiMo(players, fen, yuFen);
						} else {
							// 包三家庄被点
							BaoSanJiaZhuangBeiDian(players, fen, yuFen);
						}
					} else {
						if (ziMo) {
							// 包三家闲自摸
							BaoSanJiaXianZiMo(room, players, fen, yuFen);
						} else {
							// 点炮 庄点 普通点
							boolean zhuangDian = false;
							for (Player p : players) {
								if (p.getIsDian()
										&& p.getUserId().equals(
												room.getZhuangId())) {
									zhuangDian = true;
								}
							}
							if (zhuangDian) {
								// 包三家庄点闲
								BaoSanJiaZhuangDianXian(players, fen, yuFen);
							} else {
								// 包三家闲点闲
								BaoSanJiaXianDianXian(players, fen, yuFen);
							}
						}
					}
				} else if (room.getScoreType() == Cnst.SANJIAFU) { // 可以自摸,各付各的
					if (room.getWinPlayerId().equals(room.getZhuangId())) {
						if (ziMo) {
							// 三家付庄自摸
							SanJiaFuZhuangZiMo(players, fen, yuFen);
						} else {
							// 三家付庄被点
							SanJiaFuZhuangBeiDian(players, fen, yuFen);
						}
					} else {
						if (ziMo) {
							// 三家付闲自摸
							SanJiaFuXianZiMo(room, players, fen, yuFen);
						} else {
							boolean zhuangDian = false;
							for (Player p : players) {
								if (p.getIsDian()
										&& p.getUserId().equals(
												room.getZhuangId())) {
									zhuangDian = true;
								}
							}
							if (zhuangDian) {
								// 三家付庄点闲
								SanJiaFuZhuangDianXian(players, fen, yuFen);
							} else {
								SanJiaFuXianDianXian(room, players, fen, yuFen);
							}
						}

					}
				}
			}

			if (room.getWinPlayerId().equals(room.getZhuangId())) {
				// 庄不变
			} else {
				// 下个人坐庄
				int index = -1;
				Long[] playIds = room.getPlayerIds();
				for (int i = 0; i < playIds.length; i++) {
					if (playIds[i].equals(room.getZhuangId())) {
						index = i + 1;
						if (index == 4) {
							index = 0;
						}
						break;
					}
				}
				room.setZhuangId(playIds[index]);
				room.setCircleWind(index + 1);

				// 不是第一局,并且圈风是东风 ,证明是下一圈了.
				if (room.getXiaoJuNum() != 1
						&& room.getCircleWind() == Cnst.WIND_EAST) {
					room.setTotolCircleNum(room.getTotolCircleNum() == null ? 1
							: room.getTotolCircleNum() + 1);
					room.setLastNum(room.getCircleNum()
							- room.getTotolCircleNum());
				}
			}
		}

		// 更新redis
		RedisUtil.setPlayersList(players);

		// 添加小结算信息
		List<Integer> xiaoJS = new ArrayList<Integer>();
		for (Player p : players) {
			xiaoJS.add(p.getThisScore() + p.getGangScore() + p.getYuFen());
		}
		room.addXiaoJuInfo(xiaoJS);
		// 初始化房间
		room.initRoom();
		RedisUtil.updateRedisData(room, null);
		// 写入文件
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
		BackFileUtil.save(100102, room, null, info, null);
		// 小结算 存入一次回放
		BackFileUtil.write(room);

		// 大结算判定 (玩的圈数等于选择的圈数)
		if (room.getTotolCircleNum() == room.getCircleNum()) {
			// 最后一局 大结算
			room = RedisUtil.getRoomRespByRoomId(roomId);
			room.setState(Cnst.ROOM_STATE_YJS);
			RedisUtil.updateRedisData(room, null);
			// 这里更新数据库吧
			RoomUtil.updateDatabasePlayRecord(room);
		}
	}

	private static void TianHu(List<Player> players, Integer yuFen) {
		Player currPlayer = null;
		List<Integer> winInfo = new ArrayList<Integer>();
		for (Player player : players) {
			if (player.getIsHu()) {
				player.setHuNum(player.getHuNum() + 1);
				currPlayer = player;
			}
		}
		winInfo.add(Cnst.TIANHU);
		currPlayer.setFanShu(winInfo);
		for (Player p : players) {
			if (p.getIsHu()) {
				p.setThisScore(16 * 3);
				p.setYuFen(3 * yuFen);
				p.setScore(p.getScore()+p.getThisScore() + p.getGangScore() + p.getYuFen());
			} else {
				p.setThisScore(-16);
				p.setYuFen(-yuFen);
				p.setScore(p.getScore()+p.getThisScore() + p.getGangScore() + p.getYuFen());
			}
		}
	}

	private static void DiHu(List<Player> players, RoomResp room, Integer yuFen) {
		Player currPlayer = null;
		List<Integer> winInfo = new ArrayList<Integer>();
		for (Player player : players) {
			if (player.getIsHu()) {
				player.setHuNum(player.getHuNum() + 1);
				currPlayer = player;
			}
		}
		winInfo.add(Cnst.DIHU);
		currPlayer.setFanShu(winInfo);
		if (room.getScoreType() == Cnst.SANJIAFU) {
			for (Player p : players) {
				if (p.getIsHu()) {
					p.setThisScore(9 + 4 + 4);
					p.setYuFen(3 * yuFen);
					p.setScore(p.getScore()+p.getThisScore() + p.getGangScore()
							+ p.getYuFen());
				} else {
					if (p.getIsDian()) {
						p.setThisScore(-9);
					} else {
						p.setThisScore(-4);
					}
					p.setYuFen(-yuFen);
					p.setScore(p.getScore()+p.getThisScore() + p.getGangScore()
							+ p.getYuFen());
				}
			}
		} else {
			for (Player p : players) {
				if (p.getIsHu()) {
					p.setThisScore(9 + 4 + 4);
					p.setYuFen(3 * yuFen);
					p.setScore(p.getScore()+p.getThisScore() + p.getGangScore()
							+ p.getYuFen());
				} else {
					if (p.getIsDian()) {
						p.setThisScore(-9 - 4 - 4);
						p.setYuFen(-3 * yuFen);
						p.setScore(p.getScore()+p.getThisScore() + p.getGangScore()
								+ p.getYuFen());
					} else {
						p.setThisScore(0);
						p.setYuFen(0);
						p.setScore(p.getScore()+p.getThisScore() + p.getGangScore()
								+ p.getYuFen());
					}

				}
			}
		}
	}

	private static void SanJiaFuXianDianXian(RoomResp room,
			List<Player> players, int fen, int yuFen) {
		for (Player p : players) {
			if (p.getIsHu()) {
				p.setThisScore(fen * 2 + fen * 2 + fen);
				p.setYuFen(3 * yuFen);
				p.setScore(p.getScore()+p.getGangScore() + p.getThisScore() + p.getYuFen());
			} else {
				if (p.getIsDian()) {
					p.setThisScore(-fen * 2);
					p.setYuFen(-yuFen);
					p.setScore(p.getScore()+p.getGangScore() + p.getThisScore()
							+ p.getYuFen());
				} else {
					if (p.getUserId().equals(room.getZhuangId())) {
						p.setThisScore(-fen * 2);
						p.setYuFen(-yuFen);
						p.setScore(p.getScore()+p.getGangScore() + p.getThisScore()
								+ p.getYuFen());
					} else {
						p.setThisScore(-fen);
						p.setYuFen(-yuFen);
						p.setScore(p.getScore()+p.getGangScore() + p.getThisScore()
								+ p.getYuFen());
					}

				}
			}
		}
	}

	private static void SanJiaFuZhuangDianXian(List<Player> players, int fen,
			int yuFen) {
		for (Player p : players) {
			if (p.getIsHu()) {
				p.setThisScore(fen * 2 + fen * 2 * 2);
				p.setYuFen(3 * yuFen);
				p.setScore(p.getScore()+p.getGangScore() + p.getThisScore() + p.getYuFen());
			} else {
				if (p.getIsDian()) {
					p.setThisScore(-fen * 2 * 2);
					p.setYuFen(-yuFen);
					p.setScore(p.getScore()+p.getGangScore() + p.getThisScore()
							+ p.getYuFen());
				} else {
					p.setThisScore(-fen);
					p.setYuFen(-yuFen);
					p.setScore(p.getScore()+p.getGangScore() + p.getThisScore()
							+ p.getYuFen());
				}
			}
		}
	}

	private static void SanJiaFuZhuangBeiDian(List<Player> players, int fen,
			int yuFen) {
		for (Player p : players) {
			if (p.getIsHu()) {
				p.setThisScore(2 * 2 * fen + 2 * fen + 2 * fen);
				p.setYuFen(3 * yuFen);
				p.setScore(p.getScore()+p.getThisScore() + p.getGangScore() + p.getYuFen());
			} else {
				if (p.getIsDian()) {
					p.setThisScore(-2 * 2 * fen);
					p.setYuFen(-yuFen);
					p.setScore(p.getScore()+p.getThisScore() + p.getGangScore()
							+ p.getYuFen());
				} else {
					p.setThisScore(-2 * fen);
					p.setYuFen(-yuFen);
					p.setScore(p.getScore()+p.getThisScore() + p.getGangScore()
							+ p.getYuFen());
				}
			}
		}
	}

	private static void BaoSanJiaXianDianXian(List<Player> players, int fen,
			int yuFen) {
		for (Player p : players) {
			if (p.getIsHu()) {
				p.setThisScore(fen * 2 + fen * 2 + fen);
				p.setYuFen(3 * yuFen);
				p.setScore(p.getScore()+p.getGangScore() + p.getThisScore() + p.getYuFen());
			} else {
				if (p.getIsDian()) {
					p.setThisScore(-fen * 2 - fen * 2 - fen);
					p.setYuFen(-3 * yuFen);
					p.setScore(p.getScore()+p.getGangScore() + p.getThisScore()
							+ p.getYuFen());
				} else {
					p.setThisScore(0);
					p.setYuFen(0);
					p.setScore(p.getScore()+p.getGangScore() + p.getThisScore()
							+ p.getYuFen());
				}
			}
		}
	}

	private static void BaoSanJiaZhuangDianXian(List<Player> players, int fen,
			int yuFen) {
		for (Player p : players) {
			if (p.getIsHu()) {
				p.setThisScore(fen * 2 + fen * 2 * 2);
				p.setYuFen(3 * yuFen);
				p.setScore(p.getScore()+p.getGangScore() + p.getThisScore() + p.getYuFen());
			} else {
				if (p.getIsDian()) {
					p.setThisScore(-fen * 2 - fen * 2 * 2);
					p.setYuFen(-3 * yuFen);
					p.setScore(p.getScore()+p.getGangScore() + p.getThisScore()
							+ p.getYuFen());
				} else {
					p.setThisScore(0);
					p.setYuFen(0);
					p.setScore(p.getScore()+p.getGangScore() + p.getThisScore()
							+ p.getYuFen());
				}
			}
		}
	}

	private static void BaoSanJiaXianZiMo(RoomResp room, List<Player> players,
			int fen, int yuFen) {
		for (Player p : players) {
			if (p.getIsHu()) {
				p.setThisScore(4 * fen);
				p.setYuFen(3 * yuFen);
				p.setScore(p.getScore()+p.getThisScore() + p.getGangScore() + p.getYuFen());
			} else {
				if (p.getUserId().equals(room.getZhuangId())) {
					p.setThisScore(-2 * fen);
					p.setYuFen(-yuFen);
					p.setScore(p.getScore()+p.getThisScore() + p.getGangScore()
							+ p.getYuFen());
				} else {
					p.setThisScore(-fen);
					p.setYuFen(-yuFen);
					p.setScore(p.getScore()+p.getThisScore() + p.getGangScore()
							+ p.getYuFen());
				}
			}
		}
	}

	private static void SanJiaFuXianZiMo(RoomResp room, List<Player> players,
			int fen, int yuFen) {
		for (Player p : players) {
			if (p.getIsHu()) {
				p.setThisScore(4 * fen);
				p.setYuFen(3 * yuFen);
				p.setScore(p.getScore()+p.getThisScore() + p.getGangScore() + p.getYuFen());
			} else {
				if (p.getUserId().equals(room.getZhuangId())) {
					p.setThisScore(-2 * fen);
					p.setYuFen(-yuFen);
					p.setScore(p.getScore()+p.getThisScore() + p.getGangScore()
							+ p.getYuFen());
				} else {
					p.setThisScore(-fen);
					p.setYuFen(-yuFen);
					p.setScore(p.getScore()+p.getThisScore() + p.getGangScore()
							+ p.getYuFen());
				}
			}
		}
	}

	private static void BaoSanJiaZhuangBeiDian(List<Player> players, int fen,
			int yuFen) {
		for (Player p : players) {
			if (p.getIsHu()) {
				p.setThisScore(2 * 2 * fen + 2 * 2 * fen);
				p.setYuFen(3 * yuFen);
				p.setScore(p.getScore()+p.getThisScore() + p.getGangScore() + p.getYuFen());
			} else {
				if (p.getIsDian()) {
					p.setThisScore(-8 * fen);
					p.setYuFen(-3 * yuFen);
					p.setScore(p.getScore()+p.getThisScore() + p.getGangScore()
							+ p.getYuFen());
				} else {
					p.setThisScore(0);
					p.setYuFen(0);
					p.setScore(p.getScore()+p.getThisScore() + p.getGangScore()
							+ p.getYuFen());
				}
			}
		}
	}

	private static void BaoSanJiaZhuangZiMo(List<Player> players, int fen,
			int yuFen) {
		for (Player p : players) {
			if (p.getIsHu()) {
				p.setThisScore(3 * 2 * fen);
				p.setYuFen(3 * yuFen);
				p.setScore(p.getScore()+p.getThisScore() + p.getGangScore() + p.getYuFen());
			} else {
				p.setThisScore( -2 * fen);
				p.setYuFen(-yuFen);
				p.setScore(p.getScore()+p.getThisScore() + p.getGangScore() + p.getYuFen());
			}
		}
	}

	private static void SanJiaFuZhuangZiMo(List<Player> players, int fen,
			int yuFen) {
		for (Player p : players) {
			if (p.getIsHu()) {
				p.setThisScore(3 * 2 * fen);
				p.setYuFen(3 * yuFen);
				p.setScore(p.getScore()+p.getThisScore() + p.getGangScore() + p.getYuFen());
			} else {
				p.setThisScore(-2 * fen);
				p.setYuFen(-yuFen);
				p.setScore(p.getScore()+p.getThisScore() + p.getGangScore() + p.getYuFen());
			}
		}
	}

	public static void changeGangFen(Action action, List<Player> players,
			Player player, RoomResp room, Integer type) {
		// 明杠
//		if (type == 1) {
//			Long beiGang = action.getToUserId();
//			Integer fen = 1;
//			if (room.getGangFanBei() == 1) {
//				fen = fen * 2;
//			}
//			if (room.getGangZhuang() == 1
//					&& zhuangGangOrBeiGang(room, player, beiGang)) {
//				fen = fen * 2;
//			}
//			player.setGangScore(player.getGangScore() + fen);
//			for (Player p : players) {
//				if (p.getUserId().equals(beiGang)) {
//					p.setGangScore(p.getGangScore() - fen);
//				}
//			}
//		}
		Integer fen = type;
		if (room.getGangFanBei() == 1) {
			fen = fen * 2;
		}
		if (room.getGangZhuang() == 1) {
			if (player.getUserId().equals(room.getZhuangId())) {
				player.setGangScore(player.getGangScore() + 6 * fen);
				for (Player p : players) {
					if (p.getUserId().equals(player.getUserId())) {
						continue;
					}
					p.setGangScore(p.getGangScore() - 2 * fen);
				}
			} else {
				player.setGangScore(player.getGangScore() + 4 * fen);
				for (Player p : players) {
					if (p.getUserId().equals(player.getUserId())) {
						continue;
						}
					if (p.getUserId().equals(room.getZhuangId())) {
						p.setGangScore(p.getGangScore() - 2 * fen);
					} else {
						p.setGangScore(p.getGangScore() - fen);
					}
				}
			}
		} else {
			player.setGangScore(player.getGangScore() + 3 * fen);
			for (Player p : players) {
				if (p.getUserId().equals(player.getUserId())) {
					continue;
				}
				p.setGangScore(p.getGangScore() - fen);
			}
		}
	}
	
	public static boolean zhuangGangOrBeiGang(RoomResp room, Player p,
			Long userId) {
		Boolean result = p.getUserId().equals(room.getZhuangId())
				|| userId.equals(room.getZhuangId());
		return result;
	}

}
