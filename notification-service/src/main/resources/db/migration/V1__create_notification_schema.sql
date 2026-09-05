CREATE TABLE notifications (
 id UUID PRIMARY KEY,user_id UUID NOT NULL,type VARCHAR(64) NOT NULL,title VARCHAR(200) NOT NULL,content VARCHAR(2000) NOT NULL,
 reference_id VARCHAR(100),reference_type VARCHAR(64),is_read BOOLEAN NOT NULL DEFAULT FALSE,read_at TIMESTAMP WITH TIME ZONE,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL,CONSTRAINT ck_notification_read CHECK ((is_read=FALSE AND read_at IS NULL) OR is_read=TRUE)
);
CREATE INDEX idx_notifications_user_created ON notifications(user_id,created_at DESC);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id,is_read);
CREATE TABLE scheduled_notifications (
 id UUID PRIMARY KEY,target_role VARCHAR(32),target_faculty VARCHAR(50),title VARCHAR(200) NOT NULL,content VARCHAR(2000) NOT NULL,
 scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,status VARCHAR(32) NOT NULL,created_by UUID NOT NULL,created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 updated_at TIMESTAMP WITH TIME ZONE NOT NULL,version BIGINT NOT NULL DEFAULT 0,
 CONSTRAINT ck_scheduled_target CHECK (target_role IS NOT NULL OR target_faculty IS NOT NULL),
 CONSTRAINT ck_scheduled_status CHECK(status IN ('PENDING','PROCESSING','COMPLETED','FAILED'))
);
CREATE INDEX idx_scheduled_due ON scheduled_notifications(status,scheduled_at);
CREATE TABLE processed_events(event_id UUID PRIMARY KEY,event_type VARCHAR(100) NOT NULL,processed_at TIMESTAMP WITH TIME ZONE NOT NULL);
