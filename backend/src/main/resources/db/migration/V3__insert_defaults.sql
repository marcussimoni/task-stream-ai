-- Insert default task types
INSERT INTO daily_track.task_types (name, description, color, created_at) VALUES
('STUDY_HOURS', 'Track study hours for learning goals', '#10B981', CURRENT_TIMESTAMP),
('TAGS_COMPLETED', 'Track completed tags/categories', '#3B82F6', CURRENT_TIMESTAMP),
('HABIT_STREAK', 'Track habit streaks and consistency', '#F59E0B', CURRENT_TIMESTAMP),
('ACHIEVEMENTS', 'Track achievement unlocks', '#8B5CF6', CURRENT_TIMESTAMP),
('CUSTOM', 'Custom task type for flexible tracking', '#6B7280', CURRENT_TIMESTAMP);

-- Insert default tags
INSERT INTO daily_track.tags (name, description, color, created_at) VALUES
('Study', 'Time dedicated to learning and education', '#3B82F6', CURRENT_TIMESTAMP),
('Exercise', 'Physical activities and workouts', '#EF4444', CURRENT_TIMESTAMP),
('Read', 'Reading books, articles, or papers', '#10B981', CURRENT_TIMESTAMP),
('Work', 'Professional and career-related tasks', '#6366F1', CURRENT_TIMESTAMP),
('Personal', 'Personal development and self-care', '#F59E0B', CURRENT_TIMESTAMP),
('Health', 'Health-related habits and goals', '#EC4899', CURRENT_TIMESTAMP),
('Finance', 'Financial planning and management', '#14B8A6', CURRENT_TIMESTAMP),
('Hobby', 'Creative pursuits and leisure activities', '#8B5CF6', CURRENT_TIMESTAMP),
('Social', 'Social connections and relationships', '#F97316', CURRENT_TIMESTAMP),
('Meditation', 'Mindfulness and meditation practice', '#84CC16', CURRENT_TIMESTAMP),
('Writing', 'Writing, journaling, or blogging', '#06B6D4', CURRENT_TIMESTAMP),
('Coding', 'Programming and development work', '#A855F7', CURRENT_TIMESTAMP),
('Language', 'Language learning and practice', '#EAB308', CURRENT_TIMESTAMP),
('Music', 'Music practice and learning', '#22D3EE', CURRENT_TIMESTAMP),
('Cooking', 'Cooking and meal preparation', '#FB7185', CURRENT_TIMESTAMP);
