import { Component, OnInit, inject, signal, SecurityContext, computed } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { TaskService, TagService, AiAssistantService } from '../../core';
import { Task, Tag, Priority, PriorityConfig } from '../../core/models';
import { TaskRequestDTO } from '../../core/interfaces/task-request-dto';
import { ToastService } from '../../shared/services/toast.service';
import { ActivatedRoute, Router } from '@angular/router';
import { marked } from 'marked';
import { DomSanitizer } from '@angular/platform-browser';

interface WeekGroup {
  weekStart: Date;
  weekEnd: Date;
  weekLabel: string;
  tasks: Task[];
}

@Component({
  selector: 'app-tasks',
  templateUrl: './tasks.component.html',
  styleUrls: ['./tasks.component.css'],
  imports: [CommonModule, ReactiveFormsModule, FormsModule, DatePipe, DecimalPipe]
})
export class TasksComponent implements OnInit {
  // Signals for state management
  private tasks = signal<Task[]>([]);
  readonly tasksList = computed(() => this.tasks());
  
  private allTags = signal<Tag[]>([]);
  readonly tags = computed(() => this.allTags());
  
  private weeksInMonthState = signal<WeekGroup[]>([]);
  readonly weeksInMonth = computed(() => this.weeksInMonthState());
  
  private currentMonthState = signal<Date>(new Date());
  readonly currentMonth = computed(() => this.currentMonthState());
  
  readonly monthLabel = computed(() => {
    const date = this.currentMonthState();
    return date.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
  });
  
  // UI state signals
  private isEditingState = signal<boolean>(false);
  readonly isEditing = computed(() => this.isEditingState());

  private editingTaskIdState = signal<number | null>(null);
  readonly editingTaskId = computed(() => this.editingTaskIdState());

  private showModalState = signal<boolean>(false);
  readonly showModal = computed(() => this.showModalState());

  private completedTasksState = signal<boolean>(false);
  readonly completedTasks = computed(() => this.completedTasksState());

  private selectedTagState = signal<number | null>(null);
  readonly selectedTag = computed(() => this.selectedTagState());

  private summaryMarkdownState = signal<string>('');
  readonly summaryMarkdown = computed(() => this.summaryMarkdownState());

  // AI modal state signals
  private showAiModalState = signal<boolean>(false);
  readonly showAiModal = computed(() => this.showAiModalState());

  private aiModalStepState = signal<1 | 2>(1);
  readonly aiModalStep = computed(() => this.aiModalStepState());

  private aiPromptState = signal<string>('');
  readonly aiPrompt = computed(() => this.aiPromptState());

  private generatedTasksState = signal<TaskRequestDTO[]>([]);
  readonly generatedTasks = computed(() => this.generatedTasksState());

  private isGeneratingTasksState = signal<boolean>(false);
  readonly isGeneratingTasks = computed(() => this.isGeneratingTasksState());

  private isCreatingTasksState = signal<boolean>(false);
  readonly isCreatingTasks = computed(() => this.isCreatingTasksState());

  private isSubmittingAiState = signal<boolean>(false);
  readonly isSubmittingAi = computed(() => this.isSubmittingAiState());
  
  taskForm: FormGroup;
  priorities = Object.values(Priority);

