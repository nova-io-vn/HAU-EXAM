ALTER TABLE ai_settings ADD COLUMN persona VARCHAR(40) NOT NULL DEFAULT 'FRIENDLY';
ALTER TABLE ai_settings ADD COLUMN persona_instructions TEXT;
