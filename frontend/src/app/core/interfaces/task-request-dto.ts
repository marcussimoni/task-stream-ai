export interface TaskRequestDTO {
  name: string;
  description: string;
  currentValue: number;
  startDate: string;
  endDateInterval: number;
  endDate: string | null;
  completed: boolean;
  tagId: number;
  customEndDateSelected: boolean;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  link: string | null;
}
