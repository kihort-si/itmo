import './ErrorModal.scss';

interface ErrorModalProps {
  isOpen: boolean;
  onClose: () => void;
  error: string;
}

function ErrorModal({ isOpen, onClose, error }: ErrorModalProps) {
  if (!isOpen) return null;

  return (
    <div className="error-modal-overlay" onClick={onClose}>
      <div className="error-modal" onClick={(e) => e.stopPropagation()}>
        <div className="error-modal-header">
          <h2>Ошибка</h2>
          <button className="close-button" onClick={onClose}>×</button>
        </div>
        <div className="error-modal-body">
          <p>{error}</p>
        </div>
        <div className="error-modal-footer">
          <button onClick={onClose}>Закрыть</button>
        </div>
      </div>
    </div>
  );
}

export default ErrorModal;