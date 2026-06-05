package com.ruoyi.mall.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class MallAuthConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long configId;
    private String emailEnabled;
    private String smtpHost;
    private Integer smtpPort;
    private String smtpSsl;
    private String smtpUser;
    private String smtpPassword;
    private String emailFromName;
    private String emailFromAddress;
    private String emailSubject;
    private String emailBodyTemplate;
    private String emailMock;
    private String phoneEnabled;
    private String smsMock;
    private String smsProvider;
    private String smsAccessKey;
    private String smsSecretKey;
    private String smsSign;
    private String smsTemplateId;
    private Integer otpExpireMinutes;
    private Integer otpSendInterval;

    public Long getConfigId() { return configId; }
    public void setConfigId(Long configId) { this.configId = configId; }
    public String getEmailEnabled() { return emailEnabled; }
    public void setEmailEnabled(String emailEnabled) { this.emailEnabled = emailEnabled; }
    public String getSmtpHost() { return smtpHost; }
    public void setSmtpHost(String smtpHost) { this.smtpHost = smtpHost; }
    public Integer getSmtpPort() { return smtpPort; }
    public void setSmtpPort(Integer smtpPort) { this.smtpPort = smtpPort; }
    public String getSmtpSsl() { return smtpSsl; }
    public void setSmtpSsl(String smtpSsl) { this.smtpSsl = smtpSsl; }
    public String getSmtpUser() { return smtpUser; }
    public void setSmtpUser(String smtpUser) { this.smtpUser = smtpUser; }
    public String getSmtpPassword() { return smtpPassword; }
    public void setSmtpPassword(String smtpPassword) { this.smtpPassword = smtpPassword; }
    public String getEmailFromName() { return emailFromName; }
    public void setEmailFromName(String emailFromName) { this.emailFromName = emailFromName; }
    public String getEmailFromAddress() { return emailFromAddress; }
    public void setEmailFromAddress(String emailFromAddress) { this.emailFromAddress = emailFromAddress; }
    public String getEmailSubject() { return emailSubject; }
    public void setEmailSubject(String emailSubject) { this.emailSubject = emailSubject; }
    public String getEmailBodyTemplate() { return emailBodyTemplate; }
    public void setEmailBodyTemplate(String emailBodyTemplate) { this.emailBodyTemplate = emailBodyTemplate; }
    public String getEmailMock() { return emailMock; }
    public void setEmailMock(String emailMock) { this.emailMock = emailMock; }
    public String getPhoneEnabled() { return phoneEnabled; }
    public void setPhoneEnabled(String phoneEnabled) { this.phoneEnabled = phoneEnabled; }
    public String getSmsMock() { return smsMock; }
    public void setSmsMock(String smsMock) { this.smsMock = smsMock; }
    public String getSmsProvider() { return smsProvider; }
    public void setSmsProvider(String smsProvider) { this.smsProvider = smsProvider; }
    public String getSmsAccessKey() { return smsAccessKey; }
    public void setSmsAccessKey(String smsAccessKey) { this.smsAccessKey = smsAccessKey; }
    public String getSmsSecretKey() { return smsSecretKey; }
    public void setSmsSecretKey(String smsSecretKey) { this.smsSecretKey = smsSecretKey; }
    public String getSmsSign() { return smsSign; }
    public void setSmsSign(String smsSign) { this.smsSign = smsSign; }
    public String getSmsTemplateId() { return smsTemplateId; }
    public void setSmsTemplateId(String smsTemplateId) { this.smsTemplateId = smsTemplateId; }
    public Integer getOtpExpireMinutes() { return otpExpireMinutes; }
    public void setOtpExpireMinutes(Integer otpExpireMinutes) { this.otpExpireMinutes = otpExpireMinutes; }
    public Integer getOtpSendInterval() { return otpSendInterval; }
    public void setOtpSendInterval(Integer otpSendInterval) { this.otpSendInterval = otpSendInterval; }
}
