-- Customer service chat (AI + human agents)
-- Run after mall.sql: mysql -u root -p ry-vue < sql/mall_chat.sql

-- AI / chat settings (single row id=1)
CREATE TABLE IF NOT EXISTS mall_chat_config (
    config_id       BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT 'Config ID',
    ai_enabled      CHAR(1)         DEFAULT '1'                COMMENT 'AI enabled 0/1',
    ai_api_url      VARCHAR(512)    DEFAULT ''                 COMMENT 'OpenAI-compatible API URL',
    ai_api_key      VARCHAR(512)    DEFAULT ''                 COMMENT 'API key',
    ai_model        VARCHAR(128)    DEFAULT 'gpt-4o-mini'      COMMENT 'Model name',
    system_prompt   VARCHAR(2000)   DEFAULT ''                 COMMENT 'System prompt',
    welcome_message VARCHAR(500)    DEFAULT ''                 COMMENT 'Welcome message',
    transfer_keywords VARCHAR(500)  DEFAULT ''                 COMMENT 'Transfer keywords comma-separated',
    kb_enabled      CHAR(1)         DEFAULT '1'                COMMENT 'Knowledge base RAG 0/1',
    embedding_model VARCHAR(128)    DEFAULT 'text-embedding-v3' COMMENT 'Embedding model',
    rag_top_k       INT(11)         DEFAULT 3                  COMMENT 'Top K chunks',
    chunk_size      INT(11)         DEFAULT 500                COMMENT 'Chunk char size',
    chunk_overlap   INT(11)         DEFAULT 80                 COMMENT 'Chunk overlap',
    create_by       VARCHAR(64)     DEFAULT ''                 COMMENT 'Created by',
    create_time     DATETIME                                   COMMENT 'Created at',
    update_by       VARCHAR(64)     DEFAULT ''                 COMMENT 'Updated by',
    update_time     DATETIME                                   COMMENT 'Updated at',
    PRIMARY KEY (config_id)
) ENGINE=InnoDB COMMENT='Mall chat AI config';

INSERT INTO mall_chat_config (config_id, ai_enabled, ai_model, welcome_message, transfer_keywords, system_prompt, create_time)
SELECT 1, '1', 'gpt-4o-mini',
       '您好，我是国清商城智能客服，有什么可以帮您？如需人工请回复「转人工」。',
       '人工,转人工,客服,真人',
       '你是国清商城客服助手，简洁友好地回答商品、订单、退换货问题。无法处理时建议用户转人工。',
       sysdate()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_chat_config WHERE config_id = 1);

-- Agent seats (link sys_user)
CREATE TABLE IF NOT EXISTS mall_chat_agent (
    agent_id        BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT 'Agent ID',
    user_id         BIGINT(20)      NOT NULL                   COMMENT 'Sys user ID',
    nick_name       VARCHAR(64)     DEFAULT ''                 COMMENT 'Display name',
    status          CHAR(1)         DEFAULT '0'                COMMENT '0 offline 1 online',
    max_sessions    INT(11)         DEFAULT 5                  COMMENT 'Max concurrent sessions',
    active_sessions INT(11)         DEFAULT 0                  COMMENT 'Current active sessions',
    sort_order      INT(11)         DEFAULT 0                  COMMENT 'Assign priority',
    del_flag        CHAR(1)         DEFAULT '0'                COMMENT 'Delete flag',
    create_by       VARCHAR(64)     DEFAULT ''                 COMMENT 'Created by',
    create_time     DATETIME                                   COMMENT 'Created at',
    update_by       VARCHAR(64)     DEFAULT ''                 COMMENT 'Updated by',
    update_time     DATETIME                                   COMMENT 'Updated at',
    PRIMARY KEY (agent_id),
    UNIQUE KEY uk_chat_agent_user (user_id)
) ENGINE=InnoDB COMMENT='Mall chat agent seat';

