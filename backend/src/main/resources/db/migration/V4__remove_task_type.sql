-- Remove task_type_id column and task_types table
-- This migration removes the redundant task_type in favor of using tags only

-- Drop foreign key constraint first
ALTER TABLE daily_track.tasks DROP CONSTRAINT IF EXISTS fk_tasks_task_type;

-- Drop index on task_type_id
DROP INDEX IF EXISTS daily_track.idx_tasks_task_type_id;

-- Drop task_type_id column from tasks table
ALTER TABLE daily_track.tasks DROP COLUMN IF EXISTS task_type_id;

-- Drop index on task_types name
DROP INDEX IF EXISTS daily_track.idx_task_types_name;

-- Drop task_types table
DROP TABLE IF EXISTS daily_track.task_types;
