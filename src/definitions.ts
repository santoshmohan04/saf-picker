export interface SafPickerPluginPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
