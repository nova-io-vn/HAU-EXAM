CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES auth_accounts(id),
    token_hash VARCHAR(100) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_refresh_tokens_account ON refresh_tokens(account_id);
