--liquibase formatted sql

--changeset opencode:004-add-unread-count-to-conversation-participants
--preconditions onFail:MARK_RAN
--precondition-not columnExists tableName:conversation_participants columnName:unread_count
ALTER TABLE conversation_participants
    ADD COLUMN unread_count INT NOT NULL DEFAULT 0;

--rollback ALTER TABLE conversation_participants DROP COLUMN unread_count;

--changeset opencode:005-add-last-read-message-id-to-conversation-participants
--preconditions onFail:MARK_RAN
--precondition-not columnExists tableName:conversation_participants columnName:last_read_message_id
ALTER TABLE conversation_participants
    ADD COLUMN last_read_message_id BIGINT NULL;

--rollback ALTER TABLE conversation_participants DROP COLUMN last_read_message_id;

--changeset opencode:006-add-last-read-at-to-conversation-participants
--preconditions onFail:MARK_RAN
--precondition-not columnExists tableName:conversation_participants columnName:last_read_at
ALTER TABLE conversation_participants
    ADD COLUMN last_read_at DATETIME NULL;

--rollback ALTER TABLE conversation_participants DROP COLUMN last_read_at;

--changeset opencode:007-add-fk-last-read-message-to-conversation-participants
--preconditions onFail:MARK_RAN
--precondition-tableExists tableName:conversation_participants
--precondition-tableExists tableName:messages
--precondition-column-exists tableName:conversation_participants columnName:last_read_message_id
--precondition-not foreignKeyConstraintExists foreignKeyName:fk_cp_last_read_message
ALTER TABLE conversation_participants
    ADD CONSTRAINT fk_cp_last_read_message
        FOREIGN KEY (last_read_message_id) REFERENCES messages (id)
            ON DELETE SET NULL;

--rollback ALTER TABLE conversation_participants DROP FOREIGN KEY fk_cp_last_read_message;

--changeset opencode:008-add-index-unread-count-to-conversation-participants
--preconditions onFail:MARK_RAN
--precondition-not indexExists tableName:conversation_participants indexName:idx_cp_user_unread
CREATE INDEX idx_cp_user_unread
    ON conversation_participants (user_id, unread_count);

--rollback DROP INDEX idx_cp_user_unread ON conversation_participants;
