export type SafPickerErrorCode =
  | 'CANCELLED'
  | 'INVALID_URI'
  | 'NO_PERMISSION'
  | 'UNSUPPORTED'
  | 'INVALID_OPTIONS'
  | 'UNKNOWN';

export interface SafDocumentMetadata {
  name: string;
  uri: string;
  mimeType?: string | null;
  lastModified?: number;
  canRead: boolean;
  canWrite: boolean;
  documentId?: string | null;
}

export interface PickDirectoryOptions {
  /**
   * Optional initial tree URI to open the picker at.
   */
  initialUri?: string;
  /**
   * Request read access to the selected directory. Defaults to true.
   */
  allowRead?: boolean;
  /**
   * Request write access to the selected directory. Defaults to true.
   */
  allowWrite?: boolean;
  /**
   * Show advanced/hidden storage providers when supported by Android.
   */
  showAdvanced?: boolean;
  /**
   * Show storage roots when supported by Android.
   */
  showStorageRoots?: boolean;
}

export interface PickDirectoryResult extends SafDocumentMetadata {}

export interface SafFile extends SafDocumentMetadata {
  isFolder: boolean;
  size?: number;
}

export type SafListFilter = 'all' | 'files' | 'folders';
export type SafSortBy = 'name' | 'size' | 'lastModified';
export type SafSortOrder = 'asc' | 'desc';

export interface ListFilesOptions {
  uri: string;
  filter?: SafListFilter;
  sortBy?: SafSortBy;
  sortOrder?: SafSortOrder;
  offset?: number;
  maxItems?: number;
}

export interface ListFilesResult {
  files: SafFile[];
  totalCount: number;
  nextOffset?: number;
}

export interface PersistPermissionsOptions {
  uri: string;
  allowRead?: boolean;
  allowWrite?: boolean;
}

export interface ReleasePermissionsOptions {
  uri: string;
  allowRead?: boolean;
  allowWrite?: boolean;
}

export interface PersistedPermission {
  uri: string;
  isRead: boolean;
  isWrite: boolean;
  persistedTime?: number;
}

export interface SafPickerPlugin {
  /**
   * Launch the Android SAF directory picker.
   */
  pickDirectory(options?: PickDirectoryOptions): Promise<PickDirectoryResult>;
  /**
   * List the contents of a SAF directory.
   */
  listFiles(options: ListFilesOptions): Promise<ListFilesResult>;
  /**
   * Persist read/write access to a SAF URI.
   */
  persistPermissions(options: PersistPermissionsOptions): Promise<PersistedPermission>;
  /**
   * Release persisted permissions for a SAF URI.
   */
  releasePermissions(options: ReleasePermissionsOptions): Promise<{ released: boolean }>;
  /**
   * List persisted SAF permissions for this app.
   */
  listPersistedPermissions(): Promise<{ permissions: PersistedPermission[] }>;
}
