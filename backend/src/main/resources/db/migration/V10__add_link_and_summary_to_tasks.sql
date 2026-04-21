-- Add link and summary columns to tasks table
-- link: nullable URL field (max 300 chars)
-- summary: nullable text field for blog/site summaries (100-200 words)
-- Using separate statements for H2 compatibility

ALTER TABLE daily_track.tasks ADD COLUMN link VARCHAR(300) NULL;
ALTER TABLE daily_track.tasks ADD COLUMN summary TEXT NULL;
