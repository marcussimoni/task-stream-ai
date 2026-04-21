import { Task } from './task.model';

export interface Tag {
  id: number;
  name: string;
  description: string;
  color: string;
  createdAt?: string;
  updatedAt?: string;
  tasks?: Task[];
  totalUsed: number;
}

export interface CreateTagRequest {
  name: string;
  description: string;
  color: string;
}

export const DEFAULT_TAG_COLORS = [
  '#3B82F6', // blue
  '#10B981', // green
  '#F59E0B', // yellow
  '#EF4444', // red
  '#8B5CF6', // purple
  '#EC4899', // pink
  '#6B7280', // gray
  '#14B8A6', // teal
];
