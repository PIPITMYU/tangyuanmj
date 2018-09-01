package com.yzt.logic.mj.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.yzt.netty.client.WSClient;

/**
 * Created by Administrator on 2017/7/8.
 */
public class RoomResp extends Room {

	private static final long serialVersionUID = -5308844344084689942L;
	private List<Integer> currentMjList;// 房间内剩余麻将集合；
	private Long zhuangId;
	// 本房间状态，1等待玩家入坐；2游戏中；3小结算
	private Integer state;
	private Integer lastNum;// 房间剩余圈数
	private Integer xiaoJuNum;// 当前第几局
	private Long xjst;// 小局开始时间
	private Integer circleWind;// 圈风...
	private Integer totolCircleNum;//进行几圈
	private Integer roomType;// 房间模式，房主模式1；自由模式2
	private DissolveRoom dissolveRoom;// 申请解散信息
	private Integer lastChuPai;// 最后出的牌
	private Long lastChuPaiUserId;// 最后出牌的玩家
	private Integer lastFaPai;// 最后发的牌
	private Long lastFaPaiUserId;//最后发牌人
	private Long xiaoShuiDi;//前端小水滴显示
	private Integer createDisId;
	private Integer applyDisId;
	private Integer outNum;
	private List<Long> guoUserIds = new ArrayList<Long>();// 动作 点击过的人
	private Integer wsw_sole_main_id;// 大接口id
	private Integer wsw_sole_action_id;// 吃碰杠出牌发牌id
	private String openName;

	private Long[] playerIds;// 玩家id集合

	private Collection<WSClient> group;// 房间 4个channel集合
	private List<List<Integer>> xiaoJuInfo = new ArrayList<List<Integer>>();// 小结算info
	private List<Integer> nextAction;// 玩家动作
	private Long nextActionUserId;// 执行动作的玩家
	private Integer lastAction;//上个动作 
	private Long lastActionUserId;//上个执行动作的玩家
	private Boolean huangZhuang;
	private Long winPlayerId;//当局胡牌的玩家
	private Integer startPosition;//代开中点开局的方位
	private Long windPosition;//风向 （发牌，吃碰杠更新）
	private List<Integer> teList = new ArrayList<Integer>();//碰牌 集合 判断特用
	private Integer yuPai;//鱼牌
	public void initRoom() {
		this.lastChuPai = null;
		this.lastChuPaiUserId = null;
		this.guoUserIds = new ArrayList<Long>();
		this.dissolveRoom = null;
		this.nextAction = null;
		this.nextActionUserId = null;
		this.huangZhuang = false;
		this.xjst = null;
		this.lastAction = null;
		this.lastActionUserId = null;
		this.lastFaPai = null;
		this.lastFaPaiUserId = null;
		this.winPlayerId = null;
		this.startPosition = null;
		this.xiaoShuiDi = null;
		this.windPosition = null;
		this.teList = new ArrayList<Integer>();
		this.yuPai = null;
	}

	
	public List<Integer> getTeList() {
		return teList;
	}


	public void setTeList(List<Integer> teList) {
		this.teList = teList;
	}


	public Integer getYuPai() {
		return yuPai;
	}


	public void setYuPai(Integer yuPai) {
		this.yuPai = yuPai;
	}


	public List<Integer> getNextAction() {
		return nextAction;
	}

	public void setNextAction(List<Integer> nextAction) {
		this.nextAction = nextAction;
	}

	public Long getNextActionUserId() {
		return nextActionUserId;
	}

	public void setNextActionUserId(Long nextActionUserId) {
		this.nextActionUserId = nextActionUserId;
	}

	public List<Integer> getCurrentMjList() {
		return currentMjList;
	}

	public void setCurrentMjList(List<Integer> currentMjList) {
		this.currentMjList = currentMjList;
	}

	public Long getZhuangId() {
		return zhuangId;
	}

