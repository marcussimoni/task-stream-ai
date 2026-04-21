import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BackupService } from '../../services/backup.service';
import { ToastService } from '../../../../shared/services/toast.service';
import { BackupFileDTO, BackupCreatedDTO, BackupRestoredDTO } from '../../models/backup.model';

@Component({
  selector: 'app-database-backup',
  templateUrl: './database-backup.component.html',
  styleUrls: ['./database-backup.component.scss'],
  imports: [CommonModule]
})
export class DatabaseBackupComponent implements OnInit {
  backups = signal<BackupFileDTO[]>([]);
  loading = signal<boolean>(false);
  operationInProgress = signal<boolean>(false);
  showRestoreModal = signal<boolean>(false);
  selectedBackupFilename = signal<string>('');

  private backupService = inject(BackupService);
  private toastService = inject(ToastService);

  ngOnInit(): void {
    this.loadBackups();
  }

  loadBackups(): void {
    this.loading.set(true);
    this.backupService.getBackupFiles().subscribe({
      next: (backupFiles) => {
        const sortedBackups = backupFiles.sort((a, b) => 
          new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        );
        this.backups.set(sortedBackups);
        this.loading.set(false);
      },
      error: (error) => {
        const message = error.error?.message || error.error?.error || error.message || 'Failed to load backups';
        this.toastService.error(message, `Error ${error.status || ''}`);
        this.loading.set(false);
      }
    });
  }

  createBackup(): void {
    if (this.operationInProgress()) {
      return;
    }

    this.operationInProgress.set(true);
    this.backupService.createBackup().subscribe({
      next: (response: BackupCreatedDTO) => {
        this.toastService.success(
          `Backup created: ${response.dbFilename}`,
          response.status === 'success' ? 'Success' : 'Completed'
        );
        this.loadBackups();
        this.operationInProgress.set(false);
      },
      error: (error) => {
        const message = error.error?.message || error.error?.error || error.message || 'Failed to create backup';
        this.toastService.error(message, `Error ${error.status || ''}`);
        this.operationInProgress.set(false);
      }
    });
  }

  openRestoreModal(filename: string): void {
    this.selectedBackupFilename.set(filename);
    this.showRestoreModal.set(true);
  }

  closeRestoreModal(): void {
    this.showRestoreModal.set(false);
    this.selectedBackupFilename.set('');
  }

  confirmRestore(): void {
    const filename = this.selectedBackupFilename();
    if (!filename || this.operationInProgress()) {
      return;
    }

    this.operationInProgress.set(true);
    this.showRestoreModal.set(false);

    this.backupService.restoreBackup(filename).subscribe({
      next: (response: BackupRestoredDTO) => {
        this.toastService.success(
          `Database restored from: ${response.filename}`,
          response.status === 'success' ? 'Success' : 'Completed'
        );
        this.operationInProgress.set(false);
      },
      error: (error) => {
        const message = error.error?.message || error.error?.error || error.message || 'Failed to restore backup';
        this.toastService.error(message, `Error ${error.status || ''}`);
        this.operationInProgress.set(false);
      }
    });
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleString();
  }
}
