import { Component, OnInit, OnDestroy, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { forkJoin, Subscription } from 'rxjs';
import { WeekCalendarService } from '../../core/services/week-calendar.service';
import { TagService } from '../../core/services/tag.service';
import { AlarmService } from '../../core/services/alarm.service';
import { WeekSchedule, CreateWeekScheduleRequest, Priority, PriorityConfig } from '../../core/models';
import { Tag, Task } from '../../core/models';

interface CalendarCell {
  dayOfWeek: number;
  hour: number;
  schedule?: WeekSchedule;
}

@Component({
  selector: 'app-weekly-calendar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './weekly-calendar.component.html',
  styleUrls: ['./weekly-calendar.component.css']
})
export class WeeklyCalendarComponent implements OnInit, OnDestroy {
  // Signals for reactive state
  weekSchedule = signal<WeekSchedule[]>([]);
  tags = signal<Tag[]>([]);
  currentWeekStart = signal<Date>(this.getMonday(new Date()));
  selectedCell = signal<CalendarCell | null>(null);
  selectedDays = signal<number[]>([]);
  showTagModal = signal(false);
  showTaskDetails = signal(false);
  selectedTask = signal<Task | null>(null);
  loading = signal(false);
  highlightedScheduleId = signal<number | null>(null);

  // Constants
  readonly days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
  readonly fullDays = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
  readonly hours = Array.from({ length: 15 }, (_, i) => i + 8); // 8 AM to 10 PM

  // Services
  private weekCalendarService = inject(WeekCalendarService);
  private tagService = inject(TagService);
  private router = inject(Router);
  alarmService = inject(AlarmService);

  private highlightSubscription?: Subscription;

  ngOnInit(): void {
    this.loadTags();
    this.loadWeekSchedule();

    // Subscribe to highlight requests from alarm service
    this.highlightSubscription = this.alarmService.highlightCell$.subscribe(scheduleId => {
      this.highlightedScheduleId.set(scheduleId);
      // Auto-clear highlight after 10 seconds
      setTimeout(() => {
        if (this.highlightedScheduleId() === scheduleId) {
          this.highlightedScheduleId.set(null);
        }
      }, 10000);
    });
  }

  ngOnDestroy(): void {
    // Clean up SSE connection when component is destroyed
    this.alarmService.disconnect();
    this.highlightSubscription?.unsubscribe();
  }

  private getMonday(date: Date): Date {
    const d = new Date(date);
    const day = d.getDay();
    const diff = d.getDate() - day + (day === 0 ? -6 : 1); // Adjust when day is Sunday
    return new Date(d.setDate(diff));
  }

  private formatDateISO(date: Date): string {
    return date.toISOString().split('T')[0];
  }

  private loadTags(): void {
    this.tagService.getAllTags().subscribe({
      next: (tags) => this.tags.set(tags),
      error: (err) => console.error('Failed to load tags:', err)
    });
  }

  private loadWeekSchedule(): void {
    this.loading.set(true);
    const weekStart = this.formatDateISO(this.currentWeekStart());
    this.weekCalendarService.getWeekSchedule(weekStart).subscribe({
      next: (schedule) => {
        this.weekSchedule.set(schedule);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Failed to load week schedule:', err);
        this.loading.set(false);
      }
    });
  }

  getCell(dayOfWeek: number, hour: number): CalendarCell {
    const schedule = this.weekSchedule().find(
      s => s.dayOfWeek === dayOfWeek && s.hour === hour
    );
    return { dayOfWeek, hour, schedule };
  }

  getCellClasses(cell: CalendarCell): string {
    const classes = ['calendar-cell'];
    if (cell.schedule) {
      classes.push('has-tag');
      // Check if this cell should be highlighted (alarm triggered)
      if (cell.schedule.id === this.highlightedScheduleId()) {
        classes.push('alarm-highlight');
      }
    }
    return classes.join(' ');
  }

  /**
   * Check if a cell should be highlighted (alarm triggered for this schedule).
   */
  isHighlighted(cell: CalendarCell): boolean {
    return cell.schedule?.id === this.highlightedScheduleId();
  }

  getProgressColor(progress: number): string {
    if (progress >= 70) return 'high';
    if (progress >= 30) return 'medium';
    return 'low';
  }

  getProgressPercentage(task?: Task): number {
    if (!task || !task.currentValue) return 0;
    const targetValue = 100; // Default target
    return Math.round((task.currentValue / targetValue) * 100);
  }

  isCurrentDay(dayOfWeek: number): boolean {
    const today = new Date();
    const currentDayOfWeek = today.getDay() === 0 ? 6 : today.getDay() - 1; // Convert to 0=Monday
    const weekStart = this.currentWeekStart();
    const isCurrentWeek = this.formatDateISO(today) >= this.formatDateISO(weekStart) &&
                          today <= new Date(weekStart.getTime() + 6 * 24 * 60 * 60 * 1000);
    return isCurrentWeek && currentDayOfWeek === dayOfWeek;
  }

  isCurrentHour(hour: number): boolean {
    const today = new Date();
    const currentHour = today.getHours();
    const weekStart = this.currentWeekStart();
    const isCurrentWeek = this.formatDateISO(today) >= this.formatDateISO(weekStart) &&
                          today <= new Date(weekStart.getTime() + 6 * 24 * 60 * 60 * 1000);
    return isCurrentWeek && currentHour === hour;
  }

  getWeekRange(): string {
    const start = this.currentWeekStart();
    const end = new Date(start);
    end.setDate(end.getDate() + 6);
    const startStr = start.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    const endStr = end.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
    return `${startStr} - ${endStr}`;
  }

  getDayDate(dayOfWeek: number): string {
    const date = new Date(this.currentWeekStart());
    date.setDate(date.getDate() + dayOfWeek);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  }

  previousWeek(): void {
    const newWeekStart = new Date(this.currentWeekStart());
    newWeekStart.setDate(newWeekStart.getDate() - 7);
    this.currentWeekStart.set(newWeekStart);
    this.loadWeekSchedule();
  }

  nextWeek(): void {
    const newWeekStart = new Date(this.currentWeekStart());
    newWeekStart.setDate(newWeekStart.getDate() + 7);
    this.currentWeekStart.set(newWeekStart);
    this.loadWeekSchedule();
  }

  goToToday(): void {
    this.currentWeekStart.set(this.getMonday(new Date()));
    this.loadWeekSchedule();
  }

  onCellClick(cell: CalendarCell): void {
    this.selectedCell.set(cell);
    this.selectedDays.set([cell.dayOfWeek]);
    this.showTagModal.set(true);
  }

  onTaskClick(task: Task, event: Event): void {
    event.stopPropagation();
    this.selectedTask.set(task);
    this.showTaskDetails.set(true);
  }

  closeTagModal(): void {
    this.showTagModal.set(false);
    this.selectedCell.set(null);
    this.selectedDays.set([]);
  }

  closeTaskDetails(): void {
    this.showTaskDetails.set(false);
    this.selectedTask.set(null);
  }

  selectTag(tag: Tag): void {
    const cell = this.selectedCell();
    if (!cell) return;

    const weekStartDate = this.formatDateISO(this.currentWeekStart());
    const selectedDays = this.selectedDays();

    const requests: CreateWeekScheduleRequest[] = selectedDays.map(dayOfWeek => ({
      dayOfWeek,
      hour: cell.hour,
      weekStartDate,
      tagId: tag.id
    }));

    forkJoin(requests.map(r => this.weekCalendarService.saveSchedule(r)))
      .subscribe({
        next: () => {
          this.loadWeekSchedule();
          this.closeTagModal();
        },
        error: (err) => console.error('Failed to save schedules:', err)
      });
  }

  removeTag(): void {
    const cell = this.selectedCell();
    if (!cell || !cell.schedule?.id) return;

    this.weekCalendarService.deleteSchedule(cell.schedule.id).subscribe({
      next: () => {
        this.loadWeekSchedule();
        this.closeTagModal();
      },
      error: (err) => console.error('Failed to delete schedule:', err)
    });
  }

  navigateToTasks(): void {
    this.router.navigate(['/tasks'], {
      queryParams: {
        taskId: this.selectedTask()?.id, 
        fromWeeklyCalendar: true
      }
    });
  }

  isDaySelected(dayIndex: number): boolean {
    return this.selectedDays().includes(dayIndex);
  }

  toggleDay(dayIndex: number): void {
    const current = this.selectedDays();
    if (current.includes(dayIndex)) {
      if (current.length > 1) {
        this.selectedDays.set(current.filter(d => d !== dayIndex));
      }
    } else {
      this.selectedDays.set([...current, dayIndex]);
    }
  }

  toggleAllDays(): void {
    const current = this.selectedDays();
    const allDays = [0, 1, 2, 3, 4, 5, 6];
    if (current.length === 7) {
      this.selectedDays.set([this.selectedCell()!.dayOfWeek]);
    } else {
      this.selectedDays.set(allDays);
    }
  }

  get hasNoTags(): boolean {
    return this.tags().length === 0;
  }

  get isCurrentWeek(): boolean {
    const today = new Date();
    const weekStart = this.currentWeekStart();
    const weekEnd = new Date(weekStart);
    weekEnd.setDate(weekEnd.getDate() + 6);
    return today >= weekStart && today <= weekEnd;
  }

  getPriorityColor(priority: Priority): string {
    return PriorityConfig[priority]?.color || '#6c757d';
  }
}
