import { Capacitor } from '@capacitor/core';
import { SafPicker } from 'saf-picker';
import type { PickDirectoryResult, SafFile } from 'saf-picker';

import './style.css';

type StatusTone = 'info' | 'success' | 'warning' | 'error';

const app = document.querySelector<HTMLDivElement>('#app');

if (!app) {
  throw new Error('App root element not found.');
}

const isNative = Capacitor.isNativePlatform();
const platformLabel = Capacitor.getPlatform().toUpperCase();

app.innerHTML = `
  <div class="app-shell">
    <header class="hero">
      <div>
        <p class="eyebrow">Android Storage Access Framework</p>
        <h1>SAF Picker</h1>
        <p class="subtitle">
          Modern demo UI to pick a directory and preview files using the saf-picker plugin.
        </p>
        <div class="meta">
          <span class="chip">${platformLabel}</span>
          <span class="chip">${isNative ? 'Native runtime' : 'Web preview'}</span>
        </div>
      </div>
      <div class="hero-card">
        <h2>Quick start</h2>
        <ol class="steps">
          <li>Open the picker on Android.</li>
          <li>Select a directory using SAF.</li>
          <li>Load the files list.</li>
        </ol>
      </div>
    </header>

    <div class="status" data-status role="status" aria-live="polite"></div>

    <nav class="tabs">
      <button class="tab is-active" data-tab="overview" type="button">Overview</button>
      <button class="tab" data-tab="picker" type="button">Pick directory</button>
      <button class="tab" data-tab="files" type="button">Files</button>
    </nav>

    <main class="pages">
      <section class="page is-active" data-page="overview">
        <div class="grid">
          <article class="card">
            <h3>Experience</h3>
            <p>
              This UI mirrors a production-ready flow with cards, gradients, and clear actions
              for Android file access.
            </p>
            <div class="pill-row">
              <span class="pill">Material-inspired</span>
              <span class="pill">Touch friendly</span>
              <span class="pill">Dark UI</span>
            </div>
          </article>
          <article class="card">
            <h3>Plugin actions</h3>
            <ul class="list">
              <li>Pick a directory with SAF.</li>
              <li>Show directory metadata.</li>
              <li>List files and folders.</li>
            </ul>
          </article>
          <article class="card">
            <h3>Native readiness</h3>
            <p>
              ${isNative ? 'Native runtime detected. Picker buttons are enabled.' : 'Run on Android to enable the picker.'}
            </p>
          </article>
        </div>
      </section>

      <section class="page" data-page="picker">
        <div class="grid">
          <article class="card action-card">
            <div>
              <h3>Pick a directory</h3>
              <p>Launch Android SAF and store the chosen folder URI.</p>
            </div>
            <button class="button primary" data-action="pick" type="button">
              Pick directory
            </button>
          </article>
          <article class="card">
            <h3>Selected directory</h3>
            <div class="detail">
              <span>Name</span>
              <strong data-directory-name>None</strong>
            </div>
            <div class="detail">
              <span>URI</span>
              <code data-directory-uri>—</code>
            </div>
          </article>
        </div>
      </section>

      <section class="page" data-page="files">
        <div class="grid">
          <article class="card">
            <div class="card-header">
              <div>
                <h3>Files</h3>
                <p class="muted">Load the contents of the selected directory.</p>
              </div>
              <button class="button ghost" data-action="list" type="button">Load files</button>
            </div>
            <div class="meta-row">
              <span class="pill" data-file-count>0 items</span>
              <span class="pill muted">Folders and files</span>
            </div>
            <ul class="file-list" data-file-list></ul>
            <p class="empty" data-empty>No files loaded yet.</p>
          </article>
          <article class="card">
            <h3>Status</h3>
            <p class="muted">
              Keep this screen open while exploring different folders on Android. The UI updates
              in real time.
            </p>
            <div class="legend">
              <div class="legend-item">
                <span class="legend-dot folder"></span>
                Folder
              </div>
              <div class="legend-item">
                <span class="legend-dot file"></span>
                File
              </div>
            </div>
          </article>
        </div>
      </section>
    </main>
  </div>
`;

const statusEl = app.querySelector<HTMLDivElement>('[data-status]');
const directoryNameEl = app.querySelector<HTMLElement>('[data-directory-name]');
const directoryUriEl = app.querySelector<HTMLElement>('[data-directory-uri]');
const fileListEl = app.querySelector<HTMLUListElement>('[data-file-list]');
const emptyEl = app.querySelector<HTMLParagraphElement>('[data-empty]');
const fileCountEl = app.querySelector<HTMLElement>('[data-file-count]');
const pickButton = app.querySelector<HTMLButtonElement>('[data-action="pick"]');
const listButton = app.querySelector<HTMLButtonElement>('[data-action="list"]');

