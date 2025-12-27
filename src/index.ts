import { registerPlugin } from '@capacitor/core';

import type { SafPickerPluginPlugin } from './definitions';

const SafPickerPlugin = registerPlugin<SafPickerPluginPlugin>('SafPickerPlugin', {
  web: () => import('./web').then((m) => new m.SafPickerPluginWeb()),
});

export * from './definitions';
export { SafPickerPlugin };
