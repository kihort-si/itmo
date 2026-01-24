import { useState, useEffect } from 'react';
import { moduleApi } from '../services/api';
import type { ModuleSummary, ModuleDetails} from '../types/api';
import ModuleCard from '../components/ModuleCard';
import ModuleDetailsModal from '../components/ModuleDetailsModal';
import RegisterModuleModal from '../components/RegisterModuleModal';
import './ModulesPage.css';

function ModulesPage() {
  const [modules, setModules] = useState<ModuleSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedModule, setSelectedModule] = useState<ModuleDetails | null>(null);
  const [showRegisterModal, setShowRegisterModal] = useState(false);

  useEffect(() => {
    loadModules();
  }, []);

  const loadModules = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await moduleApi.list();
      setModules(data);
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || 'Не удалось загрузить список модулей';
      setError(errorMessage);
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleModuleClick = async (moduleId: number) => {
    try {
      setError(null);
      const details = await moduleApi.getDetails(moduleId);
      setSelectedModule(details);
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || 'Не удалось загрузить детали модуля';
      setError(errorMessage);
      console.error(err);
    }
  };

  const handleSync = async (moduleId: number) => {
    try {
      setError(null);
      const updated = await moduleApi.sync(moduleId);
      setSelectedModule(updated);
      await loadModules();
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || 'Не удалось синхронизировать модуль';
      setError(errorMessage);
      console.error(err);
    }
  };

  const handleRegister = async (baseUrl: string, name?: string) => {
    try {
      setError(null);
      await moduleApi.register({ baseUrl, name: name || null });
      setShowRegisterModal(false);
      await loadModules();
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || 'Не удалось зарегистрировать модуль';
      setError(errorMessage);
      console.error(err);
    }
  };

  const handleDelete = async (moduleId: number) => {
    try {
      setError(null);
      await moduleApi.delete(moduleId);
      setSelectedModule(null);
      await loadModules();
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || 'Не удалось удалить модуль';
      setError(errorMessage);
      console.error(err);
    }
  };

  if (loading) {
    return <div className="loading">Загрузка...</div>;
  }

  return (
    <div className="modules-page">
      <div className="page-header">
        <h2>Модули</h2>
        <button className="button" onClick={() => setShowRegisterModal(true)}>
          + Добавить модуль
        </button>
      </div>

      {error && <div className="error">{error}</div>}

      <div className="modules-grid">
        {modules.map((module) => (
          <ModuleCard
            key={module.id}
            module={module}
            onClick={() => handleModuleClick(module.id)}
          />
        ))}
      </div>

      {modules.length === 0 && (
        <div className="empty-state">
          <p>Модули не найдены. Добавьте первый модуль.</p>
        </div>
      )}

      {selectedModule && (
        <ModuleDetailsModal
          module={selectedModule}
          onClose={() => setSelectedModule(null)}
          onSync={handleSync}
          onDelete={handleDelete}
        />
      )}

      {showRegisterModal && (
        <RegisterModuleModal
          onClose={() => setShowRegisterModal(false)}
          onRegister={handleRegister}
        />
      )}
    </div>
  );
}

export default ModulesPage;

