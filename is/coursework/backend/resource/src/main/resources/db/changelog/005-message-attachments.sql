CREATE TABLE message_attachment
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    message_id BIGINT NOT NULL,
    file_id    BIGINT NOT NULL,
    CONSTRAINT fk_message_attachment_message_id
        FOREIGN KEY (message_id) REFERENCES message (id) ON DELETE CASCADE,
    CONSTRAINT fk_message_attachment_file_id
        FOREIGN KEY (file_id) REFERENCES file (id) ON DELETE CASCADE,
    CONSTRAINT uk_message_attachment_message_file UNIQUE (message_id, file_id)
);

CREATE INDEX idx_message_attachment_message_id ON message_attachment (message_id);
CREATE INDEX idx_message_attachment_file_id ON message_attachment (file_id);

