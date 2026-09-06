CREATE TABLE device_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token VARCHAR(512) NOT NULL UNIQUE,
    platform VARCHAR(16) NOT NULL,
    device_identifier VARCHAR(255),
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_device_tokens_user_active ON device_tokens(user_id, active);
