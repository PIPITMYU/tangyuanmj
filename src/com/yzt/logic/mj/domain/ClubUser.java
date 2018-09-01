package com.yzt.logic.mj.domain;

import java.io.Serializable;

/**
 * 俱乐部人员表
 */
public class ClubUser implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 740857240407272929L;
	private Integer id;
	private Integer clubId;//俱乐部id
	private Long userId;//用户id
	private Integer status;//状态 状态 0申请加入 1已通过 2申请退出
	private Long createTime;//创建时间
	/********不属于表的********/
//	private String clubName;//俱乐部名称
//	private String createName;//主持人 创建人
//	private Integer sumPerson;//人数
//	private Integer personQuota;//人数上限
	
	public Integer getId() {
		return id;
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
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}
//	public String getClubName() {
//		return clubName;
//	}
//	public void setClubName(String clubName) {
//		this.clubName = clubName;
//	}
//	public String getCreateName() {
//		return createName;
//	}
//	public void setCreateName(String createName) {
//		this.createName = createName;
//	}
//	public Integer getSumPerson() {
//		return sumPerson;
//	}
//	public void setSumPerson(Integer sumPerson) {
//		this.sumPerson = sumPerson;
//	}
//	public Integer getPersonQuota() {
//		return personQuota;
//	}
//	public void setPersonQuota(Integer personQuota) {
//		this.personQuota = personQuota;
//	}
	
}
