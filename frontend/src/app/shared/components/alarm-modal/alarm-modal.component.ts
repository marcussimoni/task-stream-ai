import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TaskAlarm } from '../../../core/models';

@Component({
  selector: 'app-alarm-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './alarm-modal.component.html',
  styleUrls: ['./alarm-modal.component.scss']
})
export class AlarmModalComponent {
  alarm = input<TaskAlarm | null>(null);

  acknowledge = output<void>();
  dismiss = output<void>();

  onStart(): void {
    this.acknowledge.emit();
  }

  onDismiss(): void {
    this.dismiss.emit();
  }

  isStartAlarm(): boolean {
    return this.alarm()?.type === 'START_ALARM';
  }
}
