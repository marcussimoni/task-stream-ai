-- Create tags table
CREATE TABLE IF NOT EXISTS daily_track.tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    color VARCHAR(7) NOT NULL DEFAULT '#3B82F6',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create task_types table
CREATE TABLE IF NOT EXISTS daily_track.task_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    color VARCHAR(7) NOT NULL DEFAULT '#3B82F6',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create habits table
CREATE TABLE IF NOT EXISTS daily_track.habits (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    current_streak INTEGER NOT NULL DEFAULT 0,
    last_completed_date DATE
);

-- Create habit_tags join table for ManyToMany relationship
CREATE TABLE IF NOT EXISTS daily_track.habit_tags (
    habit_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (habit_id, tag_id),
    CONSTRAINT fk_habit_tags_habit FOREIGN KEY (habit_id) 
        REFERENCES daily_track.habits (id) ON DELETE CASCADE,
    CONSTRAINT fk_habit_tags_tag FOREIGN KEY (tag_id) 
        REFERENCES daily_track.tags (id) ON DELETE CASCADE
);

-- Create habit_entries table
CREATE TABLE IF NOT EXISTS daily_track.habit_entries (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL,
    completed BOOLEAN NOT NULL,
    note VARCHAR(500),
    habit_id BIGINT NOT NULL,
    CONSTRAINT fk_habit_entries_habit FOREIGN KEY (habit_id) 
        REFERENCES daily_track.habits (id) ON DELETE CASCADE
);

-- Create tasks table
CREATE TABLE IF NOT EXISTS daily_track.tasks (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    task_type_id BIGINT NOT NULL,
    target_value INTEGER NOT NULL DEFAULT 100,
    current_value INTEGER NOT NULL DEFAULT 0,
    start_date DATE NOT NULL,
    end_date_interval INTEGER,
    end_date DATE,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    tag_id BIGINT NOT NULL,
    CONSTRAINT fk_tasks_task_type FOREIGN KEY (task_type_id) 
        REFERENCES daily_track.task_types (id),
    CONSTRAINT fk_tasks_tag FOREIGN KEY (tag_id) 
        REFERENCES daily_track.tags (id)
);

-- Create achievements table
CREATE TABLE IF NOT EXISTS daily_track.achievements (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    category VARCHAR(50) NOT NULL,
    date DATE NOT NULL
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_tags_name ON daily_track.tags (name);
CREATE INDEX IF NOT EXISTS idx_task_types_name ON daily_track.task_types (name);
CREATE INDEX IF NOT EXISTS idx_habit_entries_habit_id ON daily_track.habit_entries (habit_id);
CREATE INDEX IF NOT EXISTS idx_habit_entries_date ON daily_track.habit_entries (date);
CREATE INDEX IF NOT EXISTS idx_tasks_task_type_id ON daily_track.tasks (task_type_id);
CREATE INDEX IF NOT EXISTS idx_tasks_tag_id ON daily_track.tasks (tag_id);
CREATE INDEX IF NOT EXISTS idx_achievements_category ON daily_track.achievements (category);
CREATE INDEX IF NOT EXISTS idx_achievements_date ON daily_track.achievements (date);
