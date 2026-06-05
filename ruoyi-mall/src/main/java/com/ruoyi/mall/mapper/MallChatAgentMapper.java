package com.ruoyi.mall.mapper;

import java.util.List;
import com.ruoyi.mall.domain.MallChatAgent;

public interface MallChatAgentMapper
{
    MallChatAgent selectMallChatAgentById(Long agentId);

    MallChatAgent selectMallChatAgentByUserId(Long userId);

    List<MallChatAgent> selectMallChatAgentList(MallChatAgent agent);

    List<MallChatAgent> selectAvailableAgents();

    int insertMallChatAgent(MallChatAgent agent);

    int updateMallChatAgent(MallChatAgent agent);

    int deleteMallChatAgentById(Long agentId);

    int increaseActiveSessions(Long agentId);

    int decreaseActiveSessions(Long agentId);
}
