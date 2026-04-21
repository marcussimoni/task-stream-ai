import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LogService } from '../../services/log.service';
import { ToastService } from '../../../../shared/services/toast.service';
import { LogDTO, LogLevel } from '../../models/log.model';

@Component({
  selector: 'app-application-logs',
  templateUrl: './application-logs.component.html',
  styleUrls: ['./application-logs.component.scss'],
  imports: [CommonModule, FormsModule]
})
export class ApplicationLogsComponent implements OnInit {
  // State signals
  logs = signal<string[]>([]);
  title = signal<string>('');
  loading = signal<boolean>(false);
  lines = signal<number>(500);
  selectedLevels = signal<Set<LogLevel>>(new Set(Object.values(LogLevel)));
  searchTerm = signal<string>('');

  // Constants
  readonly lineOptions = [100, 500, 1000, 5000];
  readonly logLevels = Object.values(LogLevel);

  // Computed filtered logs
  filteredLogs = computed(() => {
    const allLogs = this.logs();
    const levels = this.selectedLevels();
    const search = this.searchTerm().toLowerCase().trim();

    return allLogs.filter(line => {
      // Level filter
      const level = this.detectLogLevel(line);
      if (level && !levels.has(level)) {
        return false;
      }

      // Search filter
      if (search && !line.toLowerCase().includes(search)) {
        return false;
      }

      return true;
    });
  });

  // Computed level counts
  levelCounts = computed(() => {
    const counts = new Map<LogLevel, number>();
    this.logLevels.forEach(level => counts.set(level, 0));

    this.logs().forEach(line => {
      const level = this.detectLogLevel(line);
      if (level) {
        counts.set(level, (counts.get(level) || 0) + 1);
      }
    });

    return counts;
  });

  private logService = inject(LogService);
  private toastService = inject(ToastService);

  private errorLogLineRegex = /^[a-z][a-z0-9_]*(\.[a-z0-9_]+)+/i; 

  ngOnInit(): void {
    this.loadLogs();
  }

  loadLogs(): void {
    this.loading.set(true);
    this.logService.getLogs(this.lines()).subscribe({
      next: (response: LogDTO) => {
        this.title.set(response.title);
        this.logs.set(response.logs);
        this.loading.set(false);
      },
      error: (error) => {
        const message = error.error?.message || error.error?.error || error.message || 'Failed to load logs';
        this.toastService.error(message, `Error ${error.status || ''}`);
        this.loading.set(false);
      }
    });
  }

  onLinesChange(count: number): void {
    this.lines.set(count);
    this.loadLogs();
  }

  toggleLevel(level: LogLevel): void {
    const current = new Set(this.selectedLevels());
    if (current.has(level)) {
      current.delete(level);
    } else {
      current.add(level);
    }
    this.selectedLevels.set(current);
  }

  onSearch(term: string): void {
    this.searchTerm.set(term);
  }

  clearSearch(): void {
    this.searchTerm.set('');
  }

  resetFilters(): void {
    this.searchTerm.set('');
    this.selectedLevels.set(new Set(this.logLevels));
  }

  downloadLogs(): void {
    const allLogs = this.logs();
    if (allLogs.length === 0) {
      this.toastService.warning('No logs to download');
      return;
    }

    const content = allLogs.join('\n');
    const blob = new Blob([content], { type: 'text/plain' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19);
    const sanitizedTitle = this.title().replace(/\//g, '_');

    link.href = url;
    link.download = `${sanitizedTitle}_${timestamp}.log`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);

    this.toastService.success('Logs downloaded successfully');
  }

  detectLogLevel(line: string): LogLevel | null {
    if (line.includes(' WARN ')) return LogLevel.WARN;
    if (line.includes(' INFO ')) return LogLevel.INFO;
    if (line.includes(' DEBUG ')) return LogLevel.DEBUG;
    if (line.includes(' TRACE ')) return LogLevel.TRACE;
    if (
      line.includes(' ERROR ') || 
      line.includes('at ') || 
      line.includes('Caused by:') ||
      this.errorLogLineRegex.test(line)
    ) {
      return LogLevel.ERROR;
    }
    return null;
  }

  getLevelClass(line: string): string {
    const level = this.detectLogLevel(line);
    if (!level) return '';
    return `log-level-${level.toLowerCase()}`;
  }

  escapeHtml(text: string): string {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }
}
