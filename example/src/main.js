import { Capacitor } from '@capacitor/core';
import { KmpDevice, KmpMotion } from 'capacitor-kotlin-multi-platform-umbrella';

const $ = (id) => document.getElementById(id);
const platform = Capacitor.getPlatform();
$('platform').textContent = { android: 'Android (JVM)', ios: 'iOS (Kotlin/Native)', web: 'the Web (Kotlin/JS)' }[platform];

// ---------------------------------------------------------------------------------------------
// Device info
// ---------------------------------------------------------------------------------------------
function renderKv(el, data) {
  el.replaceChildren(
    ...Object.entries(data).map(([key, value]) => {
      const row = document.createElement('div');
      const dt = document.createElement('dt');
      const dd = document.createElement('dd');
      dt.textContent = key;
      dd.textContent = typeof value === 'object' ? JSON.stringify(value) : String(value);
      row.append(dt, dd);
      return row;
    }),
  );
}

async function loadDeviceInfo() {
  try {
    const info = await KmpDevice.getDeviceInfo();
    $('deviceSummary').textContent = info.summary;
    $('deviceSummary').classList.remove('muted');
    const { screen } = info;
    // Native platforms return JSON objects without key order, so pick the display order here.
    const order = ['platform', 'manufacturer', 'model', 'operatingSystem', 'osVersion', 'browser', 'locale', 'language', 'region', 'formFactor', 'isVirtual', 'minimumOsVersion', 'isOsSupported'];
    renderKv($('device'), {
      ...Object.fromEntries(order.filter((key) => info[key] !== undefined).map((key) => [key, info[key]])),
      screen: `${screen.width}×${screen.height} @${screen.scale}x (${screen.physicalWidth}×${screen.physicalHeight}px)`,
    });
  } catch (e) {
    $('deviceSummary').textContent = `Error: ${e.message}`;
  }
}
$('deviceButton').addEventListener('click', loadDeviceInfo);

// ---------------------------------------------------------------------------------------------
// Motion
// ---------------------------------------------------------------------------------------------
let source = 'simulated';
let running = false;
const history = [];
const MAX_POINTS = 150;

function setSource(next) {
  source = next;
  $('sourceDevice').setAttribute('aria-pressed', String(next === 'device'));
  $('sourceSimulated').setAttribute('aria-pressed', String(next === 'simulated'));
  if (running) start();
}
$('sourceDevice').addEventListener('click', () => setSource('device'));
$('sourceSimulated').addEventListener('click', () => setSource('simulated'));

function flash(id) {
  const el = $(id);
  el.classList.add('flash');
  setTimeout(() => el.classList.remove('flash'), 400);
}

function drawChart() {
  const canvas = $('chart');
  const ctx = canvas.getContext('2d');
  const { width, height } = canvas;
  ctx.clearRect(0, 0, width, height);
  const range = 30; // ±30 m/s²
  const y = (v) => height / 2 - (v / range) * (height / 2);
  ctx.strokeStyle = getComputedStyle(document.body).getPropertyValue('--border');
  ctx.beginPath();
  ctx.moveTo(0, y(0));
  ctx.lineTo(width, y(0));
  ctx.stroke();
  const series = [
    ['x', '#e5484d'],
    ['y', '#30a46c'],
    ['z', '#0090ff'],
    ['linearMagnitude', getComputedStyle(document.body).getPropertyValue('--accent') || '#7f52ff'],
  ];
  for (const [key, color] of series) {
    ctx.strokeStyle = color;
    ctx.lineWidth = key === 'linearMagnitude' ? 3 : 2;
    ctx.beginPath();
    history.forEach((s, i) => {
      const px = (i / (MAX_POINTS - 1)) * width;
      i === 0 ? ctx.moveTo(px, y(s[key])) : ctx.lineTo(px, y(s[key]));
    });
    ctx.stroke();
  }
}

let frameRequested = false;
KmpMotion.addListener('motion', (s) => {
  history.push(s);
  if (history.length > MAX_POINTS) history.shift();
  $('ax').textContent = `x ${s.x.toFixed(2)}`;
  $('ay').textContent = `y ${s.y.toFixed(2)}`;
  $('az').textContent = `z ${s.z.toFixed(2)}`;
  $('tilt').textContent = `${Math.round(s.pitch)} / ${Math.round(s.roll)}`;
  if (!frameRequested) {
    frameRequested = true;
    requestAnimationFrame(() => {
      frameRequested = false;
      drawChart();
    });
  }
});
KmpMotion.addListener('step', (e) => {
  $('steps').textContent = e.count;
  $('cadence').textContent = e.cadence == null ? '–' : Math.round(e.cadence);
  flash('stepsStat');
});
KmpMotion.addListener('shake', (e) => {
  $('shakes').textContent = e.count;
  flash('shakesStat');
});
KmpMotion.addListener('motionError', (e) => {
  $('motionStatus').textContent = `Error ${e.code}: ${e.message}`;
  $('motionStatus').className = 'error';
});

async function start() {
  try {
    history.length = 0;
    ['steps', 'shakes'].forEach((id) => ($(id).textContent = '0'));
    $('cadence').textContent = '–';
    const session = await KmpMotion.startMotionUpdates({ source, intervalMs: 40 });
    running = true;
    $('startButton').textContent = 'Stop';
    $('motionStatus').textContent = `Streaming from ${session.source} source every ${session.intervalMs} ms`;
    $('motionStatus').className = 'muted';
  } catch (e) {
    $('motionStatus').textContent = `Error ${e.code ?? ''}: ${e.message}`;
    $('motionStatus').className = 'error';
  }
}

async function stop() {
  const summary = await KmpMotion.stopMotionUpdates();
  running = false;
  $('startButton').textContent = 'Start';
  $('motionStatus').textContent = `Stopped – ${summary.samples} samples, ${summary.steps} steps, ${summary.shakes} shakes`;
}

$('startButton').addEventListener('click', () => (running ? stop() : start()));

$('permissionButton').addEventListener('click', async () => {
  const { granted } = await KmpMotion.requestMotionPermission();
  $('permissionButton').hidden = granted;
  $('motionStatus').textContent = granted ? 'Motion access granted' : 'Motion access denied';
});

async function init() {
  // KmpMotion asks the KmpDevice feature – a direct Kotlin call inside the umbrella framework – whether
  // this is an emulator/simulator, and recommends a source accordingly.
  const availability = await KmpMotion.isMotionAvailable();
  $('availability').textContent = availability.device
    ? `accelerometer available${availability.isVirtualDevice ? ' (virtual device)' : ''}`
    : 'no accelerometer – simulated only';
  $('availability').classList.toggle('warn', !availability.device);
  $('sourceDevice').disabled = !availability.device;
  // iOS Safari requires an explicit permission request from a user gesture.
  $('permissionButton').hidden = !(platform === 'web' && typeof DeviceMotionEvent?.requestPermission === 'function');
  setSource(platform === 'web' ? 'simulated' : availability.recommendedSource);
}

loadDeviceInfo();
init();
