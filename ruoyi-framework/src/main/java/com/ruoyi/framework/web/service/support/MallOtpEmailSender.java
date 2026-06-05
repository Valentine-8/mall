package com.ruoyi.framework.web.service.support;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mall.domain.MallAuthConfig;
import jakarta.mail.internet.MimeMessage;

@Component
public class MallOtpEmailSender
{
    private static final Logger log = LoggerFactory.getLogger(MallOtpEmailSender.class);

    public boolean isSmtpConfigured(MallAuthConfig config)
    {
        return config != null
            && StringUtils.isNotEmpty(config.getSmtpHost())
            && StringUtils.isNotEmpty(config.getSmtpUser())
            && StringUtils.isNotEmpty(config.getSmtpPassword());
    }

    public void send(MallAuthConfig config, String to, String code, int expireMinutes)
    {
        if (!isSmtpConfigured(config))
        {
            if ("1".equals(config.getEmailMock()))
            {
                log.warn("[OTP email mock] to={} code={} (configure SMTP in mall auth settings to send real mail)", to, code);
                return;
            }
            throw new ServiceException("\u90ae\u7bb1\u53d1\u9001\u672a\u914d\u7f6e\uff0c\u8bf7\u5728\u540e\u53f0\u914d\u7f6e SMTP");
        }
        String fromName = StringUtils.isNotEmpty(config.getEmailFromName()) ? config.getEmailFromName() : "\u56fd\u6e05\u5546\u57ce";
        String fromAddress = StringUtils.isNotEmpty(config.getEmailFromAddress()) ? config.getEmailFromAddress() : config.getSmtpUser();
        String subject = StringUtils.isNotEmpty(config.getEmailSubject()) ? config.getEmailSubject() : "\u767b\u5f55\u9a8c\u8bc1\u7801";
        String template = StringUtils.isNotEmpty(config.getEmailBodyTemplate())
            ? config.getEmailBodyTemplate()
            : "\u60a8\u7684\u9a8c\u8bc1\u7801\u662f{code}\uff0c{minutes}\u5206\u949f\u5185\u6709\u6548\u3002";
        String body = template.replace("{code}", code).replace("{minutes}", String.valueOf(expireMinutes));
        try
        {
            JavaMailSenderImpl sender = buildSender(config);
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            sender.send(message);
        }
        catch (Exception e)
        {
            log.error("OTP email send failed to {}", to, e);
            throw new ServiceException("\u9a8c\u8bc1\u7801\u90ae\u4ef6\u53d1\u9001\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5");
        }
    }

    private JavaMailSenderImpl buildSender(MallAuthConfig config)
    {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(config.getSmtpHost());
        sender.setPort(config.getSmtpPort() != null ? config.getSmtpPort() : 465);
        sender.setUsername(config.getSmtpUser());
        sender.setPassword(config.getSmtpPassword());
        sender.setDefaultEncoding("UTF-8");
        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        boolean ssl = !"0".equals(config.getSmtpSsl());
        if (ssl)
        {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.port", String.valueOf(sender.getPort()));
        }
        else
        {
            props.put("mail.smtp.starttls.enable", "true");
        }
        return sender;
    }
}
