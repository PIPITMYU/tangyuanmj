package com.yzt.logic.mj.domain;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by admin on 2017/6/26.
 */
/**
 * @author wsw_008
 *
 */
public class Player extends User {

	private Integer roomId;// 房间密码，也是roomSn

	// out离开状态（断线）;inline正常在线；
	private Integer state;
	private List<Integer> currentMjList = new ArrayList<Integer>();// 用户手中当前的牌
	private List<Integer> chuList = new ArrayList<Integer>();// 出牌的集合
	private Integer position;// 位置信息；详见Cnst
	private String ip;
	

	private Boolean isZiMo; // 是不是自摸
	
	private Boolean isHu;//小结算用 是不是胡
	private Boolean isDian;//小结算用 不是点炮
	private Integer score;// 玩家这全游戏的总分
	private Integer thisScore;// 记录当玩家当前局
	private List<Integer> fanShu = new ArrayList<Integer>(); // 牌型记番,具体的番数.
	private String notice;// 跑马灯信息
	private Integer playStatus;// 用户当前状态， dating用户在大厅中; in刚进入房间，等待状态;prepared准备状态;game游戏中; xjs小结算
								
	private Integer huNum;// 胡的次数
	private Integer dianNum;// 点炮次数
	private Integer zhuangNum;// 坐庄次数
	private Integer zimoNum;// 自摸次数

	private String channelId;// 通道id
	private Long updateTime;// 更新用户数据时间
	private List<Action> actionList =new ArrayList<Action>();//统计用户所有动作 (吃碰杠等)
	private Integer yuFen;//当局鱼粉
	private Integer gangScore;//当局杠分

	private Double x_index;
	private Double y_index;
	public void initPlayer(Integer roomId,Integer playStatus,Integer score) {
		// TODO
		if(roomId == null){
			this.position = null;	
			this.roomId = null;
			this.huNum = 0;
			this.zhuangNum = 0;
			this.zimoNum = 0;
			this.dianNum = 0;
			this.score = 0;
		}
		this.playStatus = playStatus;
		this.chuList = null;
		this.isHu = false;
		this.isDian = false;
		this.score = score;
		this.thisScore = 0;
		this.actionList = new ArrayList<Action>();
		this.currentMjList = new ArrayList<Integer>();
		this.isZiMo = false;
		this.fanShu = new ArrayList<Integer>();
		this.gangScore = 0;
		this.yuFen = 0;
	}

	

	public Boolean getIsZiMo() {
		return isZiMo;
	}

	public void setIsZiMo(Boolean isZiMo) {
		this.isZiMo = isZiMo;
	}

	public List<Integer> getFanShu() {
		return fanShu;
	}

	public void setFanShu(List<Integer> fanShu) {
		this.fanShu = fanShu;
	}



	public Integer getRoomId() {
		return roomId;
	}

	public void setRoomId(Integer roomId) {
		this.roomId = roomId;
	}

	public List<Integer> getCurrentMjList() {
		return currentMjList;
	}

	public void setCurrentMjList(List<Integer> currentMjList) {
		this.currentMjList = currentMjList;
	}

	public List<Integer> getChuList() {
		return chuList;
	}

	public void setChuList(List<Integer> chuList) {
		this.chuList = chuList;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}


	public Boolean getIsHu() {
		return isHu;
	}

	public void setIsHu(Boolean isHu) {
		this.isHu = isHu;
	}

	public Boolean getIsDian() {
		return isDian;
	}

	public void setIsDian(Boolean isDian) {
		this.isDian = isDian;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public Integer getThisScore() {
		return thisScore;
	}

	public void setThisScore(Integer thisScore) {
		this.thisScore = thisScore;
	}

	public String getNotice() {
		return notice;
	}

	public void setNotice(String notice) {
		this.notice = notice;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Integer getPlayStatus() {
		return playStatus;
	}

	public void setPlayStatus(Integer playStatus) {
		this.playStatus = playStatus;
	}


	public Integer getHuNum() {
		return huNum;
	}

	public void setHuNum(Integer huNum) {
		this.huNum = huNum;
	}

	public Integer getDianNum() {
		return dianNum;
	}

	public void setDianNum(Integer dianNum) {
		this.dianNum = dianNum;
	}

	public Integer getZhuangNum() {
		return zhuangNum;
	}

	public void setZhuangNum(Integer zhuangNum) {
		this.zhuangNum = zhuangNum;
	}

	public Integer getZimoNum() {
		return zimoNum;
	}

	public void setZimoNum(Integer zimoNum) {
		this.zimoNum = zimoNum;
	}
	
	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public Long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}

	public List<Action> getActionList() {
		return actionList;
	}

	public void setActionList(List<Action> actionList) {
		this.actionList = actionList;
	}
	//添加 动作
	public void addActionList(Action action){
		this.actionList.add(action);
	}


	public Integer getGangScore() {
		return gangScore;
	}



	public void setGangScore(Integer gangScore) {
		this.gangScore = gangScore;
	}


	public Double getX_index() {
		return x_index;
	}



	public void setX_index(Double x_index) {
		this.x_index = x_index;
	}



	public Double getY_index() {
		return y_index;
	}



	public void setY_index(Double y_index) {
		this.y_index = y_index;
	}



	public Integer getYuFen() {
		return yuFen;
	}



	public void setYuFen(Integer yuFen) {
		this.yuFen = yuFen;
	}
	
	
}
