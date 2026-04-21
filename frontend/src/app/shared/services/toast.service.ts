import { Injectable, signal } from '@angular/core';
import { Toast, ToastType, DEFAULT_TOAST_CONFIG } from '../models/toast.model';

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private toasts = signal<Toast[]>([]);
  readonly activeToasts = this.toasts.asReadonly();

  success(message: string, title?: string): void {
    this.show({ type: 'success', message, title });
  }

  error(message: string, title?: string): void {
    this.show({ type: 'error', message, title });
  }

  warning(message: string, title?: string): void {
    this.show({ type: 'warning', message, title });
  }

  info(message: string, title?: string): void {
    this.show({ type: 'info', message, title });
  }

  show(toast: Partial<Toast>): void {
    const id = this.generateId();
    const newToast: Toast = {
      id,
      type: toast.type || 'info',
      message: toast.message || '',
      title: toast.title,
      duration: toast.duration ?? DEFAULT_TOAST_CONFIG.duration,
      dismissible: toast.dismissible ?? true
    };

    this.toasts.update(current => {
      const updated = [newToast, ...current];
      if (updated.length > DEFAULT_TOAST_CONFIG.maxVisible) {
        return updated.slice(0, DEFAULT_TOAST_CONFIG.maxVisible);
      }
      return updated;
    });
  }

  dismiss(toastId: string): void {
    this.toasts.update(current => current.filter(t => t.id !== toastId));
  }

  clear(): void {
    this.toasts.set([]);
  }

  private generateId(): string {
    return `toast-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }
}
