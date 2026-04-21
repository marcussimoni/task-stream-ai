export interface BackupFileDTO {
  filename: string;
  directory: string;
  size: string;
  createdAt: string;
}

export interface BackupCreatedDTO {
  dbFilename: string;
  sqlFilename: string;
  timestamp: string;
  status: string;
}

export interface BackupRestoredDTO {
  filename: string;
  timestamp: string;
  status: string;
}
