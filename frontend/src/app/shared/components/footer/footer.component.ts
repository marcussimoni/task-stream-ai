import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HealthCheckBackendService } from '../../../core/services/healthcheckbackend.service';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.css']
})
export class FooterComponent implements OnInit {
  readonly currentYear = signal(new Date().getFullYear());

  healthCheckBackendService = inject(HealthCheckBackendService);
  appInfo = signal<{name: string, description: string, version: string} | null>(null);

  ngOnInit() {
    this.loadApplicationInfo();
  }

  loadApplicationInfo() {
    this.healthCheckBackendService.getInfo().subscribe({
      next: (response) => {
        this.appInfo.set(response.app);
      },
      error: (error) => {
        console.error(error);
      }
    });
  }
}
