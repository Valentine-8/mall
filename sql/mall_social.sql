-- 第三方登录绑定表（微信 / 支付宝）
USE `ry-vue`;

DROP TABLE IF EXISTS sys_social_bind;
CREATE TABLE sys_social_bind (
  bind_id        bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '绑定ID',
  user_id        bigint(20)   NOT NULL                COMMENT '用户ID',
  social_type    varchar(20)  NOT NULL                COMMENT '类型 wechat/alipay',
  open_id        varchar(128) NOT NULL                COMMENT '第三方平台用户唯一标识',
  union_id       varchar(128) DEFAULT NULL             COMMENT '微信unionid',
  nickname       varchar(64)  DEFAULT ''              COMMENT '昵称',
  avatar         varchar(255) DEFAULT ''              COMMENT '头像',
  create_time    datetime                               COMMENT '创建时间',
  PRIMARY KEY (bind_id),
  UNIQUE KEY uk_type_open (social_type, open_id)
) ENGINE=InnoDB COMMENT='第三方账号绑定';