let directory: PickDirectoryResult | null = null;
let files: SafFile[] = [];
let isBusy = false;

const setStatus = (message: string, tone: StatusTone = 'info') => {
  if (!statusEl) {
    return;
  }

  statusEl.textContent = message;
  statusEl.dataset.tone = tone;
  statusEl.classList.toggle('is-visible', message.length > 0);
};

const formatBytes = (value?: number) => {
  if (value === undefined || value === null) {
    return '—';
  }

  if (value < 1024) {
    return `${value} B`;
  }

  const units = ['KB', 'MB', 'GB'];
  let index = -1;
  let size = value;

  do {
    size /= 1024;
    index += 1;
  } while (size >= 1024 && index < units.length - 1);

  return `${size.toFixed(1)} ${units[index]}`;
};

const updateDirectory = () => {
  if (directoryNameEl) {
    directoryNameEl.textContent = directory?.name ?? 'None';
  }

  if (directoryUriEl) {
    directoryUriEl.textContent = directory?.uri ?? '—';
  }
};

const updateFiles = () => {
  if (!fileListEl || !emptyEl || !fileCountEl) {
    return;
  }

  fileListEl.innerHTML = '';
  fileCountEl.textContent = `${files.length} item${files.length === 1 ? '' : 's'}`;

  if (!files.length) {
    emptyEl.classList.add('is-visible');
    return;
  }

  emptyEl.classList.remove('is-visible');

  files.forEach((file) => {
    const row = document.createElement('li');
    row.className = 'file-row';

    const name = document.createElement('div');
    name.className = 'file-main';
    name.textContent = file.name;

    const meta = document.createElement('div');
    meta.className = 'file-meta';
    meta.textContent = `${file.isFolder ? 'Folder' : 'File'} • ${formatBytes(file.size)}`;

    const info = document.createElement('div');
    info.append(name, meta);

    const icon = document.createElement('span');
    icon.className = `file-icon ${file.isFolder ? 'folder' : 'file'}`;

    row.append(icon, info);
    fileListEl.appendChild(row);
  });
};

const updateButtons = () => {
  const disabled = !isNative || isBusy;
  if (pickButton) {
    pickButton.disabled = disabled;
  }

  if (listButton) {
    listButton.disabled = disabled || !directory;
  }
};

const setBusy = (busy: boolean) => {
  isBusy = busy;
  updateButtons();
};

const onPickDirectory = async () => {
  if (!isNative) {
    setStatus('Run this demo on Android to access SAF.', 'warning');
    return;
  }

  try {
    setBusy(true);
    setStatus('Opening SAF picker…');
    directory = await SafPicker.pickDirectory();
    files = [];
    updateDirectory();
    updateFiles();
    setStatus(`Selected "${directory.name}".`, 'success');
  } catch (error) {
    setStatus('Picker was cancelled or failed.', 'error');
    console.error(error);
  } finally {
    setBusy(false);
  }
};

const onListFiles = async () => {
  if (!directory) {
    setStatus('Pick a directory first.', 'warning');
    return;
  }

  try {
    setBusy(true);
    setStatus('Loading files…');
    const response = await SafPicker.listFiles({ uri: directory.uri });
    files = response.files;
    updateFiles();
    setStatus(`Loaded ${files.length} item${files.length === 1 ? '' : 's'}.`, 'success');
  } catch (error) {
    setStatus('Failed to load files from SAF.', 'error');
    console.error(error);
  } finally {
    setBusy(false);
  }
};

const initTabs = () => {
  const tabs = app.querySelectorAll<HTMLButtonElement>('[data-tab]');
  const pages = app.querySelectorAll<HTMLElement>('[data-page]');

  tabs.forEach((tab) => {
    tab.addEventListener('click', () => {
      const target = tab.dataset.tab;
      if (!target) {
        return;
      }

      tabs.forEach((button) => button.classList.remove('is-active'));
      pages.forEach((page) => page.classList.remove('is-active'));

      tab.classList.add('is-active');
      pages.forEach((page) => {
        if (page.dataset.page === target) {
          page.classList.add('is-active');
        }
      });
    });
  });
};

pickButton?.addEventListener('click', onPickDirectory);
listButton?.addEventListener('click', onListFiles);

updateDirectory();
updateFiles();
initTabs();
updateButtons();

if (!isNative) {
  setStatus('Web preview only. Run on Android for SAF features.', 'warning');
}
