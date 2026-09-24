CREATE TABLE support_conversations (
    id UUID PRIMARY KEY,
    created_by_user_id UUID NOT NULL,
    created_by_role VARCHAR(32) NOT NULL,
    faculty_id VARCHAR(80),
    subject VARCHAR(200) NOT NULL,
    status VARCHAR(24) NOT NULL,
    assigned_admin_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_message_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_support_conversations_creator ON support_conversations(created_by_user_id, updated_at DESC);
CREATE INDEX idx_support_conversations_status ON support_conversations(status, last_message_at DESC);
CREATE TABLE support_messages (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES support_conversations(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL,
    sender_role VARCHAR(32) NOT NULL,
    content TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    edited_at TIMESTAMP WITH TIME ZONE,
    read_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT ck_support_message_content CHECK (content IS NULL OR length(trim(content)) > 0)
);
CREATE INDEX idx_support_messages_conversation ON support_messages(conversation_id, created_at DESC);
CREATE TABLE support_attachments (
    id UUID PRIMARY KEY,
    message_id UUID NOT NULL REFERENCES support_messages(id) ON DELETE CASCADE,
    type VARCHAR(24) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(80) NOT NULL,
    file_size BIGINT NOT NULL,
    url VARCHAR(1200) NOT NULL,
    public_id VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_support_attachments_message ON support_attachments(message_id);
