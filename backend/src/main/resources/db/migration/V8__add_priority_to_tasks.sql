-- Add priority column to tasks table
-- This column stores the priority level of a task
-- Valid values: LOW, MEDIUM, HIGH, CRITICAL
-- Default: LOW (for backward compatibility with existing tasks)

ALTER TABLE daily_track.tasks
ADD COLUMN IF NOT EXISTS priority VARCHAR(10) NOT NULL DEFAULT 'LOW';

-- Create index for potential future filtering by priority
CREATE INDEX IF NOT EXISTS idx_tasks_priority ON daily_track.tasks (priority);
