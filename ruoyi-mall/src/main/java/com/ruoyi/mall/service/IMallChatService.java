package com.ruoyi.mall.service;

import java.util.List;
import com.ruoyi.mall.domain.MallChatAgent;
import com.ruoyi.mall.domain.MallChatConfig;
import com.ruoyi.mall.domain.MallChatMessage;
import com.ruoyi.mall.domain.MallChatSession;

public interface IMallChatService
{
    MallChatConfig getConfig();

    int updateConfig(MallChatConfig config, String operator);

    List<MallChatAgent> selectAgentList(MallChatAgent query);

    MallChatAgent selectAgentByUserId(Long userId);

    int addAgent(MallChatAgent agent, String operator);

    int updateAgent(MallChatAgent agent, String operator);

    int deleteAgent(Long agentId);

    int setAgentOnline(Long userId, boolean online, String operator);

    List<MallChatSession> selectSessionList(MallChatSession query);

    MallChatSession getOrCreateSession(Long userId, String userName);

    List<MallChatMessage> listMessages(Long sessionId, Long afterId, Long userId, boolean admin);

    MallChatMessage sendUserMessage(Long userId, String userName, Long sessionId, String content, String msgType);

    MallChatMessage sendAgentMessage(Long agentUserId, String agentName, Long sessionId, String content, String msgType);

    MallChatSession transferToHuman(Long userId, Long sessionId);

    int closeSession(Long sessionId, Long operatorUserId, boolean admin);

    void tryAssignWaitingSessions();
}
