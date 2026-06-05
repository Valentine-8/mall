package com.ruoyi.mall.mapper;

import com.ruoyi.mall.domain.MallAuthConfig;

public interface MallAuthConfigMapper
{
    MallAuthConfig selectMallAuthConfig();

    int updateMallAuthConfig(MallAuthConfig config);
}
