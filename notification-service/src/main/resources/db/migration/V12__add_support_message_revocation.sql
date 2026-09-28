ALTER TABLE support_messages ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE support_messages ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE support_messages ADD COLUMN deleted_by UUID;
CREATE INDEX idx_support_messages_deleted ON support_messages(conversation_id, deleted);
