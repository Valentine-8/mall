-- Chat rich media: image / video message types
-- mysql -u root -p ry-vue < sql/mall_chat_media.sql

ALTER TABLE mall_chat_message
    ADD COLUMN msg_type VARCHAR(16) DEFAULT 'text' COMMENT 'text image video' AFTER sender_name;

UPDATE mall_chat_message SET msg_type = 'text' WHERE msg_type IS NULL OR msg_type = '';
