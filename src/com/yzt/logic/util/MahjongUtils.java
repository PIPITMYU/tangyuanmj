 package com.yzt.logic.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.yzt.logic.mj.domain.Action;
import com.yzt.logic.mj.domain.Player;
import com.yzt.logic.mj.domain.RoomResp;
import com.yzt.logic.util.JudegHu.checkHu.Hulib;
import com.yzt.logic.util.JudegHu.checkHu.TableMgr;
import com.yzt.logic.util.redis.RedisUtil;

/**
 * 
 * @author wsw_007
 *
 */
public class MahjongUtils {

	static {
		// 加载胡的可能
		TableMgr.getInstance().load();
	}

	/**
	 * 获得所需要的牌型(干里干) 并打乱牌型
	 * 
	 * @return
	 */
	public static List<Integer> getPais(RoomResp room) {
		// 1-9万 ,10-18饼,19-27条,32红中.
		ArrayList<Integer> pais = new ArrayList<Integer>();
		for (int j = 0; j < 4; j++) {
			for (int i = 1; i <= 27; i++) {
				pais.add(i);
			}
			pais.add(32);
		}
		// 2.洗牌
		Collections.shuffle(pais);
		return pais;

	}

	/**
	 * 删除用户指定的一张牌
	 * 
	 * @param currentPlayer
	 * @return
	 */
	public static void removePai(Player currentPlayer, Integer action) {
		Iterator<Integer> pai = currentPlayer.getCurrentMjList().iterator();
		while (pai.hasNext()) {
			Integer item = pai.next();
			if (item.equals(action)) {
				pai.remove();
				break;
			}
		}
	}

	/**
	 * 
	 * @param room
	 *            房间
	 * @param currentPlayer
	 *            当前操作的玩家
	 * @return 返回需要通知的操作的玩家ID
	 */
	public static Long nextActionUserId(RoomResp room, Long lastUserId) {
		Long[] playerIds = room.getPlayerIds();

		for (int i = 0; i < playerIds.length; i++) {
			if (lastUserId == playerIds[i]) {
				if (i == playerIds.length - 1) { // 如果是最后 一个,则取第一个.
					return playerIds[0];
				} else {
					return playerIds[i + 1];
				}
			}
		}
		return -100l;
	}

	/**
	 * 干里干胡牌规则 1,必须有幺九牌 2,要求三门齐或清一色(红中不算们,单门+红中胡牌算清一色) 3,必须有刻牌(三个一样,或者四个一样)
	 * 4,必须开门才能胡牌(吃,碰,杠有一个算开门) 5,每一局只能胡一个玩家. 6,胡的优先级(胡>杠>碰>吃)
	 * 
	 *
	 * @return true 为可以胡牌牌型
	 */

	public static boolean checkHuRule(Player p, RoomResp room, Integer pai,
			Integer type) {
		List<Integer> newList = getNewList(p.getCurrentMjList());
		if (type == Cnst.CHECK_TYPE_ZIJIMO) {
			pai = null;
		}
		if (pai != null) {
			newList.add(pai); // 检测要带上别人打出的牌
		}
		if (room.getQiDui() == 1 && checkQiDui(p, newList)) {
			return true;
		}

		boolean piHu = checkPiHu(p, room, pai, type);
		if (!piHu) {
			return false;
		}

		// 幺九 有刻
		if (checkShenFeng(p, room, newList)
				|| (checkYiJiu(p, newList) && isKePai(p, newList))) {
			return true;
		}

		return false;
	}

	/**
	 * 检测屁胡
	 * 
	 * @param p
	 * @param room
	 * @param pai
	 * @param type
	 * @return
	 */
	private static boolean checkPiHu(Player p, RoomResp room, Integer pai,
			Integer type) {
		int[] huPaiZu = new int[34];
		if (type == Cnst.CHECK_TYPE_ZIJIMO) {
			huPaiZu = getCheckHuPai(p.getCurrentMjList(), null);
			if (Hulib.getInstance().get_hu_info(huPaiZu, 34, 34)) {
				return true;
			}
		} else {
			huPaiZu = getCheckHuPai(p.getCurrentMjList(), pai);
			if (Hulib.getInstance().get_hu_info(huPaiZu, 34, 34)) {
				return true;
			}
		}
		return false;
	}

	/***
	 * 检测七对
	 * 
	 * @param p
	 * @param newList
	 * @param pai
	 * @return
	 */
	public static boolean checkQiDui(Player p, List<Integer> newList) {
		if (newList.size() != 14) {
			return false;
		}
		Integer hunNum = 0;
		int oneNum = 0;
		int[] checkHuPai = getCheckHuPai(newList, null);
		for (int i : checkHuPai) {
			if (i == 1 || i == 3) {
				oneNum++;
			}
		}
		if (oneNum <= hunNum) {
			return true;
		}
		return false;
	}

