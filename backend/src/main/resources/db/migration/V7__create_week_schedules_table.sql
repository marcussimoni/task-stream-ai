CREATE TABLE IF NOT EXISTS daily_track.week_schedules (
    id BIGSERIAL PRIMARY KEY,
    day_of_week INT NOT NULL CHECK (day_of_week BETWEEN 0 AND 6),
    task_hour INT NOT NULL CHECK (task_hour BETWEEN 8 AND 22),
    week_start_date DATE NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_week_schedules_tag FOREIGN KEY (tag_id) 
        REFERENCES daily_track.tags (id) ON DELETE CASCADE,
    CONSTRAINT unique_schedule_slot UNIQUE (week_start_date, day_of_week, task_hour)
);