	public void setZhuangId(Long zhuangId) {
		this.zhuangId = zhuangId;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Integer getLastNum() {
		return lastNum;
	}

	public void setLastNum(Integer lastNum) {
		this.lastNum = lastNum;
	}

	public Integer getXiaoJuNum() {
		return xiaoJuNum;
	}

	public void setXiaoJuNum(Integer xiaoJuNum) {
		this.xiaoJuNum = xiaoJuNum;
	}

	public Long getXjst() {
		return xjst;
	}

	public void setXjst(Long xjst) {
		this.xjst = xjst;
	}

	public Integer getCircleWind() {
		return circleWind;
	}

	public void setCircleWind(Integer circleWind) {
		this.circleWind = circleWind;
	}

	public Integer getRoomType() {
		return roomType;
	}

	public void setRoomType(Integer roomType) {
		this.roomType = roomType;
	}

	public DissolveRoom getDissolveRoom() {
		return dissolveRoom;
	}

	public void setDissolveRoom(DissolveRoom dissolveRoom) {
		this.dissolveRoom = dissolveRoom;
	}

	public Integer getCreateDisId() {
		return createDisId;
	}

	public void setCreateDisId(Integer createDisId) {
		this.createDisId = createDisId;
	}

	public Integer getApplyDisId() {
		return applyDisId;
	}

	public void setApplyDisId(Integer applyDisId) {
		this.applyDisId = applyDisId;
	}

	public Integer getOutNum() {
		return outNum;
	}

	public void setOutNum(Integer outNum) {
		this.outNum = outNum;
	}

	public List<Long> getGuoUserIds() {
		return guoUserIds;
	}

	public void setGuoUserIds(List<Long> guoUserIds) {
		this.guoUserIds = guoUserIds;
	}

	public Integer getWsw_sole_main_id() {
		return wsw_sole_main_id;
	}

	public void setWsw_sole_main_id(Integer wsw_sole_main_id) {
		this.wsw_sole_main_id = wsw_sole_main_id;
	}

	public Integer getWsw_sole_action_id() {
		return wsw_sole_action_id;
	}

	public void setWsw_sole_action_id(Integer wsw_sole_action_id) {
		this.wsw_sole_action_id = wsw_sole_action_id;
	}

	public String getOpenName() {
		return openName;
	}

	public void setOpenName(String openName) {
		this.openName = openName;
	}

	public Long[] getPlayerIds() {
		return playerIds;
	}

	public void setPlayerIds(Long[] playerIds) {
		this.playerIds = playerIds;
	}



	public Collection<WSClient> getGroup() {
		return group;
	}

	public void setGroup(Collection<WSClient> group) {
		this.group = group;
	}

	public List<List<Integer>> getXiaoJuInfo() {
		return xiaoJuInfo;
	}

	public void setXiaoJuInfo(List<List<Integer>> xiaoJuInfo) {
		this.xiaoJuInfo = xiaoJuInfo;
	}

	public void addXiaoJuInfo(List<Integer> list) {
		xiaoJuInfo.add(list);
	}


	public Boolean getHuangZhuang() {
		return huangZhuang;
	}

	public void setHuangZhuang(Boolean huangZhuang) {
		this.huangZhuang = huangZhuang;
	}

	public Integer getLastAction() {
		return lastAction;
	}

	public void setLastAction(Integer lastAction) {
		this.lastAction = lastAction;
	}

	public Long getLastActionUserId() {
		return lastActionUserId;
	}

	public void setLastActionUserId(Long lastActionUserId) {
		this.lastActionUserId = lastActionUserId;
	}

	public Long getLastChuPaiUserId() {
		return lastChuPaiUserId;
	}

	public void setLastChuPaiUserId(Long lastChuPaiUserId) {
		this.lastChuPaiUserId = lastChuPaiUserId;
	}

	public Integer getLastChuPai() {
		return lastChuPai;
	}

	public void setLastChuPai(Integer lastChuPai) {
		this.lastChuPai = lastChuPai;
	}

	public Integer getLastFaPai() {
		return lastFaPai;
	}

	public void setLastFaPai(Integer lastFaPai) {
		this.lastFaPai = lastFaPai;
	}

	public Long getLastFaPaiUserId() {
		return lastFaPaiUserId;
	}

	public void setLastFaPaiUserId(Long lastFaPaiUserId) {
		this.lastFaPaiUserId = lastFaPaiUserId;
	}

	public Long getWinPlayerId() {
		return winPlayerId;
	}

	public void setWinPlayerId(Long winPlayerId) {
		this.winPlayerId = winPlayerId;
	}


	public Integer getTotolCircleNum() {
		return totolCircleNum;
	}


	public void setTotolCircleNum(Integer totolCircleNum) {
		this.totolCircleNum = totolCircleNum;
	}



	public Integer getStartPosition() {
		return startPosition;
	}


	public void setStartPosition(Integer startPosition) {
		this.startPosition = startPosition;
	}


	public Long getXiaoShuiDi() {
		return xiaoShuiDi;
	}


	public void setXiaoShuiDi(Long xiaoShuiDi) {
		this.xiaoShuiDi = xiaoShuiDi;
	}


	public Long getWindPosition() {
		return windPosition;
	}


	public void setWindPosition(Long windPosition) {
		this.windPosition = windPosition;
	}
	
	
	
}
