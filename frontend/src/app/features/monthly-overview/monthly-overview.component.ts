import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { TaskService } from '../../core';
import { Task } from '../../core/models';
import { ToastService } from '../../shared/services/toast.service';

interface WeekGroup {
  weekStart: Date;
  weekEnd: Date;
  weekLabel: string;
  tasks: Task[];
}

@Component({
  selector: 'app-monthly-overview',
  templateUrl: './monthly-overview.component.html',
  styleUrls: ['./monthly-overview.component.css'],
  imports: [CommonModule, DatePipe, DecimalPipe]
})
export class MonthlyOverviewComponent implements OnInit {
  weeksInMonth: WeekGroup[] = [];
  allTasks: Task[] = [];
  
  currentMonth: Date = new Date();
  monthLabel: string = '';
  
  totalTaskCount = 0;
  completedCount = 0;
  pendingCount = 0;
  
  sortField: 'name' | 'tag' = 'name';
  sortDirection: 'asc' | 'desc' = 'asc';

  private toastService = inject(ToastService);

  constructor(
    private taskService: TaskService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.setToCurrentMonth();
    this.loadTasksForMonth();
  }

  setToCurrentMonth(): void {
    const now = new Date();
    this.currentMonth = new Date(now.getFullYear(), now.getMonth(), 1);
    this.updateMonthLabel();
  }

  updateMonthLabel(): void {
    const options: Intl.DateTimeFormatOptions = { month: 'long', year: 'numeric' };
    this.monthLabel = this.currentMonth.toLocaleDateString('en-US', options);
  }

  formatDateForApi(date: Date): string {
    return date.toISOString().split('T')[0];
  }

  getMonthStart(): Date {
    return new Date(this.currentMonth.getFullYear(), this.currentMonth.getMonth(), 1);
  }

  getMonthEnd(): Date {
    return new Date(this.currentMonth.getFullYear(), this.currentMonth.getMonth() + 1, 0, 23, 59, 59, 999);
  }

  calculateWeeksInMonth(): WeekGroup[] {
    const monthStart = this.getMonthStart();
    const monthEnd = this.getMonthEnd();
    const weeks: WeekGroup[] = [];

    // Find the first Monday of or before the month start
    let currentWeekStart = new Date(monthStart);
    const dayOfWeek = currentWeekStart.getDay();
    const diff = currentWeekStart.getDate() - dayOfWeek + (dayOfWeek === 0 ? -6 : 1);
    currentWeekStart.setDate(diff);
    currentWeekStart.setHours(0, 0, 0, 0);

    // Generate week buckets until we pass the month end
    while (currentWeekStart <= monthEnd) {
      const weekEnd = new Date(currentWeekStart);
      weekEnd.setDate(currentWeekStart.getDate() + 6);
      weekEnd.setHours(23, 59, 59, 999);

      const options: Intl.DateTimeFormatOptions = { month: 'short', day: 'numeric' };
      const startStr = currentWeekStart.toLocaleDateString('en-US', options);
      const endStr = weekEnd.toLocaleDateString('en-US', options);

      weeks.push({
        weekStart: new Date(currentWeekStart),
        weekEnd: new Date(weekEnd),
        weekLabel: `${startStr} - ${endStr}`,
        tasks: []
      });

      // Move to next week
      currentWeekStart.setDate(currentWeekStart.getDate() + 7);
    }

    return weeks;
  }

  distributeTasksIntoWeeks(tasks: Task[], weeks: WeekGroup[]): WeekGroup[] {
    return weeks.map(week => {
      const weekTasks = tasks.filter(task => {
        const taskStartDate = task.startDate ? new Date(task.startDate) : null;
        const taskEndDate = task.endDate ? new Date(task.endDate) : null;
        
        if (!taskStartDate || !taskEndDate) {
          return false;
        }
        
        // Task overlaps with week if: taskStart <= weekEnd AND taskEnd >= weekStart
        return taskStartDate <= week.weekEnd && taskEndDate >= week.weekStart;
      });

      return {
        ...week,
        tasks: this.sortTasks(weekTasks)
      };
    }).filter(week => week.tasks.length > 0);
  }

  sortTasks(tasks: Task[]): Task[] {
    return [...tasks].sort((a, b) => {
      let comparison = 0;
      
      if (this.sortField === 'name') {
        comparison = a.name.localeCompare(b.name);
      } else if (this.sortField === 'tag') {
        const tagA = a.tag?.name || '';
        const tagB = b.tag?.name || '';
        comparison = tagA.localeCompare(tagB);
      }
      
      return this.sortDirection === 'asc' ? comparison : -comparison;
    });
  }

  loadTasksForMonth(): void {
    const monthStart = this.getMonthStart();
    const monthEnd = this.getMonthEnd();
    
    const startDate = this.formatDateForApi(monthStart);
    const endDate = this.formatDateForApi(monthEnd);
    
    this.taskService.getTasksForMonth(startDate, endDate).subscribe({
      next: (tasks: Task[]) => {
        this.allTasks = tasks;
        const weeks = this.calculateWeeksInMonth();
        this.weeksInMonth = this.distributeTasksIntoWeeks(tasks, weeks);
        this.updateStats();
        this.cdr.detectChanges();
      },
      error: (error: any) => {
        const message = error.error?.message || error.error?.error || error.message || 'Failed to load tasks for this month';
        this.toastService.error(message, `Error ${error.status || ''}`);
        this.cdr.detectChanges();
      }
    });
  }

  previousMonth(): void {
    this.currentMonth.setMonth(this.currentMonth.getMonth() - 1);
    this.updateMonthLabel();
    this.loadTasksForMonth();
  }

  nextMonth(): void {
    this.currentMonth.setMonth(this.currentMonth.getMonth() + 1);
    this.updateMonthLabel();
    this.loadTasksForMonth();
  }

  goToCurrentMonth(): void {
    this.setToCurrentMonth();
    this.loadTasksForMonth();
  }

  setSort(field: 'name' | 'tag'): void {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'asc';
    }
    // Re-distribute and sort tasks
    const weeks = this.calculateWeeksInMonth();
    this.weeksInMonth = this.distributeTasksIntoWeeks(this.allTasks, weeks);
    this.cdr.detectChanges();
  }

  getProgressPercentage(task: Task): number {
    const targetValue = 100;
    return Math.min((task.currentValue || 0) / targetValue * 100, 100);
  }

  getStatusLabel(task: Task): string {
    if (task.completed) {
      return 'Completed';
    }
    const progress = this.getProgressPercentage(task);
    if (progress === 0) {
      return 'Not Started';
    } else if (progress < 100) {
      return 'In Progress';
    }
    return 'Pending';
  }

  getStatusClass(task: Task): string {
    if (task.completed) {
      return 'status-completed';
    }
    const progress = this.getProgressPercentage(task);
    if (progress === 0) {
      return 'status-not-started';
    } else if (progress < 100) {
      return 'status-in-progress';
    }
    return 'status-pending';
  }

  private updateStats(): void {
    this.totalTaskCount = this.allTasks.length;
    this.completedCount = this.allTasks.filter(t => t.completed).length;
    this.pendingCount = this.allTasks.filter(t => !t.completed).length;
  }
}
