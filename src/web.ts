import { CapacitorException, WebPlugin } from '@capacitor/core';

import type {
  ListFilesOptions,
  PersistPermissionsOptions,
  PickDirectoryOptions,
  ReleasePermissionsOptions,
  SafPickerPlugin,
} from './definitions';

export class SafPickerPluginWeb extends WebPlugin implements SafPickerPlugin {
  async pickDirectory(_options?: PickDirectoryOptions): Promise<never> {
    throw new CapacitorException('SAF picker is only available on Android.', 'UNSUPPORTED');
  }

  async listFiles(_options: ListFilesOptions): Promise<never> {
    throw new CapacitorException('SAF picker is only available on Android.', 'UNSUPPORTED');
  }

  async persistPermissions(_options: PersistPermissionsOptions): Promise<never> {
    throw new CapacitorException('SAF picker is only available on Android.', 'UNSUPPORTED');
  }

  async releasePermissions(_options: ReleasePermissionsOptions): Promise<never> {
    throw new CapacitorException('SAF picker is only available on Android.', 'UNSUPPORTED');
  }

  async listPersistedPermissions(): Promise<never> {
    throw new CapacitorException('SAF picker is only available on Android.', 'UNSUPPORTED');
  }
}
