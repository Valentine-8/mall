-- C-end OTP login (email + phone stub)
-- Run after mall.sql: mysql -u root -p ry-vue < sql/mall_auth_otp.sql

CREATE TABLE IF NOT EXISTS mall_auth_config (
    config_id           BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT 'Config ID',
    email_enabled       CHAR(1)         DEFAULT '1'                COMMENT 'Email OTP enabled 0/1',
    smtp_host           VARCHAR(256)    DEFAULT ''                 COMMENT 'SMTP host',
    smtp_port           INT(11)         DEFAULT 465                COMMENT 'SMTP port',
    smtp_ssl            CHAR(1)         DEFAULT '1'                COMMENT 'Use SSL 0/1',
    smtp_user           VARCHAR(128)    DEFAULT ''                 COMMENT 'SMTP username',
    smtp_password       VARCHAR(256)    DEFAULT ''                 COMMENT 'SMTP password',
    email_from_name     VARCHAR(64)     DEFAULT ''                 COMMENT 'Sender display name',
    email_from_address  VARCHAR(128)    DEFAULT ''                 COMMENT 'Sender email address',
    email_subject       VARCHAR(128)    DEFAULT ''                 COMMENT 'Email subject',
    email_body_template VARCHAR(500)    DEFAULT ''                 COMMENT 'Body template with {code} {minutes}',
    email_mock          CHAR(1)         DEFAULT '1'                COMMENT 'Log code when SMTP unset 0/1',
    phone_enabled       CHAR(1)         DEFAULT '0'                COMMENT 'Phone OTP enabled 0/1',
    sms_mock            CHAR(1)         DEFAULT '1'                COMMENT 'Mock SMS when provider unset 0/1',
    sms_provider        VARCHAR(32)     DEFAULT ''                 COMMENT 'aliyun/tencent',
    sms_access_key      VARCHAR(128)    DEFAULT ''                 COMMENT 'SMS access key',
    sms_secret_key      VARCHAR(256)    DEFAULT ''                 COMMENT 'SMS secret',
    sms_sign            VARCHAR(64)     DEFAULT ''                 COMMENT 'SMS signature',
    sms_template_id     VARCHAR(64)     DEFAULT ''                 COMMENT 'SMS template ID',
    otp_expire_minutes  INT(11)         DEFAULT 5                  COMMENT 'Code TTL minutes',
    otp_send_interval   INT(11)         DEFAULT 60                 COMMENT 'Resend interval seconds',
    create_by           VARCHAR(64)     DEFAULT ''                 COMMENT 'Created by',
    create_time         DATETIME                                   COMMENT 'Created at',
    update_by           VARCHAR(64)     DEFAULT ''                 COMMENT 'Updated by',
    update_time         DATETIME                                   COMMENT 'Updated at',
    PRIMARY KEY (config_id)
) ENGINE=InnoDB COMMENT='Mall OTP auth config';

INSERT INTO mall_auth_config (
    config_id, email_enabled, smtp_port, smtp_ssl, email_from_name, email_subject,
    email_body_template, email_mock, phone_enabled, sms_mock, otp_expire_minutes, otp_send_interval, create_time
)
SELECT 1, '1', 465, '1', '\u56fd\u6e05\u5546\u57ce',
       '\u56fd\u6e05\u5546\u57ce\u767b\u5f55\u9a8c\u8bc1\u7801',
       '\u60a8\u7684\u9a8c\u8bc1\u7801\u662f{code}\uff0c{minutes}\u5206\u949f\u5185\u6709\u6548\u3002\u5982\u975e\u672c\u4eba\u64cd\u4f5c\u8bf7\u5ffd\u7565\u3002',
       '1', '0', '1', 5, 60, sysdate()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_auth_config WHERE config_id = 1);

INSERT INTO sys_menu SELECT '3006', '\u767b\u5f55\u914d\u7f6e', '3000', '6', 'authConfig', 'mall/authConfig/index', '', '', 1, 0, 'C', '0', '0', 'mall:auth:config', 'peoples', 'admin', sysdate(), '', null, 'OTP login settings' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '3006');

INSERT INTO sys_menu SELECT '3141', '\u914d\u7f6e\u67e5\u8be2', '3006', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:auth:query', '#', 'admin', sysdate(), '', null, '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '3141');
INSERT INTO sys_menu SELECT '3142', '\u914d\u7f6e\u4fee\u6539', '3006', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:auth:edit', '#', 'admin', sysdate(), '', null, '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '3142');

INSERT INTO sys_role_menu SELECT '1', '3006' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = '1' AND menu_id = '3006');
INSERT INTO sys_role_menu SELECT '1', '3141' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = '1' AND menu_id = '3141');
INSERT INTO sys_role_menu SELECT '1', '3142' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = '1' AND menu_id = '3142');
