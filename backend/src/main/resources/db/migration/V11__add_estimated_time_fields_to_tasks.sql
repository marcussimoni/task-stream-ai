-- Add estimated time fields to tasks table
-- All fields are nullable for production database compatibility

ALTER TABLE daily_track.tasks ADD COLUMN total_word_count INTEGER NULL;
ALTER TABLE daily_track.tasks ADD COLUMN technical_depth VARCHAR(10) NULL;
ALTER TABLE daily_track.tasks ADD COLUMN estimated_reading_time_minutes INTEGER NULL;
ALTER TABLE daily_track.tasks ADD COLUMN depth_justification VARCHAR(500) NULL;
ALTER TABLE daily_track.tasks ADD COLUMN recommended_pace VARCHAR(200) NULL;
