import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Tag } from '../models';

@Injectable({
  providedIn: 'root'
})
export class TagService {
  private readonly endpoint = '/tags';

  constructor(private apiService: ApiService) {}

  createTag(tag: Omit<Tag, 'id' | 'createdAt' | 'updatedAt'>): Observable<Tag> {
    return this.apiService.post<Tag>(this.endpoint, tag);
  }

  getAllTags(): Observable<Tag[]> {
    return this.apiService.get<Tag[]>(this.endpoint);
  }

  getTagById(id: number): Observable<Tag> {
    return this.apiService.get<Tag>(`${this.endpoint}/${id}`);
  }

  updateTag(id: number, tag: Partial<Tag>): Observable<Tag> {
    return this.apiService.put<Tag>(`${this.endpoint}/${id}`, tag);
  }

  deleteTag(id: number): Observable<void> {
    return this.apiService.delete<void>(`${this.endpoint}/${id}`);
  }

  searchTags(query: string): Observable<Tag[]> {
    return this.apiService.get<Tag[]>(`${this.endpoint}/search?q=${encodeURIComponent(query)}`);
  }

  getTagsByIds(ids: number[]): Observable<Tag[]> {
    return this.apiService.post<Tag[]>(`${this.endpoint}/batch`, { ids });
  }

  getTagsByUsage(): Observable<{ tag: Tag; goalCount: number }[]> {
    return this.apiService.get<{ tag: Tag; goalCount: number }[]>(`${this.endpoint}/usage`);
  }
}
