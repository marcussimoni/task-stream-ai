import { Component, OnInit, ChangeDetectorRef, inject, signal } from '@angular/core';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { TaskService } from '../../core';
import { TasksGroupedDTO, TasksByTagDTO, Priority } from '../../core/models';
import { ToastService } from '../../shared/services/toast.service';

interface TagGroup {
  tagName: string;
  taskCount: number;
  completedCount: number;
  pendingCount: number;
  tasks: TasksByTagDTO[];
  isExpanded: boolean;
}

@Component({
  selector: 'app-monthly-overview',
  templateUrl: './monthly-overview.component.html',
  styleUrls: ['./monthly-overview.component.css'],
  imports: [CommonModule, DatePipe, DecimalPipe]
})
export class MonthlyOverviewComponent implements OnInit {
  private tagsInMonth = signal<TagGroup[]>([]);
  readonly tagsInMonthList = this.tagsInMonth.asReadonly();
  
  currentMonth: Date = new Date();
  monthLabel: string = '';
  
  totalTaskCount = 0;
  completedCount = 0;
  pendingCount = 0;

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

  private mapToTagGroups(groupedTasks: TasksGroupedDTO[]): TagGroup[] {
    return groupedTasks.map(group => {
      const completedCount = group.tasks.filter(task => task.completed).length;
      const pendingCount = group.tasks.filter(task => !task.completed).length;
      
      return {
        tagName: group.tag,
        taskCount: group.total,
        completedCount: completedCount,
        pendingCount: pendingCount,
        tasks: group.tasks,
        isExpanded: false // All tags collapsed by default
      };
    });
  }

  loadTasksForMonth(): void {
    const monthString = this.formatMonthForApi();
    
    this.taskService.getGroupedTasksByTags(monthString).subscribe({
      next: (groupedTasks: TasksGroupedDTO[]) => {
        const tagGroups = this.mapToTagGroups(groupedTasks);
        this.tagsInMonth.set(tagGroups);
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

  toggleTagExpansion(tagName: string): void {
    const currentTags = this.tagsInMonth();
    const updatedTags = currentTags.map(tag => 
      tag.tagName === tagName ? { ...tag, isExpanded: !tag.isExpanded } : tag
    );
    this.tagsInMonth.set(updatedTags);
  }

  expandAllTags(): void {
    const currentTags = this.tagsInMonth();
    const updatedTags = currentTags.map(tag => ({ ...tag, isExpanded: true }));
    this.tagsInMonth.set(updatedTags);
  }

  collapseAllTags(): void {
    const currentTags = this.tagsInMonth();
    const updatedTags = currentTags.map(tag => ({ ...tag, isExpanded: false }));
    this.tagsInMonth.set(updatedTags);
  }

  getProgressPercentage(task: TasksByTagDTO): number {
    const targetValue = 100;
    return Math.min((task.currentValue || 0) / targetValue * 100, 100);
  }

  getStatusLabel(task: TasksByTagDTO): string {
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

  getStatusClass(task: TasksByTagDTO): string {
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

  getPriorityClass(priority: string): string {
    switch (priority) {
      case 'LOW': return 'priority-low';
      case 'MEDIUM': return 'priority-medium';
      case 'HIGH': return 'priority-high';
      case 'CRITICAL': return 'priority-critical';
      default: return 'priority-medium';
    }
  }

  private formatMonthForApi(): string {
    const year = this.currentMonth.getFullYear();
    const month = String(this.currentMonth.getMonth() + 1).padStart(2, '0');
    return `${year}-${month}-01`;
  }

  private updateStats(): void {
    const allTags = this.tagsInMonth();
    this.totalTaskCount = allTags.reduce((sum, tag) => sum + tag.taskCount, 0);
    this.completedCount = allTags.reduce((sum, tag) => sum + tag.completedCount, 0);
    this.pendingCount = allTags.reduce((sum, tag) => sum + tag.pendingCount, 0);
  }
}
