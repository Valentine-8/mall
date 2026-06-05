package com.ruoyi.mall.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class MallChatMessage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    public static final String SENDER_USER = "user";
    public static final String SENDER_AI = "ai";
    public static final String SENDER_AGENT = "agent";
    public static final String SENDER_SYSTEM = "system";

    public static final String MSG_TEXT = "text";
    public static final String MSG_IMAGE = "image";
    public static final String MSG_VIDEO = "video";

    private Long messageId;
    private Long sessionId;
    private String senderType;
    private Long senderId;
    private String senderName;
    private String msgType;
    private String content;

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getMsgType() { return msgType; }
    public void setMsgType(String msgType) { this.msgType = msgType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
