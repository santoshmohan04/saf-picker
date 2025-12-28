import { registerPlugin } from '@capacitor/core';
import type { SafPickerPlugin } from './definitions';

export const SafPicker = registerPlugin<SafPickerPlugin>('SafPickerPlugin');

export * from './definitions';