	/**
	 * 检测神风 红中顶19刻
	 * 
	 * @param p
	 * @param room
	 * @param newList
	 * @return
	 */
	public static boolean checkShenFeng(Player p, RoomResp room,
			List<Integer> newList) {
		if (room.getShenFeng() == 2) {
			return false;
		}
		// 有两个红中即可
		int hongNum = 0;
		for (Integer i : newList) {
			if (i == 32) {
				hongNum++;
			}
			if (hongNum == 2) {
				return true;
			}
		}
		//碰红中 或 杠红肿也算
		if(p.getActionList().size()>0){
			for(int i=0;i<p.getActionList().size();i++){
				if(p.getActionList().get(i).getExtra() == 32){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 检测动作集合
	 * 
	 * @param p
	 * @param pai
	 * @param room
	 * @param type
	 * @param checkChi
	 *            自己打的牌true,不提示吃.
	 * @return
	 */
	public static List<Integer> checkActionList(Player p, Integer pai,
			RoomResp room, Integer type, Boolean checkChi) {
		List<Integer> actionList = new ArrayList<Integer>();

		if (Cnst.CHECK_TYPE_HAIDIANPAI == type) {
			if (checkHuRule(p, room, pai, Cnst.CHECK_TYPE_ZIJIMO)) {
				actionList.add(500);
			}
			return actionList;
		}
		
		if(Cnst.CHECK_TYPE_QIANGGANG == type){
			if (checkHuRule(p,room,pai,Cnst.CHECK_TYPE_BIERENCHU)) {
				//胡的这张必须是特
				if(checkQiangGangHu(p, room, pai)) {
					actionList.add(500);
				}
														
			}
			if(actionList.size() > 0){
				actionList.add(0);
			}
			return actionList;
		}
		
		if (checkChi
				&& type != Cnst.CHECK_TYPE_ZIJIMO && checkChi(p, pai)) {
			List<Integer> c = chi(p, pai);
			actionList.addAll(c);
		}
		if ( type != Cnst.CHECK_TYPE_ZIJIMO && checkPeng(p, pai)) {
			Integer peng = peng(p, pai);
			actionList.add(peng);
		}
		// 1,不是自摸,检测别人出牌的时候,能不能点杠.
		if ( Cnst.CHECK_TYPE_ZIJIMO != type && checkGang(p, pai)) {
			Integer gang = gang(p, pai, false);
			actionList.add(gang);
		}
		// 2,自摸的时候,检测能不能暗杠.
		if ( Cnst.CHECK_TYPE_ZIJIMO == type) {
			List<Integer> checkAnGang = checkAnGang(p, room);
			for (int i = 0; i < checkAnGang.size(); i++) {
				actionList.add(checkAnGang.get(i));
			}
		}
		// 3,自摸的时候,检测能不能碰杠 这里不用有手扒一限制
		if (Cnst.CHECK_TYPE_ZIJIMO == type) {
			List<Integer> pengGang = checkPengGang(p, pai);
			if (pengGang.size() != 0) {
				actionList.addAll(pengGang);
			}

		}

		if (checkHuRule(p, room, pai, type)) {
			actionList.add(500);
		}

		if (actionList.size() != 0) {
			actionList.add(0);
		} else {
			// 没有动作 只能出牌
			if (type == Cnst.CHECK_TYPE_ZIJIMO) {
				actionList.add(501);
			}
		}
		return actionList;
	}

	/**
	 * 检测能不能碰完以后再开杠.
	 * 
	 * @param p
	 * @return
	 */
	private static List<Integer> checkPengGang(Player p, Integer pai) {
		List<Action> actionList = p.getActionList();// 统计用户所有动作 (吃碰杠等)
		List<Integer> newList = getNewList(p.getCurrentMjList());
		List<Integer> gangList = new ArrayList<Integer>();
		for (int i = 0; i < actionList.size(); i++) {
			if (actionList.get(i).getType() == 2) {
				for (int m = 0; m < newList.size(); m++) {
					if (newList.get(m) == actionList.get(i).getExtra()) {
						gangList.add(newList.get(m) + 90);
					}
				}
			}
		}
		return gangList;
	}

	/**
	 * 手把一
	 * 
	 * @param p
	 * @param type
	 * @return
	 */
	public static boolean isShouBaYi(Player p, Integer type, Integer actionType) {
		// 飘可以手把一 
		List<Action> actions = p.getActionList();
		in: if (actions != null && actionType != Cnst.ACTION_TYPE_CHI
				&& actions.size() == 3) {
			for (Action action : actions) {
				if (action.getType() == Cnst.ACTION_TYPE_CHI) {
					break in;
				}
			}
			return true;
		}
		if (type == Cnst.CHECK_TYPE_BIERENCHU) {
			if (p.getCurrentMjList().size() <= 4) {
				return false;
			}
			return true;
		}
		if (type == Cnst.CHECK_TYPE_ZIJIMO
				|| type == Cnst.CHECK_TYPE_HAIDIANPAI) {
			if (p.getCurrentMjList().size() <= 5) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * 牌型是否有刻牌(三个一样或者四个一样)
	 * 
	 * @param p
	 * @return true 是
	 */
	public static boolean isKePai(Player p, List<Integer> newList) {
		// 检测动作里面是否有刻
		List<Action> actionList = p.getActionList();
		// 1吃 2碰 3点杠 4碰杠 5暗杠
		for (Action action : actionList) {
			if (action.getType() != 1) {
				return true;
			}
		}
		// 检测手中有没有刻
		Set<Integer> distinct = new HashSet<Integer>();
		for (Integer integer : newList) {
			distinct.add(integer);
		}
		// 手牌中是否有3张,这3张移除必须能胡才行 比如12333

		int num = 0;
		for (Integer distinctPai : distinct) {
			num = 0;
			for (Integer p1 : newList) {
				if (distinctPai.equals(p1)) {
					num++;
				}
			}
			if (num >= 3) {
				int[] huPaiZu = getCheckHuPai(newList, null);
				// 将这3张牌移除
				huPaiZu[distinctPai - 1] = num - 3;
				if (Hulib.getInstance().get_hu_info(huPaiZu, 34, 34)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 胡牌必须有幺九.
	 * 
	 * @param p
	 * @param list
	 * @return
	 */
	private static boolean checkYiJiu(Player p, List<Integer> shouPai) {
		// 检测手牌是否有1-9
		for (int i = 0; i < shouPai.size(); i++) {
			if (shouPai.get(i) == 1 || shouPai.get(i) == 10
					|| shouPai.get(i) == 19 || shouPai.get(i) == 9
					|| shouPai.get(i) == 18 || shouPai.get(i) == 27
					) {
				return true;

			}
		}
		// 判断有动作的牌类型是否相同
		List<Action> actionList = p.getActionList();
		if (actionList.size() > 0) {
			for (Action action : actionList) {
				if (Cnst.ACTION_YI_JIU.contains(action.getActionId())) {
					return true;
				}
			}
		}
		return false;
		// 检测手里有没有混
		// Integer hunNum = hunNum(p, hun);
		// if(hunNum == null || hunNum == 0){
		// return false;
		// }
		// List<Integer> newList = getNewList(list);
		// Iterator<Integer> it = newList.iterator();
		// while(it.hasNext()){
		// Integer x = it.next();
		// if(x == hun){
		// it.remove();
		// }
		// }
		// for (Integer i : Cnst.PAI_YI_JIU) {
		// newList.add(i);
		// int[] checkHu = getCheckHuPai(newList, null);
		// checkHu[33] = hunNum - 1;
		// if(Hulib.getInstance().get_hu_info(checkHu,34,33)){
		// //检测是碰碰胡 或 平胡
		// if(isPengPengHu(p, newList, hunNum-1)){
		// p.getFanShu().add(Cnst.YAOJIUPENGPENGHU);
		// }else{
		// p.getFanShu().add(Cnst.BUSHIYAOJIUPENGPENGHU);
		// }
		// return true;
		// }else{
		// it = newList.iterator();
		// a:while(it.hasNext()){
		// Integer x = it.next();
		// if(x == i){
		// it.remove();//!!!
		// break a;
		// }
		// }
		// }
		//
		// }
		// return false;
	}

	/***
	 * 根据出的牌 设置下个动作人和玩家
	 * 
	 * @param players
	 * @param room
	 * @param pai
	 */
	public static void getNextAction(List<Player> players, RoomResp room,
			Integer pai) {
		Integer maxAction = 0;
		Long nextActionUserId = -1L;
		List<Integer> nextAction = new ArrayList<Integer>();
		int index = -1;
		Long[] playIds = room.getPlayerIds();
		for (int i = 0; i < playIds.length; i++) {
			if (playIds[i].equals(room.getLastChuPaiUserId())) {
				index = i + 1;
				if (index == 4) {
					index = 0;
				}
				break;
			}
		}
		Long xiaYiJia = playIds[index];
		// 从下一家开始检测 多胡的话 会按顺序来
		Player[] checkList = new Player[4];
		for (int i = 0; i < players.size(); i++) {
			if (i == index) {
				checkList[0] = players.get(i);
			}
			if (i < index) {
				checkList[4 - (index - i)] = players.get(i);
			}
			if (i > index) {
				checkList[i - index] = players.get(i);
			}
		}
		for (Player p : checkList) {
			if (!room.getGuoUserIds().contains(p.getUserId())) {
				// 玩家没点击过 或者不是 出牌的人 吃只检测下个人
				List<Integer> checkActionList;
				if (p.getUserId().equals(xiaYiJia)) {
					checkActionList = checkActionList(p, pai, room,
							Cnst.CHECK_TYPE_BIERENCHU, true);
				} else {
					checkActionList = checkActionList(p, pai, room,
							Cnst.CHECK_TYPE_BIERENCHU, false);
				}

				if (checkActionList.size() == 0) {
					// 玩家没动作
					room.getGuoUserIds().add(p.getUserId());
				} else {
					Collections.sort(checkActionList);
					if (checkActionList.get(checkActionList.size() - 1) > maxAction) {
						nextActionUserId = p.getUserId();
						nextAction = checkActionList;
						maxAction = checkActionList
								.get(checkActionList.size() - 1);
					}
				}
			}
		}
		// 如果都没可执行动作 下一位玩家请求发牌
		if (maxAction == 0) {
			nextAction.add(-1);
			room.setNextAction(nextAction);
			// 取到上个出牌人的角标 下一位来发牌
			room.setNextActionUserId(xiaYiJia);
		} else {
			room.setNextAction(nextAction);
			room.setNextActionUserId(nextActionUserId);
		}

	}

	/**
	 * 检查玩家能不能碰
	 * 
	 * @param p
	 * @param Integer
	 *            peng 要碰的牌
	 * @return
	 */
	public static boolean checkPeng(Player p, Integer peng) {
		int num = 0;
		for (Integer i : p.getCurrentMjList()) {
			if (i == peng) {
				num++;
			}
		}
		if (num >= 2) {
			return true;
		}
		return false;
	}

	/**
	 * //与吃的那个牌能组合的List
	 * 
	 * @param p
	 * @param chi
	 * @return
	 */
	public static List<Integer> reChiList(Integer action, Integer chi) {
		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		for (int i = 35; i <= 56; i++) {
			if (i == action) {
				int[] js = Cnst.chiMap.get(action);
				for (int j = 0; j < js.length; j++) {
					if (js[j] != chi) {
						arrayList.add(js[j]);
					}
				}
			}
		}
		return arrayList;
	}

	/**
	 * 执行动作吃! 返回原本手里的牌
	 * 
	 * @param p
	 * @param chi
	 * @return
	 */
	public static List<Integer> chi(Player p, Integer chi) {
		List<Integer> shouPai = getNewList(p.getCurrentMjList());
		Set<Integer> set = new HashSet<Integer>();
		List<Integer> reList = new ArrayList<Integer>();
		boolean a = false; // x<x+1<x+2
		boolean b = false; // x-1<x<x+1
		boolean c = false; // x-2<x-1<x

		// 万
		if (chi < 10) { // 基数34
			List<Integer> arr = new ArrayList<Integer>();
			arr.add(chi + 1);
			arr.add(chi + 2);
			if (shouPai.containsAll(arr)) {
				a = true;
			}
			List<Integer> arr1 = new ArrayList<Integer>();
			arr1.add(chi - 1);
			arr1.add(chi + 1);
			if (shouPai.containsAll(arr1)) {
				b = true;
			}
			List<Integer> arr2 = new ArrayList<Integer>();
			arr2.add(chi - 1);
			arr2.add(chi - 2);
			if (shouPai.containsAll(arr2)) {
				c = true;
			}

			if (a && chi != 9 && chi != 8) {
				set.add(34 + chi);
			}
			if (b && chi != 9) {
				set.add(33 + chi);
			}
			if (c) {
				set.add(32 + chi);
			}

			// 饼
		} else if (chi >= 10 && chi <= 18) { // 基数32
			List<Integer> arr = new ArrayList<Integer>();
			arr.add(chi + 1);
			arr.add(chi + 2);
			if (shouPai.containsAll(arr)) {
				a = true;
			}
			List<Integer> arr1 = new ArrayList<Integer>();
			arr1.add(chi - 1);
			arr1.add(chi + 1);
			if (shouPai.containsAll(arr1)) {
				b = true;
			}
			List<Integer> arr2 = new ArrayList<Integer>();
			arr2.add(chi - 1);
			arr2.add(chi - 2);
			if (shouPai.containsAll(arr2)) {
				c = true;
			}
			if (a & chi != 18 && chi != 17) {
				set.add(32 + chi);
			}
			if (b && chi != 10 && chi != 18) {
				set.add(31 + chi);
			}
			if (c && chi != 10 && chi != 11) {
				set.add(30 + chi);
			}
			// 条
		} else if (chi >= 19 && chi <= 27) { // 基数30
			List<Integer> arr = new ArrayList<Integer>();
			arr.add(chi + 1);
			arr.add(chi + 2);
			if (shouPai.containsAll(arr)) {
				a = true;
			}
			List<Integer> arr1 = new ArrayList<Integer>();
			arr1.add(chi - 1);
			arr1.add(chi + 1);
			if (shouPai.containsAll(arr1)) {
				b = true;
			}
			List<Integer> arr2 = new ArrayList<Integer>();
			arr2.add(chi - 1);
			arr2.add(chi - 2);
			if (shouPai.containsAll(arr2)) {
				c = true;
			}
			if (a & chi != 26 && chi != 27) {
				set.add(30 + chi);
			}
			if (b && chi != 19 && chi != 27) {
				set.add(29 + chi);
			}
			if (c && chi != 19 && chi != 20) {
				set.add(28 + chi);
			}
		}
		reList.addAll(set);
		return reList;
	}

	/**
	 * 执行动作杠
	 * 
	 * @param p
	 * @param gang
	 * @return
	 */
	public static Integer gang(Player p, Integer gang, Boolean pengGang) {
		List<Integer> shouPai = p.getCurrentMjList();
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();

		if (pengGang) {
			List<Action> actionList = p.getActionList();// 统计用户所有动作 (吃碰杠等)
			for (int i = 0; i < actionList.size(); i++) {
				if (actionList.get(i).getType() == 2
						&& actionList.get(i).getExtra() == gang) {
					return 90 + gang;
				}
			}
		}

		for (Integer item : shouPai) {
			if (map.containsKey(item)) {
				map.put(item, map.get(item).intValue() + 1);
			} else {
				map.put(item, new Integer(1));
			}
		}

		Iterator<Integer> keys = map.keySet().iterator();
		while (keys.hasNext()) {
			Integer key = keys.next();
			if (map.get(key).intValue() == 3) { // 控制有几个重复的
				// System.out.println(key + "有重复的:" + map.get(key).intValue() +
				// "个 ");
				if (key == gang) {
					return 90 + gang;
				}
			}
		}

		return -100;
	}

	/**
	 * 执行动作碰
	 * 
	 * @param p
	 * @param peng
	 * @return 行为编码
	 */
	public static Integer peng(Player p, Integer peng) {
		return 56 + peng;
	}

	/**
	 * * 检测玩家能不能吃.10 与19特殊处理
	 * 
	 * @param p
	 * @param chi
	 * @param hunPai
	 *            不能吃
	 * @return
	 */
	public static boolean checkChi(Player p, Integer chi) {
		List<Integer> list = getNewList(p.getCurrentMjList());
		boolean isChi = false;
		List<Integer> arr = new ArrayList<Integer>();
		arr.add(chi + 1);
		arr.add(chi + 2);
		if (list.containsAll(arr)) {
			isChi = true;
		}
		List<Integer> arr1 = new ArrayList<Integer>();
		List<Integer> arr2 = new ArrayList<Integer>();
		if (chi != 10 && chi != 19) {
			arr1.add(chi - 1);
			arr1.add(chi + 1);
			if (list.containsAll(arr1)) {
				isChi = true;
			}
			arr2.add(chi - 1);
			arr2.add(chi - 2);
			if (list.containsAll(arr2)) {
				isChi = true;
			}
		}
		return isChi;
	}

	/**
	 * 执行暗杠
	 * 
	 * @param p
	 * @return 返回杠的牌
	 */
	public static Integer anGang(Player p) {
		List<Integer> shouPai = p.getCurrentMjList();
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();

		for (Integer item : shouPai) {
			if (map.containsKey(item)) {
				map.put(item, map.get(item).intValue() + 1);
			} else {
				map.put(item, new Integer(1));
			}
		}

		Iterator<Integer> keys = map.keySet().iterator();
		Integer gang = 0;
		while (keys.hasNext()) {
			Integer key = keys.next();
			if (map.get(key).intValue() == 4) { // 控制有几个重复的
				// System.out.println(key + "有重复的:" + map.get(key).intValue() +
				// "个 ");
				gang = key;
			}
		}

		Iterator<Integer> iter1 = p.getCurrentMjList().iterator();
		while (iter1.hasNext()) {
			Integer item = iter1.next();
			if (item == gang) {
				iter1.remove();
			}
		}
		return gang + 90;
	}

	/**
	 * 检查能不能暗杠
	 * 
	 * @param p
	 * @param gang
	 * @return
	 */
	public static List<Integer> checkAnGang(Player p, RoomResp room) {
		List<Integer> anGangList = new ArrayList<Integer>();
		List<Integer> shouPai = p.getCurrentMjList();
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();

		for (Integer item : shouPai) {
			if (map.containsKey(item)) {
				map.put(item, map.get(item).intValue() + 1);
			} else {
				map.put(item, new Integer(1));
			}
		}

		Iterator<Integer> keys = map.keySet().iterator();
		while (keys.hasNext()) {
			Integer key = keys.next();
			if (map.get(key).intValue() == 4) { // 控制有几个重复的
				anGangList.add(key + 90);
			}
		}
		return anGangList;
	}

	/**
	 * 检测玩家能不能杠 1,明杠,2暗杠,3 点杠
	 * 
	 * @param p
	 * @return
	 */
	public static boolean checkGang(Player p, Integer gang) {
		List<Integer> shouPai = p.getCurrentMjList();
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();

		for (Integer item : shouPai) {
			if (map.containsKey(item)) {
				map.put(item, map.get(item).intValue() + 1);
			} else {
				map.put(item, new Integer(1));
			}
		}

		Iterator<Integer> keys = map.keySet().iterator();
		while (keys.hasNext()) {
			Integer key = keys.next();
			if (map.get(key).intValue() == 3) { // 控制有几个重复的;
				if (key == gang) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * @param mahjongs
	 *            房间内剩余麻将的组合
	 * @param num
	 *            发的张数
	 * @return
	 */
	public static List<Integer> faPai(List<Integer> mahjongs, Integer num) {
		if (mahjongs.size() == 8) {
			return null;
		}
		List<Integer> result = new ArrayList<>();
		for (int i = 0; i < num; i++) {
			result.add(mahjongs.get(i));
			mahjongs.remove(i);
		}
		return result;
	}

	/**
	 * 返回一个新的集合
	 * 
	 * @param old
	 * @return
	 */
	public static List<Integer> getNewList(List<Integer> old) {
		List<Integer> newList = new ArrayList<Integer>();
		if (old != null && old.size() > 0) {
			for (Integer pai : old) {
				newList.add(pai);
			}
		}
		return newList;
	}

	/**
	 * 丹阳推到胡规则 返回的是分
	 * 
	 * @param players
	 * @param room
	 * @return
	 */
	public static int checkHuFenInfo(List<Player> players, RoomResp room) {
		Player p = null;
		// 胡牌就是1分
		int fen = 1;
		List<Integer> winInfo = new ArrayList<Integer>();
		for (Player player : players) {
			if (player.getIsHu()) {
				player.setHuNum(player.getHuNum() + 1);
				p = player;
			}
		}

		if (room.getZhuangId().equals(p.getUserId())) {
			winInfo.add(Cnst.ZHUANG);
		}
		// 清一色
		if (isQingYiSe(p, room, p.getCurrentMjList())) {
			winInfo.add(Cnst.QINGYISE);
			fen = fen * 4;
		}
		List<Integer> newList = getNewList(p.getCurrentMjList());
		if (room.getQiDui() == 1 && checkQiDui(p, newList)) {
			winInfo.add(Cnst.QIXIAODUI);
			fen = fen * 4;
			if(room.getDaiTe() == 1){
				//检测豪七
				Set<Integer> qiDui = new HashSet<Integer>(p.getCurrentMjList());
				if(qiDui.size()<7){
					fen = fen * 2;
					winInfo.add(Cnst.TE);
				}
			}		
		} else {
			// 飘
			if (isPengPengHu(p, newList, 0)) {
				winInfo.add(Cnst.PIAO);
				fen = fen * 4;
			} else {
				// 夹胡
				if (checkKaBianDiao(p, room)) {
					winInfo.add(Cnst.JIAHU);
					fen = fen * 2;
					if(room.getDaiTe() == 1){
						Integer huDePai = p.getCurrentMjList().get(
								p.getCurrentMjList().size() - 1);
						if (room.getTeList() != null
								&& room.getTeList().contains(huDePai)) {
							winInfo.add(Cnst.TE);
							fen = fen * 2;
						}
					}			
				}
			}
		}

		if (!winInfo.contains(Cnst.JIAHU) && !winInfo.contains(Cnst.QINGYISE)
				&& !winInfo.contains(Cnst.PIAO)
				&& !winInfo.contains(Cnst.QIXIAODUI)
				&& !winInfo.contains(Cnst.TE)) {
			winInfo.add(Cnst.PINGHU);
		}
		// 门清两番
		if (p.getActionList() != null && p.getActionList().size() == 0) {
			fen = fen * 2;
			winInfo.add(Cnst.MENQING);
		}
		// 自摸两番
		if (p.getIsZiMo()) {
			fen = fen * 2;
			winInfo.add(Cnst.ZIMO);
		} else {		
//			winInfo.add(Cnst.DIANPAO);
		}
		//手扒一
		if(isHuShouBaYi(p)){
			winInfo.add(Cnst.SHOUBAYI);
			fen = fen * 2;
		}
		p.setFanShu(winInfo);

		return fen;
	}

	/**
	 * 从牌桌上,把玩家吃碰杠的牌移除.
	 * 
	 * @param room
	 * @param players
	 */

	public static void removeCPG(RoomResp room, List<Player> players) {
		Player currentP = null;
		for (Player p : players) {
			if (p.getUserId().equals(room.getLastChuPaiUserId())) {
				currentP = p;
				List<Integer> chuList = p.getChuList();
				Iterator<Integer> iterator = chuList.iterator();
				while (iterator.hasNext()) {
					Integer pai = iterator.next();
					if (room.getLastChuPai() == pai) {
						iterator.remove();
						break;
					}
				}
			}
		}
		RedisUtil.updateRedisData(null, currentP);
	}

	/***
	 * 移除动作手牌
	 * 
	 * @param currentMjList
	 * @param chi
	 * @param action
	 * @param type
	 */
	public static void removeActionMj(List<Integer> currentMjList,
			List<Integer> chi, Integer action, Integer type) {
		Iterator<Integer> it = currentMjList.iterator(); // 遍历手牌,删除碰的牌
		switch (type) {
		case Cnst.ACTION_TYPE_CHI:
			int chi1 = 0;
			int chi2 = 0;
			a: while (it.hasNext()) {
				Integer x = it.next();
				if (x == chi.get(0) && chi1 == 0) {
					it.remove();
					chi1 = 1;
				}
				if (x == chi.get(1) && chi2 == 0) {
					it.remove();
					chi2 = 1;
				}
				if (chi1 == 1 && chi2 == 1) {
					break a;
				}
			}
			break;
		case Cnst.ACTION_TYPE_PENG:
			int num = 0;
			while (it.hasNext()) {
				Integer x = it.next();
				if (x == action - 56) {
					it.remove();
					num = num + 1;
					if (num == 2) {
						break;
					}
				}
			}
			break;
		case Cnst.ACTION_TYPE_ANGANG:
			List<Integer> gangPai = new ArrayList<Integer>();
			gangPai.add(action - 90);
			currentMjList.removeAll(gangPai);
			break;
		case Cnst.ACTION_TYPE_PENGGANG:
			gangPai = new ArrayList<Integer>();
			gangPai.add(action - 90);
			currentMjList.removeAll(gangPai);
			break;
		case Cnst.ACTION_TYPE_DIANGANG:
			gangPai = new ArrayList<Integer>();
			gangPai.add(action - 90);
			currentMjList.removeAll(gangPai);
			break;
		default:
			break;
		}
	}

	/***
	 * 获得 检测胡牌的 34位数组 包括摸得或者别人打的那张
	 * 
	 * @param currentList
	 * @param pai
	 * @return
	 */
	public static int[] getCheckHuPai(List<Integer> currentList, Integer pai) {
		int[] checkHuPai = new int[34];
		List<Integer> newList = getNewList(currentList);
		if (pai != null) {
			newList.add(pai);
		}
		for (int i = 0; i < newList.size(); i++) {
			int a = checkHuPai[newList.get(i) - 1];
			checkHuPai[newList.get(i) - 1] = a + 1;
		}
		return checkHuPai;
	}

	/***
	 * 获得 检测胡牌的 34位数组 不包括摸得或者别人打的那张
	 * 
	 * @param currentList
	 * @param pai
	 * @return
	 */
	public static int[] getRemoveLastPai(List<Integer> currentList, Integer pai) {
		int[] checkHuPai = new int[34];
		Boolean hasRemove = false;
		for (int i = 0; i < currentList.size(); i++) {
			if (currentList.get(i) == pai && !hasRemove) {
				hasRemove = true;
				continue;
			}
			int a = checkHuPai[currentList.get(i) - 1];
			checkHuPai[currentList.get(i) - 1] = a + 1;
		}
		return checkHuPai;
	}

	/**
	 * 干里干 牌型是否是清一色 红中不算门 ,单门+红中胡牌算是清一色
	 * 
	 * @param p玩家
	 * @return
	 */
	public static boolean isQingYiSe(Player p, RoomResp room, List<Integer> list) {
		Integer leixing = 0;
		Boolean needcheck = false;
		List<Integer> newList = getNewList(list);
		Collections.sort(newList);
		Integer pai = newList.get(0);
		leixing = (pai - 1) / 9;
		if (leixing == 3) {// 单调红中，只看吃碰杠类型就Ok
			needcheck = true;
		} else {// 不是单调红中
			for (Integer shouPai : newList) {
				// 红中跳出
				if ((shouPai - 1) / 9 == 3) {
					continue;
				}
				// 要检测的类型不再相同
				if (leixing != (shouPai - 1) / 9) {
					return false;
				}
			}
		}
		// 判断有动作的牌类型是否相同
		Integer extra = 0;
		List<Action> actionList = p.getActionList();
		if (actionList.size() > 0) {
			if (needcheck) {// 绝对不会大于3了，因为红中只能碰一次
				leixing = (actionList.get(0).getExtra() - 1) / 9;
				needcheck = false;
			}
			for (Action action : actionList) {
				extra = action.getExtra();
				if (extra >= 28) {
					continue;
				} else {
					if (leixing != (extra - 1) / 9) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * 牌型是不是碰碰胡(全是刻牌的牌型)
	 * 
	 * @param p
	 * @param hunNum
	 * @param newList
	 * @return
	 */
	public static boolean isPengPengHu(Player p, List<Integer> newList,
			Integer hunNum) {
		// 检测动作里面是否有刻
		List<Action> actionList = p.getActionList();
		// 1吃 2碰 3点杠 4碰杠 5暗杠
		for (Action action : actionList) {
			if (action.getType() == 1) {
				return false;
			}
		}
		// 检测手牌是不是都是刻
		int[] checkHuPai = getCheckHuPai(newList, null);
		int twoNum = 0;
		int oneNum = 0;
		for (Integer integer : checkHuPai) {
			if (integer == 1) {
				oneNum++;
			} else if (integer == 2) {
				twoNum++;
			} else if (integer == 3) {
			} else if (integer == 4) {
				return false;
			}
		}
		// 两张的需要1张混，1张需要两张混,但是将减少一张混的需求
		if ((twoNum + oneNum * 2 - 1) <= hunNum) {
			return true;
		}

		return false;
	}
	public static Boolean checkKaBianDiao(Player winPlayer, RoomResp room) {
		List<Integer> currentMjList = winPlayer.getCurrentMjList();
		List<Integer> newList = getNewList(currentMjList);
		int size = newList.size();
		Integer dongZuoPai = newList.get(size - 1);
		// 移除这张牌剩下的牌集合
		newList.remove(size - 1);
		// 获取手牌的数组集合
		int[] shouPaiArr = new int[34];
		for (Integer integer : newList) {
			int i = shouPaiArr[integer - 1];
			shouPaiArr[integer - 1] = i + 1;
		}
		// 最后那张不能是红中
		if (dongZuoPai != 32) {
			// 定义动作牌的数子大小
			int dongZuoPaiNum = dongZuoPai % 9;		
			// 说明没有红中或者房间规则没有神风
			// 动作牌必须是1或者9
			if (dongZuoPaiNum == 1 || dongZuoPaiNum == 0) {
				// 检测特殊,如果胡 1,4 但是没1,9(1,9必须存在才胡),那么也算
				// 有神风选项
				if (room.getShenFeng().equals(1)) {
					// 有红中的花不用检测了,直接不成立
					if (shouPaiArr[32 - 1] > 0) {
						return false;
					}
				}
				// 手牌里面必须没有1,9 和玩家的动作里面必须没有1和9
				if (!checkYiJiu(winPlayer, newList)) {
					// 手牌里面没有1,9
					return true;
				}
			}

		}
		int huNum = 0;
		//检测吊  只能胡一张 这里循环28张牌 往里带 只能胡一次
		for(int i=0;i<=27;i++){
			if(i == 27){
				//单独带红中
				i = 31;
			}
			shouPaiArr[i] = shouPaiArr[i] + 1;
			if (Hulib.getInstance().get_hu_info(shouPaiArr, 34, 34)) {
				huNum ++;
			}
			//在减回去
			shouPaiArr[i] = shouPaiArr[i] - 1;
		}
		if(huNum == 1){
			return true;
		}
		return false;
	}
	// 检测遍卡吊
	public static Boolean checkKaBianDiao1(Player winPlayer, RoomResp room) {
		List<Integer> currentMjList = winPlayer.getCurrentMjList();
		List<Integer> newList = getNewList(currentMjList);
		int size = newList.size();
		Integer dongZuoPai = newList.get(size - 1);
		// 移除这张牌剩下的牌集合
		newList.remove(size - 1);
		// 获取手牌的数组集合
		int[] shouPaiArr = new int[34];
		for (Integer integer : newList) {
			int i = shouPaiArr[integer - 1];
			shouPaiArr[integer - 1] = i + 1;
		}
		// 检测吊
		if (shouPaiArr[dongZuoPai - 1] != 0) {
			int dongZuoNum = shouPaiArr[dongZuoPai - 1];
			shouPaiArr[dongZuoPai - 1] = dongZuoNum - 1;
			if (Hulib.getInstance().get_hu_info(shouPaiArr, 34, 34)) {
				//还原原来的数量
				shouPaiArr[dongZuoPai - 1]=dongZuoNum;
				if(dongZuoNum<28){
					int dongZuoBigNum=(dongZuoNum-1)%9+1;
					if(dongZuoBigNum<=3){
						//找大于3的数
						int bigSanNum = shouPaiArr[dongZuoPai-1+3];
						if(bigSanNum>0){
							shouPaiArr[dongZuoPai-1+3]=bigSanNum-1;
							if (Hulib.getInstance().get_hu_info(shouPaiArr, 34, 34)) {
								//不是单吊--单吊只能胡1
								//数据还原
								 shouPaiArr[dongZuoPai-1+3]=bigSanNum;
							}else{
								return true;
							}
						}else{
							return true;
						}
					}else if(dongZuoBigNum>=7){
						//找小于3的数
						int smallSanNum = shouPaiArr[dongZuoPai-1-3];
						if(smallSanNum>0){
							shouPaiArr[dongZuoPai-1-3]=smallSanNum-1;
							if (Hulib.getInstance().get_hu_info(shouPaiArr, 34, 34)) {
								//不是单吊--单吊只能胡1
								//数据还原
								 shouPaiArr[dongZuoPai-1-3]=smallSanNum;
							}else{
								return true;
							}
						}else{
							return true;
						}
					}else {
						//找大于3和小于3的数
						Boolean needCheck=true;
						//找小于3的数
						int smallSanNum = shouPaiArr[dongZuoPai-1-3];
						if(smallSanNum>0){
							shouPaiArr[dongZuoPai-1-3]=smallSanNum-1;
							if (Hulib.getInstance().get_hu_info(shouPaiArr, 34, 34)) {
								//不是单吊--单吊只能胡1
								//数据还原
								needCheck=false;
								shouPaiArr[dongZuoPai-1-3]=smallSanNum;
							}else{
								return true;
							}
						}else{
							return true;
						}
						//找大于3的数
						if(needCheck){
							int bigSanNum = shouPaiArr[dongZuoPai-1+3];
							if(bigSanNum>0){
								shouPaiArr[dongZuoPai-1+3]=bigSanNum-1;
								if (Hulib.getInstance().get_hu_info(shouPaiArr, 34, 34)) {
									//不是单吊--单吊只能胡1
									//数据还原
									shouPaiArr[dongZuoPai-1+3]=bigSanNum;
								}else{
									return true;
								}
							}else{
								return true;
							}
							
						}
					}
				}else{
					return true;
				}
			}
			// 没胡,加回来
			shouPaiArr[dongZuoPai - 1] = dongZuoNum;
		}
		// 最后那张不能是红中
		if (dongZuoPai != 32) {
			// 定义动作牌的数子大小
			int dongZuoPaiNum = dongZuoPai % 9;
			// 检测卡
			// 动作牌不能是1和9 ----1,9怎么卡？
			if (dongZuoPaiNum != 1 && dongZuoPaiNum != 0) {
				// 看比它小1
				int smallOneNum = shouPaiArr[dongZuoPai - 2];
				// 看比它大1
				// 看比它大1和小1的是否存在
				int bigOneNum = shouPaiArr[dongZuoPai];
				if (smallOneNum > 0 && bigOneNum > 0) {
					shouPaiArr[dongZuoPai - 2] = smallOneNum - 1;
					shouPaiArr[dongZuoPai] = bigOneNum - 1;
					if (Hulib.getInstance().get_hu_info(shouPaiArr, 34, 34)) {
						return true;
					}
					// 没胡,加回来
					shouPaiArr[dongZuoPai - 2] = smallOneNum;
					shouPaiArr[dongZuoPai] = bigOneNum;
				}
			}
			// 检测边 --动作牌是3或者7
			if (dongZuoPaiNum == 3) {
				// 查看比它小2的
				int smallTwoNum = shouPaiArr[dongZuoPai - 3];
				// 查看比它小1的
				int smallOneNum = shouPaiArr[dongZuoPai - 2];
				// 看比它小2和小1的是否存在
				if (smallTwoNum > 0 && smallOneNum > 0) {
					shouPaiArr[dongZuoPai - 3] = smallTwoNum - 1;
					shouPaiArr[dongZuoPai - 2] = smallOneNum - 1;
					if (Hulib.getInstance().get_hu_info(shouPaiArr, 34, 34)) {
						return true;
					}
					// 没胡,加回来
					shouPaiArr[dongZuoPai - 3] = smallTwoNum;
					shouPaiArr[dongZuoPai - 2] = smallOneNum;
				}
			} else if (dongZuoPaiNum == 7) {
				// 查看比它大1的
				int bigOneNum = shouPaiArr[dongZuoPai];
				// 查看比它大2的
				int bigTwoNum = shouPaiArr[dongZuoPai + 1];
				// 看比它大1和大2的是否存在
				if (bigOneNum > 0 && bigTwoNum > 0) {
					shouPaiArr[dongZuoPai] = bigOneNum - 1;
					shouPaiArr[dongZuoPai + 1] = bigTwoNum - 1;
					if (Hulib.getInstance().get_hu_info(shouPaiArr, 34, 34)) {
						return true;
					}
					// 没胡,加回来
					shouPaiArr[dongZuoPai] = bigOneNum;
					shouPaiArr[dongZuoPai + 1] = bigTwoNum;
				}
			}
			// 检测特殊,如果胡 1,4 但是没1,9(1,9必须存在才胡),那么也算
			// 有神风选项
			if (room.getShenFeng().equals(1)) {
				// 有红中的花不用检测了,直接不成立
				if (shouPaiArr[32 - 1] > 0) {
					return false;
				}
			}
			// 说明没有红中或者房间规则没有神风
			// 动作牌必须是1或者9
			if (dongZuoPaiNum == 1 || dongZuoPaiNum == 0) {
				// 手牌里面必须没有1,9 和玩家的动作里面必须没有1和9
				if (!checkYiJiu(winPlayer, newList)) {
					// 手牌里面没有1,9
					return true;
				}
			}

		}
		return false;
	}
	
	//出完牌 设置手把一
	public static void setShouBaYi(Player p,RoomResp room){
		if(p.getShouBaYi() == 2){
			return;
		}
		
		if(p.getCurrentMjList().size() == 1){
			boolean allChi = true;
			for(Action a:p.getActionList()){
				if(a.getType()!=1){
					allChi = false;
					break;
				}
			}
			if(allChi == false && (checkYiJiu(p, p.getCurrentMjList())|| checkShenFeng(p, room, p.getCurrentMjList()))){
				p.setShouBaYi(2);
			}
		}
	}
	
	//结算 判断手扒一 为什么不用上边那个方法 防止一手吃 摸俩红中 胡了
	public static boolean isHuShouBaYi(Player p){
		if(p.getCurrentMjList().size() == 2){
			return true;
		}
		return false;
	}
	
	/**
	 * 必须满足特 才可以抢杠胡
	 * @param winPlayer
	 * @param room
	 * @return
	 */
	public static Boolean checkQiangGangHu(Player winPlayer, RoomResp room,Integer pai) {
		List<Integer> currentMjList = winPlayer.getCurrentMjList();
		List<Integer> newList = getNewList(currentMjList);
		newList.add(pai);
		int size = newList.size();
		Integer dongZuoPai = newList.get(size - 1);
		// 移除这张牌剩下的牌集合
		newList.remove(size - 1);
		// 获取手牌的数组集合
		int[] shouPaiArr = new int[34];
		for (Integer integer : newList) {
			int i = shouPaiArr[integer - 1];
			shouPaiArr[integer - 1] = i + 1;
		}
		// 最后那张不能是红中
		if (dongZuoPai != 32) {
			// 定义动作牌的数子大小
			int dongZuoPaiNum = dongZuoPai % 9;		
			// 说明没有红中或者房间规则没有神风
			// 动作牌必须是1或者9
			if (dongZuoPaiNum == 1 || dongZuoPaiNum == 0) {
				// 检测特殊,如果胡 1,4 但是没1,9(1,9必须存在才胡),那么也算
				// 有神风选项
				if (room.getShenFeng().equals(1)) {
					// 有红中的花不用检测了,直接不成立
					if (shouPaiArr[32 - 1] > 0) {
						return false;
					}
				}
				// 手牌里面必须没有1,9 和玩家的动作里面必须没有1和9
				if (!checkYiJiu(winPlayer, newList)) {
					// 手牌里面没有1,9
					return true;
				}
			}

		}
		int huNum = 0;
		//检测吊  只能胡一张 这里循环28张牌 往里带 只能胡一次
		for(int i=0;i<=27;i++){
			if(i == 27){
				//单独带红中
				i = 31;
			}
			shouPaiArr[i] = shouPaiArr[i] + 1;
			if (Hulib.getInstance().get_hu_info(shouPaiArr, 34, 34)) {
				huNum ++;
			}
			//在减回去
			shouPaiArr[i] = shouPaiArr[i] - 1;
		}
		if(huNum == 1){
			return true;
		}
		return false;
	}
}
