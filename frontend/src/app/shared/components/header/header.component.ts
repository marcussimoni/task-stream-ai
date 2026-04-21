import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavService } from '../../../core/services/nav.service';
import { AlarmService } from '../../../core/services/alarm.service';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { HealthCheckBackendService } from '../../../core/services/healthcheckbackend.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'],
  imports: [CommonModule, RouterLink, RouterLinkActive],
  standalone: true
})
export class HeaderComponent implements OnInit {
  title = 'Task Stream AI';

  navService = inject(NavService);
  alarmService = inject(AlarmService);
  healthCheckBackendService = inject(HealthCheckBackendService);
  appInfo = signal<{name: string, description: string, version: string} | null>(null);

  backendStatus = signal("waiting")
  isModelStatus = signal("waiting")
  backendStatusClass = signal("connection-status-backend-waiting")
  iaModelStatusClass = signal("connection-status-backend-waiting")

  ngOnInit(): void {
    this.healthCheckBackend();
    this.loadApplicationInfo();
  }

  get connectionStatusText(): string {
    const status = this.alarmService.status();
    switch (status) {
      case 'connected': return 'Live';
      case 'connecting': return 'Connecting...';
      case 'disconnected': return 'Offline - alarms paused';
      default: return '';
    }
  }

  healthCheckBackend() {
    setInterval(() => {
    this.healthCheckBackendService.healthCheck('livenessState').subscribe({
      next: (response) => {
        this.backendStatus.set(response.status.toLowerCase());
        this.backendStatusClass.set(`connection-status-backend-${response.status.toLowerCase()}`);
      },
      error: (error) => {
        this.backendStatus.set("down");
        this.backendStatusClass.set("connection-status-backend-down");
      }
    });
    }, 5000);

    setInterval(() => {
    this.healthCheckBackendService.healthCheck('ollama').subscribe({
      next: (response) => {
        this.isModelStatus.set(response.status.toLowerCase());
        this.iaModelStatusClass.set(`connection-status-backend-${response.status.toLowerCase()}`);
      },
      error: (error) => {
        this.isModelStatus.set("down");
        this.iaModelStatusClass.set("connection-status-backend-down");
      }
    });
    }, 5000);
  }

  loadApplicationInfo() {
    this.healthCheckBackendService.getInfo().subscribe({
      next: (response) => {
        console.log(response);
        this.appInfo.set(response.app);
      },
      error: (error) => {
        console.error(error);
      }
    });
  }
}
