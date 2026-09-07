CREATE TABLE contact_requests (
    id UUID PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    email VARCHAR(254) NOT NULL,
    subject VARCHAR(200) NOT NULL,
    message VARCHAR(5000) NOT NULL,
    faculty_id VARCHAR(80),
    status VARCHAR(24) NOT NULL,
    admin_note VARCHAR(5000),
    reply_message VARCHAR(5000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    replied_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_contact_requests_status ON contact_requests(status);
