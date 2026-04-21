export interface LogDTO {
  title: string;
  logs: string[];
}

export enum LogLevel {
  ERROR = 'ERROR',
  WARN = 'WARN',
  INFO = 'INFO',
  DEBUG = 'DEBUG',
  TRACE = 'TRACE'
}

export interface LogFilterState {
  lines: number;
  selectedLevels: LogLevel[];
  searchTerm: string;
}
