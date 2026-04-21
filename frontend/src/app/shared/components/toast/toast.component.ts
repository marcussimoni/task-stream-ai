import { Component, Input, Output, EventEmitter, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Toast } from '../../models/toast.model';

@Component({
  selector: 'app-toast',
  templateUrl: './toast.component.html',
  styleUrls: ['./toast.component.scss'],
  standalone: true,
  imports: [CommonModule]
})
export class ToastComponent implements OnInit, OnDestroy {
  @Input({ required: true }) toast!: Toast;
  @Output() dismiss = new EventEmitter<string>();

  private dismissTimer: ReturnType<typeof setTimeout> | null = null;
  private remainingTime: number = 0;
  private startTime: number = 0;
  isExiting = false;

  ngOnInit(): void {
    this.startDismissTimer();
  }

  ngOnDestroy(): void {
    this.clearDismissTimer();
  }

  @HostListener('mouseenter')
  onMouseEnter(): void {
    if (this.dismissTimer) {
      this.remainingTime -= Date.now() - this.startTime;
      this.clearDismissTimer();
    }
  }

  @HostListener('mouseleave')
  onMouseLeave(): void {
    if (this.remainingTime > 0) {
      this.startTime = Date.now();
      this.dismissTimer = setTimeout(() => {
        this.triggerDismiss();
      }, this.remainingTime);
    }
  }

  private startDismissTimer(): void {
    const duration = this.toast.duration ?? 5000;
    this.remainingTime = duration;
    this.startTime = Date.now();
    this.dismissTimer = setTimeout(() => {
      this.triggerDismiss();
    }, duration);
  }

  private clearDismissTimer(): void {
    if (this.dismissTimer) {
      clearTimeout(this.dismissTimer);
      this.dismissTimer = null;
    }
  }

  triggerDismiss(): void {
    this.isExiting = true;
    setTimeout(() => {
      this.dismiss.emit(this.toast.id);
    }, 300);
  }

  onCloseClick(): void {
    this.triggerDismiss();
  }

  getIcon(): string {
    switch (this.toast.type) {
      case 'success':
        return '✓';
      case 'error':
        return '✕';
      case 'warning':
        return '▲';
      case 'info':
        return 'ℹ';
      default:
        return 'ℹ';
    }
  }
}
