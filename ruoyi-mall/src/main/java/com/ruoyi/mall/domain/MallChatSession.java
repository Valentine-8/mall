package com.ruoyi.mall.domain;

import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;

public class MallChatSession extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    public static final String STATUS_AI = "0";
    public static final String STATUS_WAITING = "1";
    public static final String STATUS_HUMAN = "2";
    public static final String STATUS_CLOSED = "3";

    private Long sessionId;
    private Long userId;
    private String userName;
    private Long agentId;
    private String agentName;
    private String status;
    private String lastMessage;
    private Date lastMessageTime;

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }
    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public Date getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(Date lastMessageTime) { this.lastMessageTime = lastMessageTime; }
}
