import type { ModuleSummary } from '../types/api';
import './ModuleCard.css';

interface ModuleCardProps {
  module: ModuleSummary;
  onClick: () => void;
}

function ModuleCard({ module, onClick }: ModuleCardProps) {
  return (
    <div className="module-card" onClick={onClick}>
      <div className="module-card-header">
        <h3 className="module-card-title">
          {module.name || `Модуль #${module.moduleUid}`}
        </h3>
        <span className="module-card-id">ID: {module.moduleUid}</span>
      </div>
      <div className="module-card-body">
        <p className="module-card-description">Нажмите для настройки</p>
      </div>
    </div>
  );
}

export default ModuleCard;

