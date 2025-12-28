export interface PickDirectoryResult {
  uri: string;
  name: string;
}

export interface SafFile {
  name: string;
  uri: string;
  isFolder: boolean;
  size?: number;
}

export interface SafPickerPlugin {
  pickDirectory(): Promise<PickDirectoryResult>;
  listFiles(options: { uri: string }): Promise<{ files: SafFile[] }>;
}