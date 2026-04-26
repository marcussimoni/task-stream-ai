import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { TaskRequestDTO } from '../interfaces/task-request-dto';

export interface AutomatedTaskRequest {
  input: string;
}

@Injectable({
  providedIn: 'root'
})
export class AiAssistantService {
  private readonly endpoint = '/ai-assistant';

  constructor(private apiService: ApiService) {}

  generateTasks(prompt: string): Observable<TaskRequestDTO[]> {
    const request: AutomatedTaskRequest = { input: prompt };
    return this.apiService.post<TaskRequestDTO[]>(`${this.endpoint}/plan-automated-creation`, request);
  }

  createTaskWithAi(prompt: string): Observable<void> {
    const request: AutomatedTaskRequest = { input: prompt };
    return this.apiService.post<void>(`${this.endpoint}/plan-automated-creation`, request);
  }
}
