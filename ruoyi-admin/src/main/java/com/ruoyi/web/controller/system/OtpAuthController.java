package com.ruoyi.web.controller.system;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.framework.web.service.OtpAuthService;

@RestController
@RequestMapping("/otp")
public class OtpAuthController
{
    @Autowired
    private OtpAuthService otpAuthService;

    @GetMapping("/config")
    public AjaxResult config()
    {
        return AjaxResult.success(otpAuthService.getClientConfig());
    }

    @PostMapping("/send")
    public AjaxResult send(@RequestBody SendBody body)
    {
        otpAuthService.sendCode(body.getType(), body.getTarget());
        return AjaxResult.success();
    }

    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody body)
    {
        String token = otpAuthService.loginByCode(body.getType(), body.getTarget(), body.getCode());
        AjaxResult ajax = AjaxResult.success();
        ajax.put("token", token);
        return ajax;
    }

    public static class SendBody
    {
        private String type;
        private String target;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }
    }

    public static class LoginBody
    {
        private String type;
        private String target;
        private String code;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
}
