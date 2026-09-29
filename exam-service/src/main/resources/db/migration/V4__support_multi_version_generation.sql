ALTER TABLE exam_matrix_rules ADD COLUMN knowledge_item_id UUID;

ALTER TABLE exams ADD COLUMN version_start_code INTEGER NOT NULL DEFAULT 1;
ALTER TABLE exams ADD COLUMN version_end_code INTEGER NOT NULL DEFAULT 1;
ALTER TABLE exams ADD COLUMN shuffle_questions BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE exams ADD COLUMN shuffle_answers BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE exams ADD COLUMN allow_question_replacement BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE exams ADD COLUMN reuse_warning BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE exam_versions ADD COLUMN created_by UUID;
UPDATE exam_versions
SET created_by = (SELECT exams.created_by FROM exams WHERE exams.id = exam_versions.exam_id);
ALTER TABLE exam_versions ALTER COLUMN created_by SET NOT NULL;
ALTER TABLE exam_versions ADD COLUMN generation_seed BIGINT NOT NULL DEFAULT 0;
ALTER TABLE exam_versions ADD COLUMN question_count INTEGER NOT NULL DEFAULT 0;
UPDATE exam_versions
SET question_count = (SELECT COUNT(*) FROM exam_question_references
                      WHERE exam_question_references.exam_version_id = exam_versions.id);

CREATE TABLE exam_version_options (
    id UUID PRIMARY KEY,
    exam_question_reference_id UUID NOT NULL REFERENCES exam_question_references(id) ON DELETE CASCADE,
    option_id UUID NOT NULL,
    display_order INTEGER NOT NULL,
    CONSTRAINT uk_exam_question_option UNIQUE (exam_question_reference_id, option_id),
    CONSTRAINT uk_exam_question_option_order UNIQUE (exam_question_reference_id, display_order),
    CONSTRAINT ck_exam_option_order CHECK (display_order > 0)
);

CREATE INDEX idx_exam_version_option_question ON exam_version_options(exam_question_reference_id, display_order);
