package com.ruoyi.framework.web.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.framework.web.service.support.MallOtpEmailSender;
import com.ruoyi.framework.web.service.support.MallOtpSmsSender;
import com.ruoyi.mall.domain.MallAuthConfig;
import com.ruoyi.mall.mapper.MallAuthConfigMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.ISysUserService;

@Service
public class OtpAuthService
{
    public static final String TYPE_EMAIL = "email";
    public static final String TYPE_PHONE = "phone";

    private static final String MSG_TYPE_INVALID = "\u4e0d\u652f\u6301\u7684\u9a8c\u8bc1\u65b9\u5f0f";
    private static final String MSG_EMAIL_DISABLED = "\u90ae\u7bb1\u9a8c\u8bc1\u767b\u5f55\u672a\u5f00\u542f";
    private static final String MSG_PHONE_DISABLED = "\u624b\u673a\u9a8c\u8bc1\u767b\u5f55\u672a\u5f00\u542f";
    private static final String MSG_TARGET_EMPTY = "\u8bf7\u8f93\u5165\u624b\u673a\u53f7\u6216\u90ae\u7bb1";
    private static final String MSG_EMAIL_INVALID = "\u90ae\u7bb1\u683c\u5f0f\u4e0d\u6b63\u786e";
    private static final String MSG_PHONE_INVALID = "\u624b\u673a\u53f7\u683c\u5f0f\u4e0d\u6b63\u786e";
    private static final String MSG_SEND_COOLDOWN = "\u53d1\u9001\u8fc7\u4e8e\u9891\u7e41\uff0c\u8bf7\u7a0d\u540e\u518d\u8bd5";
    private static final String MSG_CODE_EMPTY = "\u8bf7\u8f93\u5165\u9a8c\u8bc1\u7801";
    private static final String MSG_CODE_INVALID = "\u9a8c\u8bc1\u7801\u9519\u8bef\u6216\u5df2\u8fc7\u671f";
    private static final String MSG_USER_DISABLED = "\u8d26\u53f7\u5df2\u505c\u7528";

    @Autowired
    private MallAuthConfigMapper authConfigMapper;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private MallOtpEmailSender emailSender;
    @Autowired
    private MallOtpSmsSender smsSender;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private ISysUserService userService;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private TokenService tokenService;

    public MallAuthConfig getConfig()
    {
        return authConfigMapper.selectMallAuthConfig();
    }

    public int updateConfig(MallAuthConfig config, String operator)
    {
        config.setUpdateBy(operator);
        return authConfigMapper.updateMallAuthConfig(config);
    }

    public Map<String, Object> getClientConfig()
    {
        MallAuthConfig cfg = requireConfig();
        Map<String, Object> map = new HashMap<>();
        map.put("emailEnabled", "1".equals(cfg.getEmailEnabled()));
        map.put("phoneEnabled", "1".equals(cfg.getPhoneEnabled()));
        map.put("emailMock", "1".equals(cfg.getEmailMock()) && !emailSender.isSmtpConfigured(cfg));
        map.put("phoneMock", "1".equals(cfg.getPhoneEnabled()) && "1".equals(cfg.getSmsMock()) && !smsSender.isProviderConfigured(cfg));
        return map;
    }

    public void sendCode(String type, String target)
    {
        MallAuthConfig cfg = requireConfig();
        String normalized = normalizeTarget(type, target);
        assertChannelEnabled(cfg, type);
        int interval = cfg.getOtpSendInterval() != null && cfg.getOtpSendInterval() > 0 ? cfg.getOtpSendInterval() : 60;
        String sendKey = CacheConstants.OTP_SEND_KEY + type + ":" + normalized;
        if (redisCache.getCacheObject(sendKey) != null)
        {
            throw new ServiceException(MSG_SEND_COOLDOWN);
        }
        String code = generateCode();
        int expireMinutes = cfg.getOtpExpireMinutes() != null && cfg.getOtpExpireMinutes() > 0 ? cfg.getOtpExpireMinutes() : 5;
        String codeKey = otpCodeKey(type, normalized);
        redisCache.setCacheObject(codeKey, code, expireMinutes, TimeUnit.MINUTES);
        redisCache.setCacheObject(sendKey, "1", interval, TimeUnit.SECONDS);
        if (TYPE_EMAIL.equals(type))
        {
            emailSender.send(cfg, normalized, code, expireMinutes);
        }
        else
        {
            smsSender.send(cfg, normalized, code);
        }
    }

