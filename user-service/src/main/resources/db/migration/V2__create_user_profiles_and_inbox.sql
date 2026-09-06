CREATE TABLE user_profiles (
    id UUID PRIMARY KEY,
    lecturer_code VARCHAR(50) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    date_of_birth DATE,
    phone VARCHAR(20),
    email VARCHAR(254) NOT NULL,
    address VARCHAR(500),
    avatar VARCHAR(1000),
    faculty_id VARCHAR(50),
    role VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_user_profiles_lecturer_code UNIQUE (lecturer_code),
    CONSTRAINT uk_user_profiles_email UNIQUE (email),
    CONSTRAINT ck_user_profiles_role CHECK (role IN ('SYSTEM_ADMIN', 'SUBJECT_ADMIN', 'USER')),
    CONSTRAINT ck_user_profiles_status CHECK (status IN ('PENDING_APPROVAL', 'ACTIVE', 'REJECTED', 'LOCKED'))
);
CREATE INDEX idx_user_profiles_role ON user_profiles (role);
CREATE INDEX idx_user_profiles_status ON user_profiles (status);
CREATE INDEX idx_user_profiles_faculty ON user_profiles (faculty_id);
CREATE TABLE processed_events (
    event_id UUID PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL
);
