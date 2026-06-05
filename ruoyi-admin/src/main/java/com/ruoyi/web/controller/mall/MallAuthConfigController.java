package com.ruoyi.web.controller.mall;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.OtpAuthService;
import com.ruoyi.mall.domain.MallAuthConfig;

@RestController
@RequestMapping("/mall/auth")
public class MallAuthConfigController extends BaseController
{
    @Autowired
    private OtpAuthService otpAuthService;

    @PreAuthorize("@ss.hasPermi('mall:auth:query')")
    @GetMapping("/config")
    public AjaxResult getConfig()
    {
        MallAuthConfig config = otpAuthService.getConfig();
        maskSecrets(config);
        return success(config);
    }

    @PreAuthorize("@ss.hasPermi('mall:auth:edit')")
    @PutMapping("/config")
    public AjaxResult updateConfig(@RequestBody MallAuthConfig config)
    {
        if (config.getSmtpPassword() != null && config.getSmtpPassword().contains("****"))
        {
            config.setSmtpPassword(null);
        }
        if (config.getSmsSecretKey() != null && config.getSmsSecretKey().contains("****"))
        {
            config.setSmsSecretKey(null);
        }
        return toAjax(otpAuthService.updateConfig(config, getUsername()));
    }

    private void maskSecrets(MallAuthConfig config)
    {
        if (config == null)
        {
            return;
        }
        if (StringUtils.isNotEmpty(config.getSmtpPassword()))
        {
            config.setSmtpPassword("****");
        }
        if (StringUtils.isNotEmpty(config.getSmsSecretKey()))
        {
            config.setSmsSecretKey("****");
        }
    }
}
