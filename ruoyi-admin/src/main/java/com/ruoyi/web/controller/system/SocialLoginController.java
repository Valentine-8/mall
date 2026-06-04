package com.ruoyi.web.controller.system;

import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.framework.web.service.SocialLoginService;

/**
 * WeChat / Alipay social login API.
 */
@RestController
@RequestMapping("/social")
public class SocialLoginController
{
    @Autowired
    private SocialLoginService socialLoginService;

    @GetMapping("/config")
    public AjaxResult config()
    {
        return AjaxResult.success(socialLoginService.getClientConfig());
    }

    @GetMapping("/authorize/{type}")
    public AjaxResult authorize(@PathVariable String type, @RequestParam(required = false) String redirect)
    {
        String url = socialLoginService.buildAuthorizeUrl(type, redirect);
        Map<String, Object> data = new HashMap<>();
        if (url == null)
        {
            data.put("mock", true);
            data.put("type", type);
        }
        else
        {
            data.put("url", url);
        }
        return AjaxResult.success(data);
    }

    @PostMapping("/mock/{type}")
    public AjaxResult mockLogin(@PathVariable String type, @RequestParam(required = false) String redirect)
    {
        String token = socialLoginService.mockLogin(type, redirect);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("token", token);
        return ajax;
    }

    @GetMapping("/callback/{type}")
    public void callback(@PathVariable String type,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            HttpServletResponse response) throws Exception
    {
        String redirect = SocialLoginService.decodeState(state);
        try
        {
            String token = socialLoginService.loginByCallback(type, code, state);
            response.sendRedirect(socialLoginService.buildFrontendCallbackUrl(token, redirect, null));
        }
        catch (Exception e)
        {
            response.sendRedirect(socialLoginService.buildFrontendCallbackUrl(null, redirect, e.getMessage()));
        }
    }
}
