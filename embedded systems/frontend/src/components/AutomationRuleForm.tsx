import { useState, useEffect } from 'react';
import { moduleApi } from '../services/api';
import type { ModuleSummary, AutomationRule, Port } from '../types/api';
import './AutomationRuleForm.css';

interface AutomationRuleFormProps {
  modules: ModuleSummary[];
  onClose: () => void;
  onSave: (rule: Omit<AutomationRule, 'id'>) => void;
}

function AutomationRuleForm({ modules, onClose, onSave }: AutomationRuleFormProps) {
  const [name, setName] = useState('');
  const [sourceModuleId, setSourceModuleId] = useState<number | ''>('');
  const [sourcePortId, setSourcePortId] = useState<number | ''>('');
  const [condition, setCondition] = useState<AutomationRule['condition']>('gt');
  const [threshold, setThreshold] = useState<number>(0);
  const [targetModuleId, setTargetModuleId] = useState<number | ''>('');
  const [targetPortId, setTargetPortId] = useState<number | ''>('');
  const [actionLevel, setActionLevel] = useState<number>(128);
  const [enabled, setEnabled] = useState(true);

  const [sourcePorts, setSourcePorts] = useState<Port[]>([]);
  const [targetPorts, setTargetPorts] = useState<Port[]>([]);
  const [loadingPorts, setLoadingPorts] = useState(false);

  useEffect(() => {
    if (sourceModuleId) {
      loadPorts(sourceModuleId, true);
    } else {
      setSourcePorts([]);
      setSourcePortId('');
    }
  }, [sourceModuleId]);

  useEffect(() => {
    if (targetModuleId) {
      loadPorts(targetModuleId, false);
    } else {
      setTargetPorts([]);
      setTargetPortId('');
    }
  }, [targetModuleId]);

  const loadPorts = async (moduleId: number, isSource: boolean) => {
    try {
      setLoadingPorts(true);
      const ports = await moduleApi.getPorts(moduleId);
      if (isSource) {
        setSourcePorts(ports);
      } else {
        setTargetPorts(ports);
      }
    } catch (err) {
      console.error('Failed to load ports', err);
    } finally {
      setLoadingPorts(false);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (sourceModuleId === '' || sourcePortId === '' || targetModuleId === '' || targetPortId === '') {
      return;
    }

    onSave({
      name: name || `Правило ${new Date().toLocaleString()}`,
      sourceModuleId: sourceModuleId as number,
      sourcePortId: sourcePortId as number,
      condition,
      threshold,
      targetModuleId: targetModuleId as number,
      targetPortId: targetPortId as number,
      actionLevel,
      enabled
    });
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content modal-content-large" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Новое правило автоматизации</h2>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        <form className="modal-body" onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="label">Название правила</label>
            <input
              type="text"
              className="input"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Например: Включить вентилятор при высокой температуре"
            />
          </div>

          <div className="rule-section">
            <h3 className="section-subtitle">Источник (датчик)</h3>
            <div className="form-row">
              <div className="form-group">
                <label className="label">Модуль *</label>
                <select
                  className="select"
                  value={sourceModuleId}
                  onChange={(e) => setSourceModuleId(e.target.value ? Number(e.target.value) : '')}
                  required
                >
                  <option value="">Выберите модуль</option>
                  {modules.map((module) => (
                    <option key={module.id} value={module.id}>
                      {module.name || `Модуль #${module.moduleUid}`}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label className="label">Порт *</label>
                <select
                  className="select"
                  value={sourcePortId}
                  onChange={(e) => setSourcePortId(e.target.value ? Number(e.target.value) : '')}
                  required
                  disabled={!sourceModuleId || loadingPorts}
                >
                  <option value="">Выберите порт</option>
                  {sourcePorts.map((port) => (
                    <option key={port.id} value={port.id}>
                      Порт {port.id}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </div>

          <div className="rule-section">
            <h3 className="section-subtitle">Условие</h3>
            <div className="form-row">
              <div className="form-group">
                <label className="label">Оператор</label>
                <select
                  className="select"
                  value={condition}
                  onChange={(e) => setCondition(e.target.value as AutomationRule['condition'])}
                >
                  <option value="gt">Больше (&gt;)</option>
                  <option value="gte">Больше или равно (&gt;=)</option>
                  <option value="lt">Меньше (&lt;)</option>
                  <option value="lte">Меньше или равно (&lt;=)</option>
                  <option value="eq">Равно (=)</option>
                </select>
              </div>

              <div className="form-group">
                <label className="label">Пороговое значение *</label>
                <input
                  type="number"
                  className="input"
                  value={threshold}
                  onChange={(e) => setThreshold(Number(e.target.value))}
                  required
                  step="0.1"
                />
              </div>
            </div>
          </div>

          <div className="rule-section">
            <h3 className="section-subtitle">Цель (устройство управления)</h3>
            <div className="form-row">
              <div className="form-group">
                <label className="label">Модуль *</label>
                <select
                  className="select"
                  value={targetModuleId}
                  onChange={(e) => setTargetModuleId(e.target.value ? Number(e.target.value) : '')}
                  required
                >
                  <option value="">Выберите модуль</option>
                  {modules.map((module) => (
                    <option key={module.id} value={module.id}>
                      {module.name || `Модуль #${module.moduleUid}`}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label className="label">Порт *</label>
                <select
                  className="select"
                  value={targetPortId}
                  onChange={(e) => setTargetPortId(e.target.value ? Number(e.target.value) : '')}
                  required
                  disabled={!targetModuleId || loadingPorts}
                >
                  <option value="">Выберите порт</option>
                  {targetPorts.map((port) => (
                    <option key={port.id} value={port.id}>
                      Порт {port.id}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label className="label">Уровень (0-255) *</label>
                <input
                  type="number"
                  className="input"
                  min="0"
                  max="255"
                  value={actionLevel}
                  onChange={(e) => setActionLevel(Number(e.target.value))}
                  required
                />
              </div>
            </div>
          </div>

          <div className="form-group">
            <label className="label checkbox-label">
              <input
                type="checkbox"
                checked={enabled}
                onChange={(e) => setEnabled(e.target.checked)}
              />
              <span>Включено</span>
            </label>
          </div>

          <div className="modal-actions">
            <button type="button" className="button button-secondary" onClick={onClose}>
              Отмена
            </button>
            <button
              type="submit"
              className="button"
              disabled={sourceModuleId === '' || sourcePortId === '' || targetModuleId === '' || targetPortId === ''}
            >
              Сохранить
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default AutomationRuleForm;

