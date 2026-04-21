export interface TaskMetricsItem {
  tagId: number;
  tag: string;
  total: number;
  status: 'completed' | 'incompleted';
  date: string;
}

export interface MonthlyTasksMetrics {
    tasksMetrics: Array<TaskMetricsItem>,
    totalCompleted: number,
    totalIncomplete: number,
}
