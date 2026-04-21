import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { WeekSchedule, CreateWeekScheduleRequest } from '../models';
import { Task } from '../models';

@Injectable({
  providedIn: 'root'
})
export class WeekCalendarService {
  private readonly endpoint = '/week-calendar';

  constructor(private apiService: ApiService) {}

  getWeekSchedule(weekStartDate: string): Observable<WeekSchedule[]> {
    return this.apiService.get<WeekSchedule[]>(`${this.endpoint}?weekStartDate=${encodeURIComponent(weekStartDate)}`);
  }

  saveSchedule(schedule: CreateWeekScheduleRequest): Observable<WeekSchedule> {
    return this.apiService.post<WeekSchedule>(this.endpoint, schedule);
  }

  deleteSchedule(scheduleId: number): Observable<void> {
    return this.apiService.delete<void>(`${this.endpoint}/${scheduleId}`);
  }

  getTaskForTag(tagId: number): Observable<Task | null> {
    return this.apiService.get<Task | null>(`${this.endpoint}/task-for-tag?tagId=${tagId}`);
  }
}
