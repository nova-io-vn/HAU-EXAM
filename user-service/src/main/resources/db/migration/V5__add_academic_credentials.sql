ALTER TABLE user_profiles
    ADD COLUMN academic_rank VARCHAR(16) NOT NULL DEFAULT 'NONE';

ALTER TABLE user_profiles
    ADD COLUMN academic_degree VARCHAR(16) NOT NULL DEFAULT 'NONE';
