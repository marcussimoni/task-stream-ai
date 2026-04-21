-- Add completed column to habits table
-- This column marks if a habit has been accomplished/archived (completed=true) or is still active (completed=false)
-- Default is true for existing habits as they are considered already accomplished

ALTER TABLE daily_track.habits
ADD COLUMN IF NOT EXISTS completed BOOLEAN NOT NULL DEFAULT FALSE;
