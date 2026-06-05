package com.ruoyi.mall.mapper;

import java.util.List;
import com.ruoyi.mall.domain.MallChatSession;

public interface MallChatSessionMapper
{
    MallChatSession selectMallChatSessionById(Long sessionId);

    MallChatSession selectOpenSessionByUserId(Long userId);

    List<MallChatSession> selectMallChatSessionList(MallChatSession session);

    List<MallChatSession> selectWaitingSessions();

    int insertMallChatSession(MallChatSession session);

    int updateMallChatSession(MallChatSession session);
}
