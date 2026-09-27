CREATE TABLE subject_lecturer_assignments (
    id UUID PRIMARY KEY,
    subject_id UUID NOT NULL REFERENCES subjects(id),
    user_id UUID NOT NULL,
    assigned_by UUID NOT NULL,
    assigned_at TIMESTAMP WITH TIME ZONE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_subject_lecturer_assignment UNIQUE (subject_id, user_id)
);
CREATE INDEX ix_subject_lecturer_subject_active ON subject_lecturer_assignments(subject_id, active);
CREATE INDEX ix_subject_lecturer_user_active ON subject_lecturer_assignments(user_id, active);
