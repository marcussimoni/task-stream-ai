-- Add custom_end_date_selected column to tasks table
-- This column tracks whether the user has selected a custom end date for the task

ALTER TABLE daily_track.tasks
ADD COLUMN IF NOT EXISTS custom_end_date_selected BOOLEAN NOT NULL DEFAULT FALSE;
