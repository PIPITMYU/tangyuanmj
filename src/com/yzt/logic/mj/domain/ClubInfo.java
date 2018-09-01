package com.yzt.logic.mj.domain;

import java.io.Serializable;
/**
 * 俱乐部表
 */
public class ClubInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 740857240407272929L;
	private Integer id;
	private Integer clubId;//俱乐部id
	private String clubName;//俱乐部名称
	private Long createId;//创建人
	private Integer personQuota;//总人数限额
	private Integer roomCardNum;//房卡数
	private Integer roomCardQuota;//房卡限额
	private Integer	roomCardNotice;//房卡消费预警数
	private Long createTime;//创建时间
	private Long freeStart;//俱乐部限免开始时间
	private Long freeEnd;//俱乐部限免结束时间
	/**************不存在数据库的******************/
//	private String createName;//创建人名称
//	private Integer userNum;//俱乐部当前人数
//	private Integer usedCard;//今日消耗房卡数
	
	
	
	public Integer getId() {
		return id;
	}
	public Long getFreeStart() {
		return freeStart;
	}
	public void setFreeStart(Long freeStart) {
		this.freeStart = freeStart;
	}
	public Long getFreeEnd() {
		return freeEnd;
	}
	public void setFreeEnd(Long freeEnd) {
		this.freeEnd = freeEnd;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getClubId() {
		return clubId;
	}
	public void setClubId(Integer clubId) {
		this.clubId = clubId;
	}
	public String getClubName() {
		return clubName;
	}
	public void setClubName(String clubName) {
		this.clubName = clubName;
	}
	public Long getCreateId() {
		return createId;
	}
	public void setCreateId(Long createId) {
		this.createId = createId;
	}
//	public String getCreateName() {
//		return createName;
//	}
//	public void setCreateName(String createName) {
//		this.createName = createName;
//	}
	public Integer getPersonQuota() {
		return personQuota;
	}
	public void setPersonQuota(Integer personQuota) {
		this.personQuota = personQuota;
	}
	public Integer getRoomCardNum() {
		return roomCardNum;
	}
	public void setRoomCardNum(Integer roomCardNum) {
		this.roomCardNum = roomCardNum;
	}
	public Integer getRoomCardQuota() {
		return roomCardQuota;
	}
	public void setRoomCardQuota(Integer roomCardQuota) {
		this.roomCardQuota = roomCardQuota;
	}
	public Integer getRoomCardNotice() {
		return roomCardNotice;
	}
	public void setRoomCardNotice(Integer roomCardNotice) {
		this.roomCardNotice = roomCardNotice;
	}
	public Long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}
//	public Integer getUserNum() {
//		return userNum;
//	}
//	public void setUserNum(Integer userNum) {
//		this.userNum = userNum;
//	}
//	public Integer getUsedCard() {
//		return usedCard;
//	}
//	public void setUsedCard(Integer usedCard) {
//		this.usedCard = usedCard;
//	}
	
	
}
