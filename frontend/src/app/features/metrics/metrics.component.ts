import { Component, OnInit, ViewChild, ElementRef, AfterViewInit, ChangeDetectorRef, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Chart, registerables } from 'chart.js';
import { MetricsService } from '../../core';
import { MonthlyTasksMetrics, TaskMetricsItem } from '../../core/models';
import { ToastService } from '../../shared/services/toast.service';

// Register all Chart.js components
Chart.register(...registerables);

@Component({
  selector: 'app-metrics',
  templateUrl: './metrics.component.html',
  styleUrls: ['./metrics.component.css'],
  imports: [CommonModule, ReactiveFormsModule]
})
export class MetricsComponent implements OnInit, AfterViewInit {
  taskMetrics: MonthlyTasksMetrics = {
    tasksMetrics: [],
    totalCompleted: 0,
    totalIncomplete: 0
  };
  metricsForm: FormGroup;

  @ViewChild('taskChart') taskChartRef!: ElementRef<HTMLCanvasElement>;
  private taskChart: Chart | null = null;

  private toastService = inject(ToastService);

  constructor(
    private fb: FormBuilder,
    private metricsService: MetricsService,
    private cdr: ChangeDetectorRef
  ) {
    this.metricsForm = this.fb.group({
      month: [new Date().getMonth() + 1],
      year: [new Date().getFullYear()]
    });
  }

  ngOnInit(): void {
    this.loadTaskMetrics();
  }

  private viewReady = false;

  ngAfterViewInit(): void {
    this.viewReady = true;
    // If data already loaded, create chart now that view is ready
    if (this.taskMetrics?.tasksMetrics?.length > 0) {
      this.createTaskChart();
    }
  }

  private createTaskChart(): void {
    // Only create chart when view is ready AND data is available
    if (!this.viewReady || !this.taskChartRef?.nativeElement || this.taskMetrics.tasksMetrics.length === 0) {
      return;
    }

    if (this.taskChart) {
      this.taskChart.destroy();
    }

    const groupedData = this.getTaskMetricsByTag();

    this.taskChart = new Chart(this.taskChartRef.nativeElement, {
      type: 'bar',
      data: {
        labels: groupedData.map(item => item.tag),
        datasets: [
          {
            label: `Completed ${this.taskMetrics.totalCompleted}`,
            data: groupedData.map(item => item.completed),
            backgroundColor: '#28a745',
            borderColor: '#28a745',
            borderWidth: 1
          },
          {
            label: `Pending ${this.taskMetrics.totalIncomplete}`,
            data: groupedData.map(item => item.incompleted),
            backgroundColor: '#dc3545',
            borderColor: '#dc3545',
            borderWidth: 1
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: true,
            position: 'top'
          },
          title: {
            display: true,
            text: 'Tasks by Tag'
          }
        },
        scales: {
          x: {
            stacked: false
          },
          y: {
            stacked: false,
            beginAtZero: true,
            ticks: {
              stepSize: 1
            }
          }
        }
      }
    });
  }

  getMonthName(month: number): string {
    const date = new Date();
    date.setMonth(month - 1);
    return date.toLocaleDateString('en-US', { month: 'long' });
  }

  loadTaskMetrics(): void {
    const month = this.metricsForm.get('month')?.value;
    const year = this.metricsForm.get('year')?.value;

    this.metricsService.getMonthlyTaskMetrics(month, year).subscribe({
      next: (metrics) => {
        this.taskMetrics = metrics;
        this.updateChartData();
      },
      error: (error) => {
        const message = error.error?.message || error.error?.error || error.message || 'Failed to load task metrics';
        this.toastService.error(message, `Error ${error.status || ''}`);
      }
    });
  }

  updateChartData(): void {
    // Force change detection then wait for browser paint
    this.cdr.detectChanges();
    requestAnimationFrame(() => {
      this.createTaskChart();
    });
  }

  getTaskMetricsByTag(): { tag: string; completed: number; incompleted: number }[] {
    const grouped = new Map<string, { completed: number; incompleted: number }>();

    for (const item of this.taskMetrics.tasksMetrics) {
      if (!grouped.has(item.tag)) {
        grouped.set(item.tag, { completed: 0, incompleted: 0 });
      }
      const current = grouped.get(item.tag)!;
      if (item.status === 'completed') {
        current.completed += item.total;
      } else {
        current.incompleted += item.total;
      }
    }

    return Array.from(grouped.entries()).map(([tag, counts]) => ({
      tag,
      completed: counts.completed,
      incompleted: counts.incompleted
    }));
  }

}
