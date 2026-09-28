ALTER TABLE user_profiles DROP CONSTRAINT IF EXISTS ck_user_profiles_status;
ALTER TABLE user_profiles ADD CONSTRAINT ck_user_profiles_status CHECK (
  status IN ('PENDING_APPROVAL', 'ACTIVE', 'REJECTED', 'LOCKED', 'DELETED')
);
