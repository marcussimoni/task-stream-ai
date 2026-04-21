-- Create pending_alarms table for alarm notification system
-- Tracks alarm state for resilience across browser refreshes and server restarts

CREATE TABLE IF NOT EXISTS daily_track.pending_alarms (
    id VARCHAR(36) PRIMARY KEY,
    schedule_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    scheduled_time TIMESTAMP NOT NULL,
    emitted_at TIMESTAMP,
    acknowledged_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pending_alarms_schedule FOREIGN KEY (schedule_id) 
        REFERENCES daily_track.week_schedules (id) ON DELETE CASCADE,
    CONSTRAINT chk_pending_alarms_type CHECK (type IN ('PRE_REMINDER', 'START_ALARM'))
);

-- Index for efficient time-based queries
CREATE INDEX IF NOT EXISTS idx_pending_alarms_time ON daily_track.pending_alarms (scheduled_time);

-- Index for unacknowledged alarms (for reconnection sync)
CREATE INDEX IF NOT EXISTS idx_pending_alarms_acknowledged ON daily_track.pending_alarms (acknowledged_at);
