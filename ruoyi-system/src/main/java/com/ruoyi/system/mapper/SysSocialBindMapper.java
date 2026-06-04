package com.ruoyi.system.mapper;

import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.SysSocialBind;

public interface SysSocialBindMapper
{
    SysSocialBind selectByTypeAndOpenId(@Param("socialType") String socialType, @Param("openId") String openId);

    int insertSocialBind(SysSocialBind bind);
}
