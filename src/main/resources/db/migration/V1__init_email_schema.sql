-- Email Service schema
-- Matches schema.md Section 5 (PostgreSQL 18+)

CREATE TABLE IF NOT EXISTS email_log (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    workflow_id     VARCHAR(50),
    assessment_id   UUID,
    recipient_email VARCHAR(255) NOT NULL,
    email_type      VARCHAR(50)  NOT NULL,
    subject         VARCHAR(255) NOT NULL,
    status          VARCHAR(50)  NOT NULL DEFAULT 'queued',
    sent_at         TIMESTAMPTZ,
    error_message   TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Idempotency: prevent duplicate emails for same workflow + recipient + type
CREATE UNIQUE INDEX uq_email_log_idempotency
    ON email_log(workflow_id, recipient_email, email_type);

CREATE INDEX idx_email_log_workflow_id ON email_log(workflow_id);
CREATE INDEX idx_email_log_assessment_id ON email_log(assessment_id);
CREATE INDEX idx_email_log_status ON email_log(status);
