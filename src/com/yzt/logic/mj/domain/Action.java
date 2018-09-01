package com.yzt.logic.mj.domain;

import java.io.Serializable;

public class Action implements Serializable {

	private Integer type;// 1吃 2碰 3点杠 4碰杠 5暗杠
	private Long createTime;
	private Long userId;// 执行动作人
	private Long toUserId;// 被执行动作人
	private Integer actionId;// 行为编码
	private Integer extra;// 吃的牌 碰的牌 杠的牌

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getToUserId() {
		return toUserId;
	}

	public void setToUserId(Long toUserId) {
		this.toUserId = toUserId;
	}

	public Integer getExtra() {
		return extra;
	}

	public void setExtra(Integer extra) {
		this.extra = extra;
	}

	public Action() {

	}

	public Action(Integer type, Integer actionId, Long userId, Long toUserId, Integer extra) {
		this.type = type;
		this.actionId = actionId;
		this.createTime = System.currentTimeMillis();
		this.userId = userId;
		this.toUserId = toUserId;
		this.extra = extra;
	}

	public Integer getActionId() {
		return actionId;
	}

	public void setActionId(Integer actionId) {
		this.actionId = actionId;
	}

}
