import { Tag } from './tag.model';
import { Task } from './task.model';

export interface WeekSchedule {
  id?: number;
  dayOfWeek: number; // 0 = Monday, 6 = Sunday
  hour: number; // 8-22
  tagId: number;
  tag?: Tag;
  task?: Task; // Computed/loaded task
  weekStartDate?: string; // ISO date string for week identification
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateWeekScheduleRequest {
  dayOfWeek: number;
  hour: number;
  weekStartDate: string;
  tagId: number;
}
