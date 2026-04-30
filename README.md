# saf-picker

Android Storage Access Framework directory picker

## Platform support

This plugin uses the Android Storage Access Framework, so native APIs are **Android-only**. Web and iOS
calls reject with an `UNSUPPORTED` error code.

## Install

```bash
npm install saf-picker
npx cap sync
```

## Example app (modern UI)

An Android-focused demo UI lives in `example/` and showcases directory picking + file listing.

```bash
cd example
npm install
npm run sync
npx cap add android
npm run open:android
```

Open the app on your device/emulator and use **Pick directory** and **Load files** to exercise the
plugin. The web preview shows the UI, but SAF actions require Android.

## Permissions & options

- `pickDirectory` supports initial URI, read/write flags, and advanced storage visibility options.
- `listFiles` supports filtering, sorting, and pagination (`offset`/`maxItems`).
- Use `persistPermissions`, `releasePermissions`, and `listPersistedPermissions` to manage
  long-lived access for previously selected URIs.
- Errors include structured codes such as `CANCELLED`, `INVALID_URI`, `NO_PERMISSION`, and `UNSUPPORTED`.

## API

<docgen-index></docgen-index>

<docgen-api>
<!-- run docgen to generate docs from the source -->
<!-- More info: https://github.com/ionic-team/capacitor-docgen -->
</docgen-api>
