import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { BackupFileDTO, BackupCreatedDTO, BackupRestoredDTO } from '../models/backup.model';

@Injectable({
  providedIn: 'root'
})
export class BackupService {
  private readonly endpoint = '/backup-database';

  constructor(private apiService: ApiService) {}

  getBackupFiles(): Observable<BackupFileDTO[]> {
    return this.apiService.get<BackupFileDTO[]>(`${this.endpoint}/backup-files`);
  }

  createBackup(): Observable<BackupCreatedDTO> {
    return this.apiService.get<BackupCreatedDTO>(this.endpoint);
  }

  restoreBackup(filename: string): Observable<BackupRestoredDTO> {
    return this.apiService.get<BackupRestoredDTO>(`${this.endpoint}/restore?filename=${encodeURIComponent(filename)}`);
  }
}
