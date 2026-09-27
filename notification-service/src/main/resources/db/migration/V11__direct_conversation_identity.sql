-- Reconcile legacy duplicate direct conversations before enforcing uniqueness.
CREATE TEMP TABLE support_conversation_merge AS
SELECT id,
       first_value(id) OVER (
         PARTITION BY LEAST(created_by_user_id, assigned_admin_id), GREATEST(created_by_user_id, assigned_admin_id)
         ORDER BY created_at ASC, id::text ASC
       ) AS canonical_id
FROM support_conversations
WHERE assigned_admin_id IS NOT NULL;

-- Preserve the complete activity window on the canonical row before removing
-- duplicate conversation shells. Messages (and therefore attachments/read state)
-- are reassigned below and remain untouched.
UPDATE support_conversations canonical
SET created_at = activity.first_created_at,
    updated_at = activity.last_updated_at,
    last_message_at = activity.last_message_at
FROM (
  SELECT merge.canonical_id,
         min(conversation.created_at) AS first_created_at,
         max(conversation.updated_at) AS last_updated_at,
         max(conversation.last_message_at) AS last_message_at
  FROM support_conversation_merge merge
  JOIN support_conversations conversation ON conversation.id = merge.id
  GROUP BY merge.canonical_id
) activity
WHERE canonical.id = activity.canonical_id;

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
