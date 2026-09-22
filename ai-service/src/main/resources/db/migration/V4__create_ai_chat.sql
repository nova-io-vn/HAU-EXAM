CREATE TABLE ai_chat_conversations(
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_ai_chat_conversations_user_updated ON ai_chat_conversations(user_id, updated_at DESC);
CREATE TABLE ai_chat_messages(
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES ai_chat_conversations(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_ai_chat_message_role CHECK(role IN ('USER','ASSISTANT','SYSTEM'))
);
CREATE INDEX idx_ai_chat_messages_conversation_created ON ai_chat_messages(conversation_id, created_at);
CREATE TABLE ai_chat_attachments(
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES ai_chat_conversations(id) ON DELETE CASCADE,
    document_id UUID NOT NULL REFERENCES documents(id),
    attached_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_ai_chat_attachment UNIQUE(conversation_id, document_id)
);
