-- Metadata columns are already present in V6; this migration documents the invariant for upgraded databases.
ALTER TABLE support_attachments ALTER COLUMN file_name SET NOT NULL;
ALTER TABLE support_attachments ALTER COLUMN content_type SET NOT NULL;
ALTER TABLE support_attachments ALTER COLUMN file_size SET NOT NULL;
