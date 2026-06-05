package com.ruoyi.framework.web.service.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mall.domain.MallAuthConfig;

/**
 * SMS OTP sender. Production providers (Aliyun/Tencent) to be wired when credentials are ready.
 */
@Component
public class MallOtpSmsSender
{
    private static final Logger log = LoggerFactory.getLogger(MallOtpSmsSender.class);

    public boolean isProviderConfigured(MallAuthConfig config)
    {
        return config != null
            && StringUtils.isNotEmpty(config.getSmsProvider())
            && StringUtils.isNotEmpty(config.getSmsAccessKey())
            && StringUtils.isNotEmpty(config.getSmsSecretKey())
            && StringUtils.isNotEmpty(config.getSmsSign())
            && StringUtils.isNotEmpty(config.getSmsTemplateId());
    }

    public void send(MallAuthConfig config, String phone, String code)
    {
        if (!isProviderConfigured(config))
        {
            if ("1".equals(config.getSmsMock()))
            {
                log.warn("[OTP SMS mock] phone={} code={} (configure SMS provider in mall auth settings for production)", phone, code);
                return;
            }
            throw new ServiceException("\u77ed\u4fe1\u53d1\u9001\u672a\u914d\u7f6e\uff0c\u8bf7\u8054\u7cfb\u7ba1\u7406\u5458");
        }
        // TODO: integrate Aliyun / Tencent SMS SDK when going live
        throw new ServiceException("\u77ed\u4fe1\u901a\u9053\u5c1a\u672a\u63a5\u5165\uff0c\u8bf7\u5148\u5f00\u542f\u6a21\u62df\u6a21\u5f0f\u6216\u7b49\u5f85\u6b63\u5f0f\u73af\u5883\u914d\u7f6e");
    }
}
