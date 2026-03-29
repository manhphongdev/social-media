CREATE TABLE message_reads
(
    message_id BIGINT   NOT NULL,
    user_id    BIGINT   NOT NULL,
    read_at    DATETIME NOT NULL,
    CONSTRAINT pk_message_reads PRIMARY KEY (message_id, user_id),
    CONSTRAINT fk_message_reads_message FOREIGN KEY (message_id) REFERENCES messages (id) ON DELETE CASCADE,
    CONSTRAINT fk_message_reads_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_message_reads_user_read_at
    ON message_reads (user_id, read_at);

CREATE INDEX idx_message_reads_message_read_at
    ON message_reads (message_id, read_at);

