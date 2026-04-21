import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { MonthlyTasksMetrics } from '../models';

@Injectable({
  providedIn: 'root'
})
export class MetricsService {
  private readonly endpoint = '/metrics';

  constructor(private apiService: ApiService) {}

  getMonthlyTaskMetrics(month: number, year: number): Observable<MonthlyTasksMetrics> {
    return this.apiService.get<MonthlyTasksMetrics>(`${this.endpoint}/monthly/tasks?month=${month}&year=${year}`);
  }
}
