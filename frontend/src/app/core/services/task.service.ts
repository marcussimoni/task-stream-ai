import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Task, TasksGroupedDTO } from '../models';
import { HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private readonly endpoint = '/tasks';

  constructor(private apiService: ApiService) {}

  createTask(task: Omit<Task, 'id' | 'createdAt' | 'updatedAt'> & { tagId: number }): Observable<Task> {
    return this.apiService.post<Task>(this.endpoint, task);
  }

  createTasks(tasks: any[]): Observable<void> {
    return this.apiService.post<void>(`${this.endpoint}/create-all`, tasks);
  }

  getAllTasks(tag: number | null): Observable<Task[]> {
    let params = new HttpParams();
    if(tag){
      params = params.set('tag', tag.toString());
    }
    return this.apiService.getWithParams<Task[]>(this.endpoint, params);
  }

  getTaskById(id: number): Observable<Task> {
    return this.apiService.get<Task>(`${this.endpoint}/${id}`);
  }

  updateTask(id: number, task: Partial<Task> & { tagId: number }): Observable<Task> {
    return this.apiService.put<Task>(`${this.endpoint}/${id}`, task);
  }

  deleteTask(id: number): Observable<void> {
    return this.apiService.delete<void>(`${this.endpoint}/${id}`);
  }

  markAsCompleted(id: number): Observable<Task> {
    return this.apiService.patch<Task>(`${this.endpoint}/${id}/completed`, {});
  }

  getTasksByTag(tagId: number): Observable<Task[]> {
    return this.apiService.get<Task[]>(`${this.endpoint}/tag/${tagId}`);
  }

  searchTasks(query: string, tagId?: number): Observable<Task[]> {
    let url = `${this.endpoint}/search?q=${encodeURIComponent(query)}`;
    if (tagId) {
      url += `&tagId=${tagId}`;
    }
    return this.apiService.get<Task[]>(url);
  }

  getTasksForMonth(startDate: string, endDate: string): Observable<Task[]> {
    const url = `${this.endpoint}/month?startDate=${startDate}&endDate=${endDate}`;
    return this.apiService.get<Task[]>(url);
  }

  loadLinkContent(link: string): Observable<{ title: string }> {
    return this.apiService.get<{ title: string; content: string }>(`${this.endpoint}/link-content?url=${encodeURIComponent(link)}`);
  }

  getGroupedTasksByTags(month: string): Observable<TasksGroupedDTO[]> {
    const url = `${this.endpoint}/grouped-by-tags?month=${month}`;
    return this.apiService.get<TasksGroupedDTO[]>(url);
  }
}
