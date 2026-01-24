import { useState, useEffect } from 'react';
import { moduleApi } from '../services/api';
import type { Port, Driver, Binding } from '../types/api';
import { PortType, PortTypeLabels } from '../types/api';
import './PortConfig.css';

interface PortConfigProps {
  port: Port;
  drivers: Driver[];
  moduleId: number;
  onBind: (portId: number, driverId: number) => void;
}

function PortConfig({ port, drivers, moduleId, onBind }: PortConfigProps) {
  const [binding, setBinding] = useState<Binding | null>(null);
  const [selectedDriverId, setSelectedDriverId] = useState<number | ''>('');
  const [value, setValue] = useState<number | null>(null);
  const [writeLevel, setWriteLevel] = useState<number>(128);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadBinding();
  }, [moduleId, port.id]);

  const loadBinding = async () => {
    try {
      const bindingData = await moduleApi.getBinding(moduleId, port.id);
      setBinding(bindingData);
      if (bindingData) {
        setSelectedDriverId(bindingData.driverId);
      }
    } catch (err) {
      console.error('Failed to load binding', err);
    }
  };

  const handleBind = async () => {
    if (selectedDriverId === '') return;
    try {
      setError(null);
      await onBind(port.id, selectedDriverId as number);
      await loadBinding();
    } catch (err: any) {
      // Error is handled by parent, but we can also show it here
      const errorMessage = err.response?.data?.message || err.message;
      if (errorMessage) {
        setError(errorMessage);
      }
    }
  };

  const handleRead = async () => {
    if (port.type === PortType.OUTPUT) return;
    try {
      setLoading(true);
      setError(null);
      const measurement = await moduleApi.read(moduleId, port.id);
      setValue(measurement.value);
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || 'Не удалось прочитать значение';
      if (err.response?.status === 400) {
        setError('Порт не привязан к драйверу');
      } else if (err.response?.status === 405) {
        setError('Метод не поддерживается устройством');
      } else {
        setError(errorMessage);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleWrite = async () => {
    if (port.type !== PortType.OUTPUT) return;
    try {
      setLoading(true);
      setError(null);
      await moduleApi.write(moduleId, port.id, { level: writeLevel });
      setError(null);
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || 'Не удалось записать значение';
      if (err.response?.status === 400) {
        setError('Порт не привязан к драйверу');
      } else if (err.response?.status === 405) {
        setError('Метод не поддерживается устройством');
      } else {
        setError(errorMessage);
      }
    } finally {
      setLoading(false);
    }
  };

  const isInput = port.type === PortType.ANALOG_INPUT || port.type === PortType.DIGITAL_INPUT;
  const isOutput = port.type === PortType.OUTPUT;

  return (
    <div className="port-config">
      <div className="port-header">
        <div>
          <h4 className="port-title">Порт {port.id}</h4>
          <span className="port-type-badge">{PortTypeLabels[port.type]}</span>
        </div>
        {binding && (
          <span className="binding-badge">
            Привязан: {binding.driverName || `Драйвер #${binding.driverId}`}
          </span>
        )}
      </div>

      <div className="port-body">
        <div className="form-group">
          <label className="label">Драйвер</label>
          <select
            className="select"
            value={selectedDriverId}
            onChange={(e) => setSelectedDriverId(e.target.value ? Number(e.target.value) : '')}
          >
            <option value="">Выберите драйвер</option>
            {drivers.map((driver) => (
              <option key={driver.id} value={driver.id}>
                {driver.name} (ID: {driver.id})
              </option>
            ))}
          </select>
          <button
            className="button"
            onClick={handleBind}
            disabled={selectedDriverId === ''}
            style={{ marginTop: '0.5rem' }}
          >
            Привязать
          </button>
        </div>

        {binding && (
          <div className="port-controls">
            {isInput && (
              <div className="control-group">
                <button
                  className="button"
                  onClick={handleRead}
                  disabled={loading}
                >
                  {loading ? 'Чтение...' : 'Прочитать значение'}
                </button>
                {value !== null && (
                  <div className="value-display">
                    Значение: <strong>{value.toFixed(2)}</strong>
                  </div>
                )}
              </div>
            )}

            {isOutput && (
              <div className="control-group">
                <div className="form-group">
                  <label className="label">Уровень (0-255)</label>
                  <input
                    type="number"
                    className="input"
                    min="0"
                    max="255"
                    value={writeLevel}
                    onChange={(e) => setWriteLevel(Number(e.target.value))}
                  />
                </div>
                <button
                  className="button"
                  onClick={handleWrite}
                  disabled={loading}
                >
                  {loading ? 'Запись...' : 'Записать'}
                </button>
              </div>
            )}
          </div>
        )}

        {error && <div className="error">{error}</div>}
      </div>
    </div>
  );
}

export default PortConfig;

