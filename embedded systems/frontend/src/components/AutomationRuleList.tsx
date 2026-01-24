import type { AutomationRule, ModuleSummary } from '../types/api';
import './AutomationRuleList.css';

interface AutomationRuleListProps {
  rules: AutomationRule[];
  modules: ModuleSummary[];
  onDelete: (ruleId: number) => void;
  onToggle: (ruleId: number) => void;
}

function AutomationRuleList({ rules, modules, onDelete, onToggle }: AutomationRuleListProps) {
  const getModuleName = (moduleId: number) => {
    const module = modules.find(m => m.id === moduleId);
    return module?.name || `Модуль #${module?.moduleUid || moduleId}`;
  };

  const getConditionLabel = (condition: AutomationRule['condition']) => {
    const labels: Record<AutomationRule['condition'], string> = {
      gt: '>',
      gte: '≥',
      lt: '<',
      lte: '≤',
      eq: '='
    };
    return labels[condition];
  };

  if (rules.length === 0) {
    return (
      <div className="empty-rules">
        <p>Правила автоматизации не настроены. Добавьте первое правило.</p>
      </div>
    );
  }

  return (
    <div className="rules-list">
      {rules.map((rule) => (
        <div key={rule.id} className={`rule-item ${!rule.enabled ? 'rule-disabled' : ''}`}>
          <div className="rule-header">
            <div className="rule-title-section">
              <h4 className="rule-title">{rule.name}</h4>
              <span className={`rule-status ${rule.enabled ? 'enabled' : 'disabled'}`}>
                {rule.enabled ? 'Включено' : 'Выключено'}
              </span>
            </div>
            <div className="rule-actions">
              <button
                className="button button-secondary"
                onClick={() => rule.id && onToggle(rule.id)}
              >
                {rule.enabled ? 'Выключить' : 'Включить'}
              </button>
              <button
                className="button button-danger"
                onClick={() => rule.id && onDelete(rule.id)}
              >
                Удалить
              </button>
            </div>
          </div>

          <div className="rule-body">
            <div className="rule-condition">
              <span className="rule-label">Если</span>
              <span className="rule-value">
                {getModuleName(rule.sourceModuleId)} → Порт {rule.sourcePortId}
              </span>
              <span className="rule-operator">{getConditionLabel(rule.condition)}</span>
              <span className="rule-value">{rule.threshold}</span>
            </div>

            <div className="rule-arrow">→</div>

            <div className="rule-action">
              <span className="rule-label">То</span>
              <span className="rule-value">
                {getModuleName(rule.targetModuleId)} → Порт {rule.targetPortId}
              </span>
              <span className="rule-value">на уровень {rule.actionLevel}</span>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}

export default AutomationRuleList;

