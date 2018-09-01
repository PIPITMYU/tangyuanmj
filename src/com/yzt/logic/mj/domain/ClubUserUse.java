package com.yzt.logic.mj.domain;

import java.io.Serializable;
/**
 * 俱乐部消费信息表
 */
public class ClubUserUse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 740857240407272929L;
	private Integer id;
	private Long userId;//消费人员id
	private Integer clubId;//俱乐部id
	private Integer roomId;//房间id
	private Integer money;//消费房卡数量
	private Long createTime;//创建时间
	/**************不存在数据库的******************/
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Integer getClubId() {
		return clubId;
	}
	public void setClubId(Integer clubId) {
		this.clubId = clubId;
	}
	public Integer getRoomId() {
		return roomId;
	}
	public void setRoomId(Integer roomId) {
		this.roomId = roomId;
	}
	public Integer getMoney() {
		return money;
	}
	public void setMoney(Integer money) {
		this.money = money;
	}
	public Long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}
	
}
