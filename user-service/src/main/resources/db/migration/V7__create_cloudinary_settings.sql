CREATE TABLE cloudinary_settings (
    id UUID PRIMARY KEY,
    cloud_name VARCHAR(255) NOT NULL,
    api_key VARCHAR(255) NOT NULL,
    api_secret_encrypted TEXT NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    updated_by UUID NOT NULL
);