-- Chat sessions
CREATE TABLE IF NOT EXISTS mall_chat_session (
    session_id      BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT 'Session ID',
    user_id         BIGINT(20)      NOT NULL                   COMMENT 'Buyer user ID',
    user_name       VARCHAR(64)     DEFAULT ''                 COMMENT 'Buyer account',
    agent_id        BIGINT(20)      DEFAULT NULL               COMMENT 'Assigned agent ID',
    agent_name      VARCHAR(64)     DEFAULT ''                 COMMENT 'Agent display name',
    status          CHAR(1)         DEFAULT '0'                COMMENT '0 AI 1 waiting 2 human 3 closed',
    last_message    VARCHAR(500)    DEFAULT ''                 COMMENT 'Last message preview',
    last_message_time DATETIME                                 COMMENT 'Last message time',
    create_time     DATETIME                                   COMMENT 'Created at',
    update_time     DATETIME                                   COMMENT 'Updated at',
    PRIMARY KEY (session_id),
    KEY idx_chat_session_user (user_id),
    KEY idx_chat_session_agent (agent_id),
    KEY idx_chat_session_status (status)
) ENGINE=InnoDB COMMENT='Mall chat session';

-- Chat messages
CREATE TABLE IF NOT EXISTS mall_chat_message (
    message_id      BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT 'Message ID',
    session_id      BIGINT(20)      NOT NULL                   COMMENT 'Session ID',
    sender_type     VARCHAR(16)     NOT NULL                   COMMENT 'user ai agent system',
    sender_id       BIGINT(20)      DEFAULT NULL               COMMENT 'Sender user ID',
    sender_name     VARCHAR(64)     DEFAULT ''                 COMMENT 'Sender name',
    msg_type        VARCHAR(16)     DEFAULT 'text'             COMMENT 'text image video',
    content         VARCHAR(2000)   NOT NULL                   COMMENT 'Content',
    create_time     DATETIME                                   COMMENT 'Created at',
    PRIMARY KEY (message_id),
    KEY idx_chat_message_session (session_id)
) ENGINE=InnoDB COMMENT='Mall chat message';

-- Menus (skip if already exists)
INSERT INTO sys_menu SELECT '3004', '客服会话', '3000', '4', 'chat', 'mall/chat/index', '', '', 1, 0, 'C', '0', '0', 'mall:chat:list', 'message', 'admin', sysdate(), '', null, 'Customer service sessions' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '3004');
INSERT INTO sys_menu SELECT '3005', '客服设置', '3000', '5', 'chatConfig', 'mall/chatConfig/index', '', '', 1, 0, 'C', '0', '0', 'mall:chat:config', 'edit', 'admin', sysdate(), '', null, 'AI and agent settings' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '3005');

INSERT INTO sys_menu SELECT '3131', '会话查询', '3004', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:chat:query', '#', 'admin', sysdate(), '', null, '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '3131');
INSERT INTO sys_menu SELECT '3132', '会话回复', '3004', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:chat:reply', '#', 'admin', sysdate(), '', null, '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '3132');
INSERT INTO sys_menu SELECT '3133', '会话关闭', '3004', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:chat:close', '#', 'admin', sysdate(), '', null, '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '3133');
INSERT INTO sys_menu SELECT '3134', '设置修改', '3005', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:chat:config', '#', 'admin', sysdate(), '', null, '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '3134');
INSERT INTO sys_menu SELECT '3135', '坐席管理', '3005', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:chat:agent', '#', 'admin', sysdate(), '', null, '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '3135');

INSERT INTO sys_role_menu SELECT '1', '3004' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = '1' AND menu_id = '3004');
INSERT INTO sys_role_menu SELECT '1', '3005' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = '1' AND menu_id = '3005');
INSERT INTO sys_role_menu SELECT '1', '3131' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = '1' AND menu_id = '3131');
INSERT INTO sys_role_menu SELECT '1', '3132' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = '1' AND menu_id = '3132');
INSERT INTO sys_role_menu SELECT '1', '3133' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = '1' AND menu_id = '3133');
INSERT INTO sys_role_menu SELECT '1', '3134' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = '1' AND menu_id = '3134');
INSERT INTO sys_role_menu SELECT '1', '3135' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = '1' AND menu_id = '3135');
