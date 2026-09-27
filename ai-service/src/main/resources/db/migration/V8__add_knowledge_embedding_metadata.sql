ALTER TABLE ai_knowledge_documents ADD COLUMN IF NOT EXISTS embedding_provider VARCHAR(80) NOT NULL DEFAULT 'LOCAL_HASH';
ALTER TABLE ai_knowledge_documents ADD COLUMN IF NOT EXISTS embedding_model VARCHAR(160) NOT NULL DEFAULT 'unicode-token-v1';
ALTER TABLE ai_knowledge_documents ADD COLUMN IF NOT EXISTS embedding_dimension INT NOT NULL DEFAULT 128;
ALTER TABLE ai_knowledge_documents ADD COLUMN IF NOT EXISTS embedding_normalization VARCHAR(40) NOT NULL DEFAULT 'L2';
ALTER TABLE ai_knowledge_chunks ADD COLUMN IF NOT EXISTS heading VARCHAR(500);
ALTER TABLE ai_knowledge_chunks ADD COLUMN IF NOT EXISTS page_number INT;
