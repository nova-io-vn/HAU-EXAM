CREATE TABLE user_onboarding_states (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    role VARCHAR(32) NOT NULL,
    version_completed INTEGER NOT NULL DEFAULT 0,
    completed_at TIMESTAMP NULL,
    CONSTRAINT uk_user_onboarding_role UNIQUE (user_id, role)
);
