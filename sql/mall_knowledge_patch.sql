-- Idempotent patch when mall_knowledge.sql ALTER columns already exist
CREATE TABLE IF NOT EXISTS mall_knowledge_doc (
    doc_id          BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT 'Document ID',
    title           VARCHAR(200)    DEFAULT ''                 COMMENT 'Title',
    file_name       VARCHAR(255)    DEFAULT ''                 COMMENT 'Original filename',
    file_type       VARCHAR(32)     DEFAULT ''                 COMMENT 'txt md docx pdf',
    file_path       VARCHAR(500)    DEFAULT ''                 COMMENT 'Stored path',
    status          CHAR(1)         DEFAULT '0'                COMMENT '0 processing 1 ok 2 fail',
    chunk_count     INT(11)         DEFAULT 0                  COMMENT 'Chunk count',
    error_msg       VARCHAR(500)    DEFAULT ''                 COMMENT 'Error message',
    del_flag        CHAR(1)         DEFAULT '0'                COMMENT 'Delete flag',
    create_by       VARCHAR(64)     DEFAULT ''                 COMMENT 'Created by',
    create_time     DATETIME                                   COMMENT 'Created at',
    update_by       VARCHAR(64)     DEFAULT ''                 COMMENT 'Updated by',
    update_time     DATETIME                                   COMMENT 'Updated at',
    PRIMARY KEY (doc_id),
    KEY idx_kb_doc_status (status)
) ENGINE=InnoDB COMMENT='Mall knowledge document';

CREATE TABLE IF NOT EXISTS mall_knowledge_chunk (
    chunk_id        BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT 'Chunk ID',
    doc_id          BIGINT(20)      NOT NULL                   COMMENT 'Document ID',
    chunk_index     INT(11)         DEFAULT 0                  COMMENT 'Order in doc',
    content         TEXT                                       COMMENT 'Chunk text',
    keywords        VARCHAR(500)    DEFAULT ''                 COMMENT 'Keywords for fallback search',
    embedding       MEDIUMTEXT                                 COMMENT 'Embedding JSON array',
    create_time     DATETIME                                   COMMENT 'Created at',
    PRIMARY KEY (chunk_id),
    KEY idx_kb_chunk_doc (doc_id)
) ENGINE=InnoDB COMMENT='Mall knowledge chunk';

INSERT INTO sys_menu SELECT '3006', '知识库', '3000', '6', 'knowledge', 'mall/knowledge/index', '', '', 1, 0, 'C', '0', '0', 'mall:knowledge:list', 'documentation', 'admin', sysdate(), '', null, 'Knowledge base RAG' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '3006');

INSERT INTO sys_menu SELECT '3141', '文档查询', '3006', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:knowledge:query', '#', 'admin', sysdate(), '', null, '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '3141');
INSERT INTO sys_menu SELECT '3142', '文档上传', '3006', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:knowledge:upload', '#', 'admin', sysdate(), '', null, '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '3142');
INSERT INTO sys_menu SELECT '3143', '文档删除', '3006', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:knowledge:remove', '#', 'admin', sysdate(), '', null, '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '3143');
INSERT INTO sys_menu SELECT '3144', '重新索引', '3006', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:knowledge:reindex', '#', 'admin', sysdate(), '', null, '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '3144');

INSERT INTO sys_role_menu SELECT '1', '3006' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = '1' AND menu_id = '3006');
INSERT INTO sys_role_menu SELECT '1', '3141' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = '1' AND menu_id = '3141');
INSERT INTO sys_role_menu SELECT '1', '3142' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = '1' AND menu_id = '3142');
INSERT INTO sys_role_menu SELECT '1', '3143' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = '1' AND menu_id = '3143');
INSERT INTO sys_role_menu SELECT '1', '3144' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = '1' AND menu_id = '3144');
