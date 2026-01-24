import express from 'express';
import cors from 'cors';

const app = express();
app.use(cors());
app.use(express.json());

// In-memory state
const state = {
  moduleId: 1001,
  temp: 23.5,
  ports: [
    { id: 1, type: 0 }, // analog input (temperature)
    { id: 2, type: 2 }, // output (e.g., relay)
  ],
  drivers: [
    { id: 1, name: 'TempSensor', type: 0 },
    { id: 2, name: 'Relay', type: 2 },
  ],
  bindings: new Map(), // portId -> driverId
  outputs: new Map(),  // portId -> level (0..255)
};

// Helpers
function findPort(portId) {
  return state.ports.find((p) => p.id === portId);
}
function findDriver(driverId) {
  return state.drivers.find((d) => d.id === driverId);
}

// Device API
app.get('/info', (req, res) => {
  res.json({ 'module-id': state.moduleId, moduleId: state.moduleId });
});

app.get('/drivers', (req, res) => {
  res.json({ drivers: state.drivers });
});

app.get('/ports', (req, res) => {
  res.json({ ports: state.ports });
});

app.put('/ports/:port/bind', (req, res) => {
  const portId = Number(req.params.port);
  const { driverId } = req.body || {};
  const port = findPort(portId);
  const driver = findDriver(driverId);
  if (!port || !driver) {
    return res.status(422).json({ message: 'Driver or port not found' });
  }
  if (port.type !== driver.type) {
    return res.status(409).json({ message: 'Driver incompatible with port type' });
  }
  state.bindings.set(portId, driverId);
  res.status(200).json({ driverId });
});

app.get('/ports/:port/bind', (req, res) => {
  const portId = Number(req.params.port);
  if (!findPort(portId)) {
    return res.status(422).json({ message: 'Port not found' });
  }
  if (!state.bindings.has(portId)) {
    return res.status(400).json({ message: 'Port not bound' });
  }
  res.json({ driverId: state.bindings.get(portId) });
});

app.get('/ports/:port/control', (req, res) => {
  const portId = Number(req.params.port);
  const port = findPort(portId);
  if (!port) {
    return res.status(422).json({ message: 'Port not found' });
  }
  if (!state.bindings.has(portId)) {
    return res.status(400).json({ message: 'Port not bound' });
  }
  // For input port id=1 return temperature value
  if (port.type === 0) {
    return res.json({ value: state.temp });
  }
  // For output ports reading is not supported in our mock
  return res.status(405).json({ message: 'Method not supported for this device' });
});

app.post('/ports/:port/control', (req, res) => {
  const portId = Number(req.params.port);
  const port = findPort(portId);
  if (!port) {
    return res.status(422).json({ message: 'Port not found' });
  }
  if (!state.bindings.has(portId)) {
    return res.status(400).json({ message: 'Port not bound' });
  }
  const { level } = req.body || {};
  if (typeof level !== 'number' || level < 0 || level > 255) {
    return res.status(400).json({ message: 'Invalid level' });
  }
  if (port.type !== 2) {
    return res.status(405).json({ message: 'Method not supported for this device' });
  }
  state.outputs.set(portId, level);
  res.status(200).json({ ok: true });
});

// Debug endpoints to view/change temperature
app.get('/debug/temp', (req, res) => {
  res.json({ value: state.temp });
});

app.post('/debug/temp', (req, res) => {
  const { value } = req.body || {};
  if (typeof value !== 'number') {
    return res.status(400).json({ message: 'value must be number' });
  }
  state.temp = value;
  res.json({ value: state.temp });
});

const PORT = process.env.PORT || 8081;
app.listen(PORT, () => {
  console.log(`ESP mock listening on http://localhost:${PORT}`);
});
