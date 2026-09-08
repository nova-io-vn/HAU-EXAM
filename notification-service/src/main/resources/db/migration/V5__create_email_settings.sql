CREATE TABLE email_settings (
    id UUID PRIMARY KEY,
    smtp_host VARCHAR(255) NOT NULL,
    smtp_port INTEGER NOT NULL,
    smtp_username VARCHAR(254),
    smtp_password_encrypted TEXT,
    from_email VARCHAR(254) NOT NULL,
    from_name VARCHAR(160) NOT NULL,
    security VARCHAR(16) NOT NULL,
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
