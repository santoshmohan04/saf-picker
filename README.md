# saf-picker

Android Storage Access Framework directory picker

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

## API

<docgen-index></docgen-index>

<docgen-api>
<!-- run docgen to generate docs from the source -->
<!-- More info: https://github.com/ionic-team/capacitor-docgen -->
</docgen-api>
