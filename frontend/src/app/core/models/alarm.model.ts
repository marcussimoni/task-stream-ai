export interface TaskAlarm {
  id: string;
  type: 'PRE_REMINDER' | 'START_ALARM';
  scheduleId: number;
  tagName: string;
  tagColor: string;
  taskName?: string;
  scheduledTime: string;
  message: string;
}

export type AlarmConnectionStatus = 'connected' | 'connecting' | 'disconnected';

export interface AlarmState {
  alarm: TaskAlarm | null;
  connectionStatus: AlarmConnectionStatus;
}
