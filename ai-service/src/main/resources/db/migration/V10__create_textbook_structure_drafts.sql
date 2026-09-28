CREATE TABLE textbook_structure_drafts (
    id UUID PRIMARY KEY,
    subject_id UUID NOT NULL,
    owner_id UUID NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    extracted_text TEXT NOT NULL,
    structure_json TEXT NOT NULL,
    processing_error VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_textbook_draft_status CHECK (status IN ('PROCESSING','READY','FAILED','CONFIRMED'))
);
CREATE INDEX idx_textbook_drafts_subject ON textbook_structure_drafts(subject_id, created_at);
