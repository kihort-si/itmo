import { useState, useEffect } from 'react';
import { moduleApi } from '../services/api';
import type { ModuleDetails, Driver, Port } from '../types/api';
import PortConfig from './PortConfig';
import './ModuleDetailsModal.css';

interface ModuleDetailsModalProps {
  module: ModuleDetails;
  onClose: () => void;
  onSync: (moduleId: number) => void;
  onDelete?: (moduleId: number) => void;
}

function ModuleDetailsModal({ module, onClose, onSync, onDelete }: ModuleDetailsModalProps) {
  const [moduleDetails, setModuleDetails] = useState<ModuleDetails>(module);
  const [drivers, setDrivers] = useState<Driver[]>([]);
  const [ports, setPorts] = useState<Port[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadModuleData();
  }, [module.id]);

  const loadModuleData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const [details, driversData, portsData] = await Promise.all([
        moduleApi.getDetails(module.id),
        moduleApi.getDrivers(module.id).catch(() => []),
        moduleApi.getPorts(module.id).catch(() => [])
      ]);

      setModuleDetails(details);
      setDrivers(driversData);
      setPorts(portsData);
    } catch (err) {
      setError('Не удалось загрузить данные модуля');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSync = async () => {
    try {
      await onSync(module.id);
      await loadModuleData();
    } catch (err) {
      setError('Не удалось синхронизировать модуль');
    }
  };

  const handleBind = async (portId: number, driverId: number) => {
    try {
      setError(null);
      await moduleApi.bind(module.id, portId, { driverId });
      await loadModuleData();
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || 'Не удалось привязать драйвер';
      if (err.response?.status === 409) {
        setError('Драйвер несовместим с типом порта');
      } else if (err.response?.status === 422) {
        setError('Драйвер или порт не найдены');
      } else {
        setError(errorMessage);
      }
      console.error(err);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{moduleDetails.name || `Модуль #${moduleDetails.moduleUid}`}</h2>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        <div className="modal-body">
          {error && <div className="error">{error}</div>}

          <div className="module-info">
            <div className="info-item">
              <span className="info-label">ID модуля:</span>
              <span className="info-value">{moduleDetails.moduleUid}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Base URL:</span>
              <span className="info-value" style={{ fontSize: '0.9rem' }}>{moduleDetails.baseUrl}</span>
            </div>
            <div className="module-actions">
              <button className="button" onClick={handleSync}>
                Синхронизировать
              </button>
              {onDelete && (
                <button 
                  className="button button-danger" 
                  onClick={() => {
                    if (window.confirm(`Вы уверены, что хотите удалить модуль "${moduleDetails.name || `#${moduleDetails.moduleUid}`}"?`)) {
                      onDelete(module.id);
                    }
                  }}
                >
                  Удалить модуль
                </button>
              )}
            </div>
          </div>

          {loading ? (
            <div className="loading">Загрузка...</div>
          ) : (
            <>
              <h3 className="section-title">Порты</h3>
              <div className="ports-list">
                {(ports.length > 0 ? ports : (moduleDetails.ports || [])).map((port) => (
                  <PortConfig
                    key={port.id}
                    port={port}
                    drivers={(drivers.length > 0 ? drivers : (moduleDetails.drivers || [])).filter(d => d.type === port.type)}
                    moduleId={module.id}
                    onBind={handleBind}
                  />
                ))}
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

export default ModuleDetailsModal;

