package com.ruoyi.mall.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.domain.MallChatMessage;

public interface MallChatMessageMapper
{
    List<MallChatMessage> selectMessagesBySessionId(@Param("sessionId") Long sessionId, @Param("afterId") Long afterId);

    int insertMallChatMessage(MallChatMessage message);
}
