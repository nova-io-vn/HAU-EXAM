ALTER TABLE exams ADD COLUMN exam_code VARCHAR(50);
UPDATE exams SET exam_code = 'EXAM-' || UPPER(SUBSTRING(REPLACE(id::text, '-', '') FROM 1 FOR 8)) WHERE exam_code IS NULL;
ALTER TABLE exams ALTER COLUMN exam_code SET NOT NULL;
ALTER TABLE exams ADD COLUMN duration_minutes INTEGER NOT NULL DEFAULT 60;
ALTER TABLE exams ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE exams ADD CONSTRAINT ck_exam_duration CHECK (duration_minutes BETWEEN 1 AND 600);
CREATE UNIQUE INDEX uk_exam_faculty_code ON exams(faculty_id, exam_code);
