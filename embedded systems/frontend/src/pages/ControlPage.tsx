import { useState, useEffect } from 'react';
import { moduleApi, automationApi } from '../services/api';
import type { ModuleSummary, AutomationRule } from '../types/api';
import AutomationRuleForm from '../components/AutomationRuleForm';
import AutomationRuleList from '../components/AutomationRuleList';
import './ControlPage.css';

function ControlPage() {
  const [modules, setModules] = useState<ModuleSummary[]>([]);
  const [rules, setRules] = useState<AutomationRule[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showAddRule, setShowAddRule] = useState(false);

  useEffect(() => {
    const init = async () => {
      try {
        setLoading(true);
        setError(null);
        await Promise.all([loadModules(), loadRules()]);
      } finally {
        setLoading(false);
      }
    };
    void init();
  }, []);

  const loadModules = async () => {
    try {
      const data = await moduleApi.list();
      setModules(data);
    } catch (err: any) {
      const errorMessage = err?.response?.data?.message || err?.message || 'Не удалось загрузить список модулей';
      setError(errorMessage);
      console.error(err);
    }
  };

  const loadRules = async () => {
    try {
      const data = await automationApi.list();
      setRules(data);
    } catch (err: any) {
      const errorMessage = err?.response?.data?.message || err?.message || 'Не удалось загрузить правила автоматизации';
      setError(errorMessage);
      console.error(err);
    }
  };

  const handleAddRule = async (rule: Omit<AutomationRule, 'id'>) => {
    try {
      setError(null);
      const created = await automationApi.create(rule);
      setRules((prev) => [...prev, created]);
      setShowAddRule(false);
    } catch (err: any) {
      const errorMessage = err?.response?.data?.message || err?.message || 'Не удалось создать правило';
      setError(errorMessage);
      console.error(err);
    }
  };

  const handleDeleteRule = async (ruleId: number) => {
    try {
      setError(null);
      await automationApi.delete(ruleId);
      setRules((prev) => prev.filter((r) => r.id !== ruleId));
    } catch (err: any) {
      const errorMessage = err?.response?.data?.message || err?.message || 'Не удалось удалить правило';
      setError(errorMessage);
      console.error(err);
    }
  };

  const handleToggleRule = async (ruleId: number) => {
    try {
      setError(null);
      const updated = await automationApi.toggle(ruleId);
      setRules((prev) => prev.map((r) => (r.id === ruleId ? updated : r)));
    } catch (err: any) {
      const errorMessage = err?.response?.data?.message || err?.message || 'Не удалось переключить правило';
      setError(errorMessage);
      console.error(err);
    }
  };

  if (loading) {
    return <div className="loading">Загрузка...</div>;
  }

  return (
    <div className="control-page">
      <div className="page-header">
        <h2>Управление</h2>
        <button className="button" onClick={() => setShowAddRule(true)}>
          + Добавить правило
        </button>
      </div>

      {error && <div className="error">{error}</div>}

      <div className="control-content">
        <div className="card">
          <h3 className="card-title">Правила автоматизации</h3>
          <p className="card-description">
            Настройте автоматические действия на основе показаний датчиков.
            Например: если температура выше 25°C, включить вентилятор на 50%.
          </p>

          <AutomationRuleList
            rules={rules}
            modules={modules}
            onDelete={handleDeleteRule}
            onToggle={handleToggleRule}
          />
        </div>
      </div>

      {showAddRule && (
        <AutomationRuleForm
          modules={modules}
          onClose={() => setShowAddRule(false)}
          onSave={handleAddRule}
        />
      )}
    </div>
  );
}

export default ControlPage;
