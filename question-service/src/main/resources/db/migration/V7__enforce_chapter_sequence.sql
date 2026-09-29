-- Normalize legacy/non-positive/duplicate chapter order before enforcing the invariant.
CREATE TEMPORARY TABLE chapter_sequence_rank AS
SELECT id, ROW_NUMBER() OVER (PARTITION BY subject_id ORDER BY ordinal_number, code, id) AS next_ordinal
FROM chapters;

UPDATE chapters
SET ordinal_number = (SELECT next_ordinal FROM chapter_sequence_rank r WHERE r.id = chapters.id),
    updated_at = CURRENT_TIMESTAMP;

DROP TABLE chapter_sequence_rank;

ALTER TABLE chapters
    ADD CONSTRAINT uk_chapter_subject_ordinal UNIQUE (subject_id, ordinal_number);
