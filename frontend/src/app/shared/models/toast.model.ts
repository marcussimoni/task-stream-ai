export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface Toast {
  id: string;
  type: ToastType;
  title?: string;
  message: string;
  duration?: number;
  dismissible?: boolean;
}

export interface ToastConfig {
  duration: number;
  maxVisible: number;
}

export const DEFAULT_TOAST_CONFIG: ToastConfig = {
  duration: 5000,
  maxVisible: 5
};
