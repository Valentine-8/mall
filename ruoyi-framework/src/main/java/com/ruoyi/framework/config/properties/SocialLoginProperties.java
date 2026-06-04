package com.ruoyi.framework.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * WeChat / Alipay social login configuration.
 */
@Component
@ConfigurationProperties(prefix = "social")
public class SocialLoginProperties
{
    private boolean enabled = true;
    private boolean mockEnabled = true;
    private String frontendBase = "http://localhost";

    private Wechat wechat = new Wechat();
    private Alipay alipay = new Alipay();

    public static class Wechat
    {
        private boolean enabled = true;
        private String appId = "";
        private String appSecret = "";
        private String redirectUri = "http://localhost:8080/social/callback/wechat";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getAppId() { return appId; }
        public void setAppId(String appId) { this.appId = appId; }
        public String getAppSecret() { return appSecret; }
        public void setAppSecret(String appSecret) { this.appSecret = appSecret; }
        public String getRedirectUri() { return redirectUri; }
        public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
        public boolean isConfigured() { return appId != null && !appId.isBlank() && appSecret != null && !appSecret.isBlank(); }
    }

    public static class Alipay
    {
        private boolean enabled = true;
        private String appId = "";
        private String redirectUri = "http://localhost:8080/social/callback/alipay";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getAppId() { return appId; }
        public void setAppId(String appId) { this.appId = appId; }
        public String getRedirectUri() { return redirectUri; }
        public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
        public boolean isConfigured() { return appId != null && !appId.isBlank(); }
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isMockEnabled() { return mockEnabled; }
    public void setMockEnabled(boolean mockEnabled) { this.mockEnabled = mockEnabled; }
    public String getFrontendBase() { return frontendBase; }
    public void setFrontendBase(String frontendBase) { this.frontendBase = frontendBase; }
    public Wechat getWechat() { return wechat; }
    public void setWechat(Wechat wechat) { this.wechat = wechat; }
    public Alipay getAlipay() { return alipay; }
    public void setAlipay(Alipay alipay) { this.alipay = alipay; }
}
