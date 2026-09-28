ALTER TABLE subjects RENAME COLUMN faculty_id TO managing_faculty_id;

CREATE TABLE subject_faculty_scopes (
    id UUID PRIMARY KEY,
    subject_id UUID NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    faculty_id VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_subject_faculty_scope UNIQUE (subject_id, faculty_id)
);

INSERT INTO subject_faculty_scopes (id, subject_id, faculty_id, active)
SELECT id, id, managing_faculty_id, TRUE
FROM subjects;

CREATE INDEX ix_subject_faculty_scope_faculty_active
    ON subject_faculty_scopes(faculty_id, active);
CREATE INDEX ix_subject_faculty_scope_subject_active
    ON subject_faculty_scopes(subject_id, active);
