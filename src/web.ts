import { WebPlugin } from '@capacitor/core';

import type { SafPickerPluginPlugin } from './definitions';

export class SafPickerPluginWeb extends WebPlugin implements SafPickerPluginPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
