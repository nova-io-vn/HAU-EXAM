-- Reconcile legacy duplicate direct conversations before enforcing uniqueness.
CREATE TEMP TABLE support_conversation_merge ON COMMIT DROP AS
SELECT id,
       min(id) OVER (PARTITION BY LEAST(created_by_user_id, assigned_admin_id), GREATEST(created_by_user_id, assigned_admin_id)) AS canonical_id
FROM support_conversations
WHERE assigned_admin_id IS NOT NULL;

UPDATE support_messages m
SET conversation_id = x.canonical_id
FROM support_conversation_merge x
WHERE m.conversation_id = x.id
  AND x.id <> x.canonical_id;

DELETE FROM support_conversations
WHERE id IN (SELECT id FROM support_conversation_merge WHERE id <> canonical_id);

ALTER TABLE support_conversations ADD COLUMN IF NOT EXISTS deleted_at_creator TIMESTAMP WITH TIME ZONE;
ALTER TABLE support_conversations ADD COLUMN IF NOT EXISTS deleted_at_admin TIMESTAMP WITH TIME ZONE;
ALTER TABLE support_conversations ADD COLUMN IF NOT EXISTS direct_participant_key TEXT;

UPDATE support_conversations
SET direct_participant_key = CASE WHEN assigned_admin_id IS NULL THEN NULL
  ELSE LEAST(created_by_user_id, assigned_admin_id)::text || ':' || GREATEST(created_by_user_id, assigned_admin_id)::text
END
WHERE assigned_admin_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_support_direct_participant_key
  ON support_conversations(direct_participant_key);
CREATE INDEX IF NOT EXISTS idx_support_conversations_direct_lookup
  ON support_conversations(created_by_user_id, assigned_admin_id, last_message_at DESC);
