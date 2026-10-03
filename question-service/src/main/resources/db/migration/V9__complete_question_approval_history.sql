ALTER TABLE question_review_history ADD COLUMN IF NOT EXISTS from_status VARCHAR(30);
ALTER TABLE question_review_history ADD COLUMN IF NOT EXISTS to_status VARCHAR(30);

ALTER TABLE question_review_history DROP CONSTRAINT IF EXISTS ck_review_action;
ALTER TABLE question_review_history
    ADD CONSTRAINT ck_review_action CHECK (action IN (
        'SUBMITTED', 'RESUBMITTED', 'APPROVED', 'REJECTED',
        'REVISION_REQUESTED', 'ARCHIVED', 'RESTORED'
    ));

ALTER TABLE question_review_history
    ADD CONSTRAINT ck_review_from_status CHECK (from_status IS NULL OR from_status IN (
        'DRAFT', 'PENDING_REVIEW', 'APPROVED', 'NEED_REVISION', 'REJECTED', 'ARCHIVED'
    ));
ALTER TABLE question_review_history
    ADD CONSTRAINT ck_review_to_status CHECK (to_status IS NULL OR to_status IN (
        'DRAFT', 'PENDING_REVIEW', 'APPROVED', 'NEED_REVISION', 'REJECTED', 'ARCHIVED'
    ));
