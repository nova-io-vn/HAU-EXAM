CREATE TABLE question_assignments (
    id UUID PRIMARY KEY,
    faculty_id VARCHAR(50) NOT NULL,
    subject_id UUID NOT NULL REFERENCES subjects(id),
    chapter_id UUID REFERENCES chapters(id),
    topic_id UUID REFERENCES topics(id),
    knowledge_item_id UUID REFERENCES knowledge_items(id),
    lecturer_id UUID NOT NULL,
    assigned_by UUID NOT NULL,
    required_question_count INTEGER NOT NULL,
    required_easy INTEGER NOT NULL DEFAULT 0,
    required_medium INTEGER NOT NULL DEFAULT 0,
    required_hard INTEGER NOT NULL DEFAULT 0,
    deadline DATE NOT NULL,
    note VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_assignment_counts CHECK (
      required_question_count > 0 AND required_easy >= 0 AND required_medium >= 0 AND required_hard >= 0
      AND required_easy + required_medium + required_hard = required_question_count
    )
);

ALTER TABLE questions ADD COLUMN knowledge_item_id UUID REFERENCES knowledge_items(id);
ALTER TABLE questions ADD COLUMN assignment_id UUID REFERENCES question_assignments(id);
CREATE INDEX idx_question_assignments_lecturer ON question_assignments(lecturer_id, deadline);
CREATE INDEX idx_question_assignments_faculty ON question_assignments(faculty_id, deadline);
CREATE INDEX idx_questions_assignment ON questions(assignment_id, status, difficulty);
