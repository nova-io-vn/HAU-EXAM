CREATE TABLE knowledge_items (
    id UUID PRIMARY KEY,
    topic_id UUID NOT NULL,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    ordinal INTEGER NOT NULL DEFAULT 0,
    target_easy INTEGER NOT NULL DEFAULT 0,
    target_medium INTEGER NOT NULL DEFAULT 0,
    target_hard INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_knowledge_item_topic FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE,
    CONSTRAINT uq_knowledge_item_code UNIQUE (topic_id, code),
    CONSTRAINT ck_knowledge_item_targets CHECK (target_easy >= 0 AND target_medium >= 0 AND target_hard >= 0)
);

CREATE INDEX idx_knowledge_items_topic_ordinal ON knowledge_items(topic_id, ordinal, code);
