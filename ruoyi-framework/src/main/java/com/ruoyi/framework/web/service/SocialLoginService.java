package com.ruoyi.framework.web.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.framework.config.properties.SocialLoginProperties;
import com.ruoyi.system.domain.SysSocialBind;
import com.ruoyi.system.mapper.SysSocialBindMapper;
import com.ruoyi.system.service.ISysUserService;

/** WeChat / Alipay OAuth login service. */
@Service
public class SocialLoginService
{
    public static final String TYPE_WECHAT = "wechat";
    public static final String TYPE_ALIPAY = "alipay";

    private static final String MSG_WECHAT_DISABLED = "\u672a\u542f\u7528\u5fae\u4fe1\u767b\u5f55";
    private static final String MSG_WECHAT_CONFIG = "\u8bf7\u5728 application.yml \u914d\u7f6e social.wechat.app-id \u4e0e app-secret";
    private static final String MSG_ALIPAY_DISABLED = "\u672a\u542f\u7528\u652f\u4ed8\u5b9d\u767b\u5f55";
    private static final String MSG_ALIPAY_CONFIG = "\u8bf7\u5728 application.yml \u914d\u7f6e social.alipay.app-id";
    private static final String MSG_UNSUPPORTED = "\u4e0d\u652f\u6301\u7684\u767b\u5f55\u65b9\u5f0f";
    private static final String MSG_MOCK_OFF = "\u6a21\u62df\u767b\u5f55\u672a\u5f00\u542f";
    private static final String MSG_CODE_EMPTY = "\u6388\u6743\u7801\u4e3a\u7a7a";
    private static final String MSG_ALIPAY_SDK = "\u652f\u4ed8\u5b9d\u6b63\u5f0f\u6388\u6743\u9700\u63a5\u5165\u652f\u4ed8\u5b9d SDK\uff0c\u8bf7\u5148\u4f7f\u7528\u6a21\u62df\u767b\u5f55\u6216\u914d\u7f6e\u540e\u6269\u5c55";
    private static final String MSG_WECHAT_AUTH_FAIL = "\u5fae\u4fe1\u6388\u6743\u5931\u8d25\uff1a";
    private static final String MSG_BIND_USER_MISSING = "\u7ed1\u5b9a\u7528\u6237\u4e0d\u5b58\u5728";
    private static final String NICK_WECHAT = "\u5fae\u4fe1\u7528\u6237";
    private static final String NICK_ALIPAY = "\u652f\u4ed8\u5b9d\u7528\u6237";

    @Autowired
    private SocialLoginProperties socialProps;
    @Autowired
    private SysSocialBindMapper socialBindMapper;
    @Autowired
    private ISysUserService userService;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private TokenService tokenService;

    public Map<String, Object> getClientConfig()
    {
        Map<String, Object> map = new HashMap<>();
        map.put("enabled", socialProps.isEnabled());
        map.put("mockEnabled", socialProps.isMockEnabled());
        map.put("wechatEnabled", socialProps.isEnabled() && socialProps.getWechat().isEnabled());
        map.put("alipayEnabled", socialProps.isEnabled() && socialProps.getAlipay().isEnabled());
        map.put("wechatConfigured", socialProps.getWechat().isConfigured());
        map.put("alipayConfigured", socialProps.getAlipay().isConfigured());
        return map;
    }

    public String buildAuthorizeUrl(String type, String redirect)
    {
        String state = encodeState(redirect);
        if (TYPE_WECHAT.equals(type))
        {
            if (!socialProps.getWechat().isEnabled())
            {
                throw new ServiceException(MSG_WECHAT_DISABLED);
            }
            if (!socialProps.getWechat().isConfigured())
            {
                if (socialProps.isMockEnabled())
                {
                    return null;
                }
                throw new ServiceException(MSG_WECHAT_CONFIG);
            }
            SocialLoginProperties.Wechat wx = socialProps.getWechat();
            String redirectUri = urlEncode(wx.getRedirectUri());
            return "https://open.weixin.qq.com/connect/qrconnect?appid=" + wx.getAppId()
                + "&redirect_uri=" + redirectUri
                + "&response_type=code&scope=snsapi_login&state=" + urlEncode(state)
                + "#wechat_redirect";
        }
        if (TYPE_ALIPAY.equals(type))
        {
            if (!socialProps.getAlipay().isEnabled())
            {
                throw new ServiceException(MSG_ALIPAY_DISABLED);
            }
            if (!socialProps.getAlipay().isConfigured())
            {
                if (socialProps.isMockEnabled())
                {
                    return null;
                }
                throw new ServiceException(MSG_ALIPAY_CONFIG);
            }
            SocialLoginProperties.Alipay ali = socialProps.getAlipay();
            return "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?app_id=" + ali.getAppId()
                + "&scope=auth_user&redirect_uri=" + urlEncode(ali.getRedirectUri())
                + "&state=" + urlEncode(state);
        }
        throw new ServiceException(MSG_UNSUPPORTED);
    }

    public String mockLogin(String type, String redirect)
    {
        if (!socialProps.isMockEnabled())
        {
            throw new ServiceException(MSG_MOCK_OFF);
        }
        String openId = "mock_" + type + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String nickname = TYPE_WECHAT.equals(type) ? NICK_WECHAT : NICK_ALIPAY;
        return loginByOpenId(type, openId, null, nickname, "");
    }

