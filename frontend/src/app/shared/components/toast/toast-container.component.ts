import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../services/toast.service';
import { ToastComponent } from './toast.component';
import { Toast } from '../../models/toast.model';

@Component({
  selector: 'app-toast-container',
  templateUrl: './toast-container.component.html',
  styleUrls: ['./toast-container.component.scss'],
  standalone: true,
  imports: [CommonModule, ToastComponent]
})
export class ToastContainerComponent {
  private toastService = inject(ToastService);
  readonly toasts = this.toastService.activeToasts;

  onDismiss(toastId: string): void {
    this.toastService.dismiss(toastId);
  }

  trackByToastId(index: number, toast: Toast): string {
    return toast.id;
  }
}
