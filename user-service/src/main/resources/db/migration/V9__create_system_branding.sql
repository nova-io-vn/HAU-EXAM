CREATE TABLE system_branding (
    id UUID PRIMARY KEY,
    system_name VARCHAR(120) NOT NULL,
    short_name VARCHAR(40) NOT NULL,
    logo_url VARCHAR(1000),
    favicon_url VARCHAR(1000),
    updated_by UUID,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