    public String loginByCallback(String type, String code, String state)
    {
        if (StringUtils.isEmpty(code))
        {
            throw new ServiceException(MSG_CODE_EMPTY);
        }
        if (TYPE_WECHAT.equals(type))
        {
            if ("mock".equals(code) && socialProps.isMockEnabled())
            {
                return mockLogin(type, decodeState(state));
            }
            return loginWechat(code);
        }
        if (TYPE_ALIPAY.equals(type))
        {
            if ("mock".equals(code) && socialProps.isMockEnabled())
            {
                return mockLogin(type, decodeState(state));
            }
            throw new ServiceException(MSG_ALIPAY_SDK);
        }
        throw new ServiceException(MSG_UNSUPPORTED);
    }

    private String loginWechat(String code)
    {
        SocialLoginProperties.Wechat wx = socialProps.getWechat();
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + wx.getAppId()
            + "&secret=" + wx.getAppSecret() + "&code=" + code + "&grant_type=authorization_code";
        JSONObject json = fetchJson(url);
        if (json.containsKey("errcode"))
        {
            throw new ServiceException(MSG_WECHAT_AUTH_FAIL + json.getString("errmsg"));
        }
        String openId = json.getString("openid");
        String unionId = json.getString("unionid");
        return loginByOpenId(TYPE_WECHAT, openId, unionId, NICK_WECHAT, "");
    }

    private String loginByOpenId(String type, String openId, String unionId, String nickname, String avatar)
    {
        SysSocialBind bind = socialBindMapper.selectByTypeAndOpenId(type, openId);
        SysUser user;
        if (bind != null)
        {
            user = userService.selectUserById(bind.getUserId());
            if (user == null)
            {
                throw new ServiceException(MSG_BIND_USER_MISSING);
            }
        }
        else
        {
            user = createSocialUser(type, openId, nickname);
            SysSocialBind newBind = new SysSocialBind();
            newBind.setUserId(user.getUserId());
            newBind.setSocialType(type);
            newBind.setOpenId(openId);
            newBind.setUnionId(unionId);
            newBind.setNickname(nickname);
            newBind.setAvatar(avatar);
            socialBindMapper.insertSocialBind(newBind);
        }
        LoginUser loginUser = (LoginUser) userDetailsService.createLoginUser(user);
        recordLogin(user);
        return tokenService.createToken(loginUser);
    }

    private SysUser createSocialUser(String type, String openId, String nickname)
    {
        String prefix = TYPE_WECHAT.equals(type) ? "wx_" : "ali_";
        String username = prefix + openId;
        if (username.length() > 30)
        {
            username = prefix + openId.substring(openId.length() - (30 - prefix.length()));
        }
        int suffix = 1;
        String tryName = username;
        while (userService.selectUserByUserName(tryName) != null)
        {
            tryName = username.substring(0, Math.min(26, username.length())) + suffix;
            suffix++;
        }
        SysUser user = new SysUser();
        user.setUserName(tryName);
        user.setNickName(StringUtils.isNotEmpty(nickname) ? nickname : tryName);
        user.setDeptId(105L);
        user.setPassword(SecurityUtils.encryptPassword(UUID.randomUUID().toString()));
        user.setStatus("0");
        user.setRoleIds(new Long[] { 2L });
        user.setCreateBy("social");
        userService.insertUser(user);
        return userService.selectUserByUserName(tryName);
    }

    private void recordLogin(SysUser user)
    {
        user.setLoginIp(IpUtils.getIpAddr());
        user.setLoginDate(DateUtils.getNowDate());
        userService.updateUserProfile(user);
    }

    public String buildFrontendCallbackUrl(String token, String redirect, String error)
    {
        String base = socialProps.getFrontendBase();
        if (base.endsWith("/"))
        {
            base = base.substring(0, base.length() - 1);
        }
        String path = "/social/callback";
        StringBuilder q = new StringBuilder();
        if (StringUtils.isNotEmpty(token))
        {
            q.append("token=").append(urlEncode(token));
        }
        if (StringUtils.isNotEmpty(error))
        {
            if (q.length() > 0) q.append("&");
            q.append("error=").append(urlEncode(error));
        }
        if (StringUtils.isNotEmpty(redirect))
        {
            if (q.length() > 0) q.append("&");
            q.append("redirect=").append(urlEncode(redirect));
        }
        return base + path + (q.length() > 0 ? "?" + q : "");
    }

    private JSONObject fetchJson(String url)
    {
        RestTemplate rest = new RestTemplate();
        String body = rest.getForObject(url, String.class);
        return JSON.parseObject(body);
    }

    public static String encodeState(String redirect)
    {
        String raw = StringUtils.isEmpty(redirect) ? "/shop/home" : redirect;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static String decodeState(String state)
    {
        if (StringUtils.isEmpty(state))
        {
            return "/shop/home";
        }
        try
        {
            return new String(Base64.getUrlDecoder().decode(state), StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            return "/shop/home";
        }
    }

    private static String urlEncode(String value)
    {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
