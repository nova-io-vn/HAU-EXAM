ALTER TABLE auth_accounts ADD COLUMN role VARCHAR(32) NOT NULL DEFAULT 'USER';
ALTER TABLE auth_accounts ADD COLUMN faculty_id VARCHAR(50);
ALTER TABLE auth_accounts ADD COLUMN security_email VARCHAR(254);

ALTER TABLE auth_accounts ADD CONSTRAINT ck_auth_accounts_role
    CHECK (role IN ('SYSTEM_ADMIN', 'SUBJECT_ADMIN', 'USER'));

CREATE TABLE auth_processed_events (
    event_id UUID PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL
);
