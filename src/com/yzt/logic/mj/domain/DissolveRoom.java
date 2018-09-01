package com.yzt.logic.mj.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/11.
 */
public class DissolveRoom implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = -8680777279186262414L;
	private Long dissolveTime;
    private Long userId;
    private List<Map<String,Object>> othersAgree;

    public Long getDissolveTime() {
        return dissolveTime;
    }

    public void setDissolveTime(Long dissolveTime) {
        this.dissolveTime = dissolveTime;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<Map<String, Object>> getOthersAgree() {
        return othersAgree;
    }

    public void setOthersAgree(List<Map<String, Object>> othersAgree) {
        this.othersAgree = othersAgree;
    }
}
