CREATE TABLE auth_accounts (
    id UUID PRIMARY KEY,
    lecturer_code VARCHAR(50) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_auth_accounts_lecturer_code UNIQUE (lecturer_code),
    CONSTRAINT ck_auth_accounts_status CHECK (
        status IN ('PENDING_APPROVAL', 'ACTIVE', 'REJECTED', 'LOCKED')
    )
);

CREATE INDEX idx_auth_accounts_status ON auth_accounts (status);
