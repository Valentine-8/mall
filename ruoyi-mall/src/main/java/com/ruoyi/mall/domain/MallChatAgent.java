package com.ruoyi.mall.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class MallChatAgent extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long agentId;
    private Long userId;
    private String nickName;
    private String status;
    private Integer maxSessions;
    private Integer activeSessions;
    private Integer sortOrder;
    private String delFlag;
    private String userName;

    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getMaxSessions() { return maxSessions; }
    public void setMaxSessions(Integer maxSessions) { this.maxSessions = maxSessions; }
    public Integer getActiveSessions() { return activeSessions; }
    public void setActiveSessions(Integer activeSessions) { this.activeSessions = activeSessions; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}
