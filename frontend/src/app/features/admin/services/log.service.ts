import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { LogDTO } from '../models/log.model';

@Injectable({
  providedIn: 'root'
})
export class LogService {
  private readonly endpoint = '/application-logs';

  constructor(private apiService: ApiService) {}

  getLogs(lines: number = 500): Observable<LogDTO> {
    return this.apiService.get<LogDTO>(`${this.endpoint}?lines=${lines}`);
  }
}
