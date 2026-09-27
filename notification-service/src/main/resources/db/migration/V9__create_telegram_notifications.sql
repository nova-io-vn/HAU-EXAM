CREATE TABLE telegram_bot_settings (
    id UUID PRIMARY KEY,
    bot_username VARCHAR(120) NOT NULL,
    bot_token_encrypted TEXT,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE telegram_connections (
    user_id UUID PRIMARY KEY,
    chat_id VARCHAR(80) NOT NULL UNIQUE,
    username VARCHAR(120),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    linked_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE telegram_link_tokens (
    token_hash VARCHAR(128) PRIMARY KEY,
    user_id UUID NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    consumed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE telegram_preferences (
    user_id UUID PRIMARY KEY,
    new_users BOOLEAN NOT NULL DEFAULT TRUE,
    actionable BOOLEAN NOT NULL DEFAULT TRUE,
    system_events BOOLEAN NOT NULL DEFAULT TRUE,
    login_events BOOLEAN NOT NULL DEFAULT FALSE,
    question_pending BOOLEAN NOT NULL DEFAULT TRUE,
    question_resubmitted BOOLEAN NOT NULL DEFAULT TRUE,
    subject_events BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
