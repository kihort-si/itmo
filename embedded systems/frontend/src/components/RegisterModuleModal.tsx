import { useState } from 'react';
import './RegisterModuleModal.css';

interface RegisterModuleModalProps {
  onClose: () => void;
  onRegister: (baseUrl: string, name?: string) => void;
}

function RegisterModuleModal({ onClose, onRegister }: RegisterModuleModalProps) {
  const [baseUrl, setBaseUrl] = useState('');
  const [name, setName] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!baseUrl.trim()) return;
    onRegister(baseUrl.trim(), name.trim() || undefined);
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Регистрация модуля</h2>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        <form className="modal-body" onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="label">Base URL модуля *</label>
            <input
              type="text"
              className="input"
              value={baseUrl}
              onChange={(e) => setBaseUrl(e.target.value)}
              required
              placeholder="http://192.168.1.100"
            />
            <small style={{ color: 'var(--eco-primary)', fontSize: '0.85rem', marginTop: '0.25rem', display: 'block' }}>
              Введите URL модуля (например: http://192.168.1.100)
            </small>
          </div>

          <div className="form-group">
            <label className="label">Название (необязательно)</label>
            <input
              type="text"
              className="input"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Введите название модуля"
            />
          </div>

          <div className="modal-actions">
            <button type="button" className="button button-secondary" onClick={onClose}>
              Отмена
            </button>
            <button type="submit" className="button" disabled={!baseUrl.trim()}>
              Зарегистрировать
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default RegisterModuleModal;

