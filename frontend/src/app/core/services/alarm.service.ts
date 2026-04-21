import { Injectable, signal, computed } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { TaskAlarm, AlarmConnectionStatus } from '../models';
import { ApiService } from './api.service';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AlarmService {
  private currentAlarm = signal<TaskAlarm | null>(null);
  readonly activeAlarm = computed(() => this.currentAlarm());

  private connectionStatus = signal<AlarmConnectionStatus>('disconnected');
  readonly status = computed(() => this.connectionStatus());

  private eventSource: EventSource | null = null;
  private reconnectAttempts = 0;
  private maxReconnectDelay = 30000; // 30 seconds max
  private lastEventId: string | null = null;
  private intentionallyClosed = false;

  // Subject to notify calendar component to highlight a cell
  highlightCell$ = new Subject<number>(); // emits scheduleId

  private readonly endpoint = '/alarms';

  constructor(
    private router: Router,
    private http: HttpClient,
    private apiService: ApiService
  ) {}

  /**
   * Start SSE connection for alarm notifications.
   */
  connect(): void {
    if (this.eventSource) {
      this.eventSource.close();
    }

    this.connectionStatus.set('connecting');

    const url = this.lastEventId
      ? `/api${this.endpoint}/stream?lastEventId=${encodeURIComponent(this.lastEventId)}`
      : `/api${this.endpoint}/stream`;

    this.intentionallyClosed = false;
    this.eventSource = new EventSource(url);

    this.eventSource.onopen = () => {
      this.connectionStatus.set('connected');
      this.reconnectAttempts = 0;
    };

    // Handle heartbeat events (named event from backend)
    this.eventSource.addEventListener('heartbeat', (event) => {
      this.lastEventId = event.lastEventId || null;
    });

    // Handle alarm events (named event from backend)
    this.eventSource.addEventListener('alarm', (event) => {
      this.lastEventId = event.lastEventId || null;

      try {
        const alarm: TaskAlarm = JSON.parse(event.data);
        this.handleAlarm(alarm);
      } catch (e) {
        console.error('Failed to parse alarm:', e);
      }
    });

    // Handle connection errors
    this.eventSource.onerror = (error) => {
      this.connectionStatus.set('disconnected');

      // Don't reconnect if intentionally closed (e.g., page navigation)
      if (this.intentionallyClosed) {
        return;
      }

      // Let the browser's built-in reconnect handle it initially
      // Only force reconnect after multiple failures
      if (this.reconnectAttempts > 3) {
        this.scheduleReconnect();
      }
      this.reconnectAttempts++;
    };
  }

  /**
   * Disconnect SSE connection.
   */
  disconnect(): void {
    this.intentionallyClosed = true;

    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
    }

    this.connectionStatus.set('disconnected');
    this.reconnectAttempts = 0;
  }

  /**
   * Acknowledge the current alarm (removes it).
   */
  acknowledge(): void {
    const alarm = this.currentAlarm();
    if (!alarm) return;

    this.apiService.post(`${this.endpoint}/${alarm.id}/acknowledge`, {}).subscribe();
    this.currentAlarm.set(null);
  }

  /**
   * Dismiss the current alarm (doesn't acknowledge server-side).
   */
  dismiss(): void {
    this.currentAlarm.set(null);
  }

  /**
   * Check if there's currently an active alarm.
   */
  hasActiveAlarm(): boolean {
    return this.currentAlarm() !== null;
  }

  private handleAlarm(alarm: TaskAlarm): void {
    if (alarm.type === 'PRE_REMINDER') {
      this.showToast(alarm);
    } else {

      this.currentAlarm.set(alarm);

      // Auto-navigate to weekly calendar
      this.router.navigate(['/weekly-calendar']).then(() => {
        // Signal to calendar to highlight the cell
        this.highlightCell$.next(alarm.scheduleId);
      });
    }
  }

  private showToast(alarm: TaskAlarm): void {
    // Toast is handled by the alarm modal component showing briefly
    // Could integrate with a toast service here if needed
  }

  private scheduleReconnect(): void {
    const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), this.maxReconnectDelay);
    this.reconnectAttempts++;

    setTimeout(() => {
      if (this.connectionStatus() !== 'connected') {
        this.connect();
      }
    }, delay);
  }

}