  // Dependency injection
  private fb = inject(FormBuilder);
  private taskService = inject(TaskService);
  private tagService = inject(TagService);
  private aiAssistantService = inject(AiAssistantService);
  private toastService = inject(ToastService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private sanitizer = inject(DomSanitizer);

  constructor() {
    this.taskForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      currentValue: [0, [Validators.min(0)]],
      startDate: [''],
      endDateInterval: [null],
      endDate: [''],
      completed: [false],
      tagId: [null, Validators.required],
      customEndDateSelected: [false, Validators.required],
      priority: ['', Validators.required],
      link: [''],
      summary: ['']
    });
  }

  ngOnInit(): void {
    this.setToCurrentMonth();
    this.loadTasksForMonth();
    this.loadTags();
    this.loadTaskFromWeeklyCalendar();
  }

  // Month navigation methods
  setToCurrentMonth(): void {
    const now = new Date();
    this.currentMonthState.set(new Date(now.getFullYear(), now.getMonth(), 1));
  }

  previousMonth(): void {
    const current = this.currentMonthState();
    const newMonth = new Date(current);
    newMonth.setMonth(current.getMonth() - 1);
    this.currentMonthState.set(newMonth);
    this.loadTasksForMonth();
  }

  nextMonth(): void {
    const current = this.currentMonthState();
    const newMonth = new Date(current);
    newMonth.setMonth(current.getMonth() + 1);
    this.currentMonthState.set(newMonth);
    this.loadTasksForMonth();
  }

  goToCurrentMonth(): void {
    this.setToCurrentMonth();
    this.loadTasksForMonth();
  }

  // Week calculation methods
  private getMonthStart(): Date {
    const current = this.currentMonthState();
    return new Date(current.getFullYear(), current.getMonth(), 1);
  }

  private getMonthEnd(): Date {
    const current = this.currentMonthState();
    return new Date(current.getFullYear(), current.getMonth() + 1, 0, 23, 59, 59, 999);
  }

  private formatDateForApi(date: Date): string {
    return date.toISOString().split('T')[0];
  }

  private calculateWeeksInMonth(): WeekGroup[] {
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

  private distributeTasksIntoWeeks(tasks: Task[], weeks: WeekGroup[]): WeekGroup[] {
    const selectedTag = this.selectedTagState();
    const showCompleted = this.completedTasksState();
    
    return weeks.map(week => {
      const weekTasks = tasks.filter(task => {
        const taskStartDate = task.startDate ? new Date(task.startDate) : null;
        const taskEndDate = task.endDate ? new Date(task.endDate) : null;
        
        if (!taskStartDate || !taskEndDate) {
          return false;
        }
        
        // Apply tag filter
        if (selectedTag && task.tag?.id !== selectedTag) {
          return false;
        }
        
        // Apply completed filter
        if (task.completed && !showCompleted) {
          return false;
        }
        
        // Task appears only in the week where it starts (not all weeks it spans)
        return taskStartDate >= week.weekStart && taskStartDate <= week.weekEnd;
      });

      return {
        ...week,
        tasks: weekTasks
      };
    });
  }

  loadTasksForMonth(): void {
    const monthStart = this.getMonthStart();
    const monthEnd = this.getMonthEnd();
    
    const startDate = this.formatDateForApi(monthStart);
    const endDate = this.formatDateForApi(monthEnd);
    
    this.taskService.getTasksForMonth(startDate, endDate).subscribe({
      next: (tasks) => {
        this.tasks.set(tasks);
        const weeks = this.calculateWeeksInMonth();
        this.weeksInMonthState.set(this.distributeTasksIntoWeeks(tasks, weeks));
      },
      error: (error) => {
        const message = error.error?.message || error.error?.error || error.message || 'Failed to load tasks for this month';
        this.toastService.error(message, `Error ${error.status || ''}`);
      }
    });
  }

  onTagChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.selectedTagState.set(value ? Number(value) : null);
    const weeks = this.calculateWeeksInMonth();
    this.weeksInMonthState.set(this.distributeTasksIntoWeeks(this.tasks(), weeks));
  }

  showCompletedTasks(): void {
    this.completedTasksState.set(!this.completedTasksState());
    const weeks = this.calculateWeeksInMonth();
    this.weeksInMonthState.set(this.distributeTasksIntoWeeks(this.tasks(), weeks));
  }

  loadTags(): void {
    this.tagService.getAllTags().subscribe({
      next: (tags) => {
        this.allTags.set(tags);
      },
      error: (error) => {
        const message = error.error?.message || error.error?.error || error.message || 'Failed to load tags';
        this.toastService.error(message, `Error ${error.status || ''}`);
      }
    });
  }

  onSubmit(): void {
    if (this.taskForm.invalid) {
      return;
    }

    const taskData = this.taskForm.value;

    if (this.isEditingState() && this.editingTaskIdState()) {
      this.taskService.updateTask(this.editingTaskIdState()!, taskData).subscribe({
        next: () => {
          this.toastService.success('Task updated successfully!');
          this.resetForm();
          this.loadTasksForMonth();
          this.resetQueryParams();
        },
        error: (error) => {
          const message = error.error?.message || error.error?.error || error.message || 'Failed to update task';
          this.toastService.error(message, `Error ${error.status || ''}`);
        }
      });
    } else {
      this.taskService.createTask(taskData).subscribe({
        next: () => {
          this.toastService.success('Task created successfully!');
          this.resetForm();
          this.loadTasksForMonth();
        },
        error: (error) => {
          const message = error.error?.message || error.error?.error || error.message || 'Failed to create task';
          this.toastService.error(message, `Error ${error.status || ''}`);
        }
      });
    }
  }

  editTask(task: Task): void {
    this.isEditingState.set(true);
    this.editingTaskIdState.set(task.id!);
    this.taskForm.patchValue({
      name: task.name,
      description: task.description,
      currentValue: task.currentValue,
      startDate: task.startDate,
      endDateInterval: task.endDateInterval,
      endDate: task.endDate,
      completed: task.completed,
      tagId: task.tag?.id,
      customEndDateSelected: task.customEndDateSelected,
      priority: task.priority || Priority.LOW,
      link: task.link,
      summary: task.summary
    });
    this.summaryAsMarkdown(task);
    this.openModal();
  }

  deleteTask(id: number): void {
    if (confirm('Are you sure you want to delete this task?')) {
      this.taskService.deleteTask(id).subscribe({
        next: () => {
          this.toastService.success('Task deleted successfully!');
          this.loadTasksForMonth();
        },
        error: (error) => {
          const message = error.error?.message || error.error?.error || error.message || 'Failed to delete task';
          this.toastService.error(message, `Error ${error.status || ''}`);
        }
      });
    }
  }

  toggleTaskCompletion(task: Task): void {
    const updatedTask = { ...task, completed: !task.completed, tagId: task.tag?.id! };
    this.taskService.updateTask(task.id!, updatedTask).subscribe({
      next: () => {
        this.loadTasksForMonth();
      },
      error: (error) => {
        const message = error.error?.message || error.error?.error || error.message || 'Failed to update task';
        this.toastService.error(message, `Error ${error.status || ''}`);
      }
    });
  }

  markAsCompleted(task: Task): void {
    this.taskService.markAsCompleted(task.id ? task.id : this.editingTaskIdState()!).subscribe({
      next: () => {
        this.loadTasksForMonth();
        this.closeModal()
      },
      error: (error) => {
        const message = error.error?.message || error.error?.error || error.message || 'Failed to mark task as completed';
        this.toastService.error(message, `Error ${error.status || ''}`);
      }
    });
  }

  resetForm(): void {
    this.isEditingState.set(false);
    this.editingTaskIdState.set(null);
    this.taskForm.reset({
      currentValue: 0,
      completed: false,
      tagId: null,
      priority: ''
    });
  }

  openModal(): void {
    this.showModalState.set(true);
  }

  closeModal(): void {
    this.showModalState.set(false);
    this.resetForm();
    this.resetQueryParams();
  }

  getProgressPercentage(task: Task): number {
    const targetValue = 100;
    return Math.min((task.currentValue || 0) / targetValue * 100, 100);
  }

  getPriorityColor(priority: Priority): string {
    return PriorityConfig[priority]?.color || '#6c757d';
  }

  getPriorityLabel(priority: Priority): string {
    return PriorityConfig[priority]?.label || priority;
  }

  loadLinkContent(): void {
    const link = this.taskForm.get('link')?.value
    if(link){
      this.taskService.loadLinkContent(link).subscribe({
        next: (response) => {
          this.taskForm.get('name')?.setValue(response.title)
        },
        error: (error) => {
          console.error("Failed to load link content:", error);
        }
      });
    } else {
      this.taskForm.get('name')?.setValue('');
    }
  }

  private loadTaskFromWeeklyCalendar(): void {
    this.route.queryParamMap.subscribe(params => {
      if (params.get('fromWeeklyCalendar') === 'true') {
        this.loadTaskAndOpenModal(Number(params.get('taskId')));
      }
    });
  }

  private loadTaskAndOpenModal(taskId: number): void {
    this.taskService.getTaskById(taskId).subscribe({
      next: (task) => {
        this.editingTaskIdState.set(task.id!);
        this.isEditingState.set(true);
        this.taskForm.patchValue({
          name: task.name,
          description: task.description,
          currentValue: task.currentValue,
          startDate: task.startDate,
          endDateInterval: task.endDateInterval,
          endDate: task.endDate,
          completed: task.completed,
          tagId: task.tag?.id,
          customEndDateSelected: task.endDate ? true : false,
          priority: task.priority,
          link: task.link,
          summary: task.summary
        });
        this.showModalState.set(true);
        this.summaryAsMarkdown(task);
      },
      error: (error) => {
        const message = error.error?.message || error.error?.error || error.message || 'Failed to load task';
        this.toastService.error(message, `Error ${error.status || ''}`);
      }
    });
  }

  private resetQueryParams(): void {
    this.router.navigate([], {
      queryParams: {
        fromWeeklyCalendar: false,
        taskId: null
      }
    });
  }

  private summaryAsMarkdown(task: Task) {
    const markdownSummary = marked.parse(task.summary || '') as string
    this.summaryMarkdownState.set(this.sanitizer.sanitize(SecurityContext.HTML, markdownSummary) ?? '');
  }

  private extractErrorMessage(error: any, defaultMessage: string = 'An error occurred'): string {
    return error.error?.message || error.error?.error || error.message || defaultMessage;
  }

  // AI Modal Methods
  openAiModal(): void {
    this.showAiModalState.set(true);
    this.aiModalStepState.set(1);
    this.aiPromptState.set('');
    this.generatedTasksState.set([]);
  }

  closeAiModal(): void {
    if (!this.isGeneratingTasksState() && !this.isCreatingTasksState()) {
      this.showAiModalState.set(false);
    }
  }

  onAiPromptChange(value: string): void {
    this.aiPromptState.set(value);
  }

  generateTasks(): void {
    const prompt = this.aiPromptState().trim();
    if (!prompt) {
      return;
    }

    this.isGeneratingTasksState.set(true);

    this.aiAssistantService.generateTasks(prompt).subscribe({
      next: (tasks) => {
        this.generatedTasksState.set(tasks);
        this.aiModalStepState.set(2);
        this.toastService.success('Tasks generated successfully! Review and edit before confirming.');
        this.isGeneratingTasksState.set(false);
      },
      error: (error) => {
        const message = this.extractErrorMessage(error);
        this.toastService.error(message, 'Error');
        this.isGeneratingTasksState.set(false);
      }
    });
  }

  confirmTasks(): void {
    const tasks = this.generatedTasksState();
    if (!tasks || tasks.length === 0) {
      return;
    }

    this.isCreatingTasksState.set(true);

    this.taskService.createTasks(tasks).subscribe({
      next: () => {
        this.toastService.success('Tasks submitted for creation successfully!');
        this.isCreatingTasksState.set(false);
        this.closeAiModal();
        this.loadTasksForMonth(); // Refresh the task list
      },
      error: (error: any) => {
        const message = this.extractErrorMessage(error);
        this.toastService.error(message, 'Error');
        this.isCreatingTasksState.set(false);
      }
    });
  }

  updateTask(index: number, field: keyof TaskRequestDTO, value: any): void {
    const tasks = this.generatedTasksState();
    const updatedTask = { ...tasks[index], [field]: value };
    
    // Create a new array but preserve the reference for non-updated items
    const newTasks = tasks.map((task, i) => 
      i === index ? updatedTask : task
    );
    
    this.generatedTasksState.set(newTasks);
  }

  goBackToStep1(): void {
    this.aiModalStepState.set(1);
  }

  // Helper method for template number conversion
  toNumber(value: string): number {
    return parseInt(value, 10) || 0;
  }

  // TrackBy function to prevent unnecessary re-renders
  trackByTaskIndex(index: number, task: TaskRequestDTO): number {
    return index;
  }

  // Legacy method for backward compatibility
  submitAiTask(): void {
    this.generateTasks();
  }

}
