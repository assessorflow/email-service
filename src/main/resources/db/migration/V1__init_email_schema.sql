 CREATE TABLE IF NOT EXISTS email_log (
    id              BINARY(16)   PRIMARY KEY,
    workflow_id     VARCHAR(50),
    recipient_email VARCHAR(255) NOT NULL,
    email_type      VARCHAR(50)  NOT NULL,
    subject         VARCHAR(255) NOT NULL,
    status          VARCHAR(50)  NOT NULL DEFAULT 'QUEUED',
    sent_at         DATETIME,
    error_message   TEXT,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