    public String loginByCode(String type, String target, String code)
    {
        if (StringUtils.isEmpty(code))
        {
            throw new ServiceException(MSG_CODE_EMPTY);
        }
        MallAuthConfig cfg = requireConfig();
        String normalized = normalizeTarget(type, target);
        assertChannelEnabled(cfg, type);
        String codeKey = otpCodeKey(type, normalized);
        String cached = redisCache.getCacheObject(codeKey);
        if (cached == null || !cached.equals(code.trim()))
        {
            throw new ServiceException(MSG_CODE_INVALID);
        }
        redisCache.deleteObject(codeKey);
        SysUser user = findUserByTarget(type, normalized);
        if (user == null)
        {
            user = createOtpUser(type, normalized);
        }
        if (!"0".equals(user.getStatus()))
        {
            throw new ServiceException(MSG_USER_DISABLED);
        }
        LoginUser loginUser = (LoginUser) userDetailsService.createLoginUser(user);
        recordLogin(user);
        return tokenService.createToken(loginUser);
    }

    private MallAuthConfig requireConfig()
    {
        MallAuthConfig cfg = authConfigMapper.selectMallAuthConfig();
        if (cfg == null)
        {
            throw new ServiceException("\u767b\u5f55\u914d\u7f6e\u672a\u521d\u59cb\u5316\uff0c\u8bf7\u6267\u884c sql/mall_auth_otp.sql");
        }
        return cfg;
    }

    private void assertChannelEnabled(MallAuthConfig cfg, String type)
    {
        if (TYPE_EMAIL.equals(type))
        {
            if (!"1".equals(cfg.getEmailEnabled()))
            {
                throw new ServiceException(MSG_EMAIL_DISABLED);
            }
        }
        else if (TYPE_PHONE.equals(type))
        {
            if (!"1".equals(cfg.getPhoneEnabled()))
            {
                throw new ServiceException(MSG_PHONE_DISABLED);
            }
        }
        else
        {
            throw new ServiceException(MSG_TYPE_INVALID);
        }
    }

    private String normalizeTarget(String type, String target)
    {
        if (StringUtils.isEmpty(target))
        {
            throw new ServiceException(MSG_TARGET_EMPTY);
        }
        String t = target.trim();
        if (TYPE_EMAIL.equals(type))
        {
            t = t.toLowerCase();
            if (!t.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"))
            {
                throw new ServiceException(MSG_EMAIL_INVALID);
            }
            return t;
        }
        if (TYPE_PHONE.equals(type))
        {
            if (!t.matches("^1[3-9]\\d{9}$"))
            {
                throw new ServiceException(MSG_PHONE_INVALID);
            }
            return t;
        }
        throw new ServiceException(MSG_TYPE_INVALID);
    }

    private SysUser findUserByTarget(String type, String target)
    {
        SysUser brief = TYPE_EMAIL.equals(type)
            ? userMapper.checkEmailUnique(target)
            : userMapper.checkPhoneUnique(target);
        if (brief == null)
        {
            return null;
        }
        return userService.selectUserById(brief.getUserId());
    }

    private SysUser createOtpUser(String type, String target)
    {
        String prefix = TYPE_EMAIL.equals(type) ? "em_" : "ph_";
        String base = TYPE_EMAIL.equals(type) ? target.split("@")[0].replaceAll("[^a-zA-Z0-9]", "") : target;
        if (StringUtils.isEmpty(base))
        {
            base = TYPE_EMAIL.equals(type) ? "user" : target;
        }
        String username = prefix + base;
        if (username.length() > 30)
        {
            username = prefix + base.substring(0, 30 - prefix.length());
        }
        int suffix = 1;
        String tryName = username;
        while (userMapper.checkUserNameUnique(tryName) != null)
        {
            tryName = username.substring(0, Math.min(26, username.length())) + suffix;
            suffix++;
        }
        SysUser user = new SysUser();
        user.setUserName(tryName);
        user.setNickName(TYPE_EMAIL.equals(type) ? target.split("@")[0] : target);
        if (TYPE_EMAIL.equals(type))
        {
            user.setEmail(target);
        }
        else
        {
            user.setPhonenumber(target);
        }
        user.setDeptId(105L);
        user.setPassword(SecurityUtils.encryptPassword(UUID.randomUUID().toString()));
        user.setStatus("0");
        user.setRoleIds(new Long[] { 2L });
        user.setCreateBy("otp");
        userService.insertUser(user);
        return userService.selectUserByUserName(tryName);
    }

    private void recordLogin(SysUser user)
    {
        user.setLoginIp(IpUtils.getIpAddr());
        user.setLoginDate(DateUtils.getNowDate());
        userService.updateUserProfile(user);
    }

    private static String otpCodeKey(String type, String target)
    {
        return CacheConstants.OTP_CODE_KEY + type + ":" + target;
    }

    private static String generateCode()
    {
        int n = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(n);
    }
}
