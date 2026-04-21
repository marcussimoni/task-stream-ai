import { Tag } from './tag.model';

export enum Priority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL'
}

export const PriorityConfig: Record<Priority, { label: string; color: string; bgColor: string }> = {
  [Priority.LOW]: { label: 'Low', color: '#28a745', bgColor: '#d4edda' },
  [Priority.MEDIUM]: { label: 'Medium', color: '#007bff', bgColor: '#cce5ff' },
  [Priority.HIGH]: { label: 'High', color: '#fd7e14', bgColor: '#ffe5cc' },
  [Priority.CRITICAL]: { label: 'Critical', color: '#dc3545', bgColor: '#f8d7da' }
};

export interface Task {
  id?: number;
  name: string;
  description?: string;
  currentValue?: number;
  startDate?: string;
  endDateInterval?: number;
  endDate?: string;
  completed: boolean;
  tag?: Tag;
  createdAt?: string;
  updatedAt?: string;
  customEndDateSelected: boolean;
  priority: Priority;
  link?: string;
  summary?: string;
}
