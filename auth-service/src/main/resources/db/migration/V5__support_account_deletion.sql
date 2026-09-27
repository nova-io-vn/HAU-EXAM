ALTER TABLE auth_accounts DROP CONSTRAINT IF EXISTS ck_auth_accounts_status;
ALTER TABLE auth_accounts ADD CONSTRAINT ck_auth_accounts_status CHECK (
  status IN ('PENDING_APPROVAL', 'ACTIVE', 'REJECTED', 'LOCKED', 'DELETED')
);
