CREATE TABLE vercel_analytics_settings (
    id UUID PRIMARY KEY,
    project_id VARCHAR(255) NOT NULL,
    team_id VARCHAR(255) NOT NULL,
    token_encrypted TEXT NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    updated_by UUID NOT NULL
);
