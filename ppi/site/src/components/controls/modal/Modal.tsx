import {useEffect, useRef} from 'react';
import {createPortal} from 'react-dom';
import './Modal.scss';

type ModalProps = {
  open: boolean;
  onClose: () => void;
  children: React.ReactNode;
  titleId?: string;
  closeOnBackdropClick?: boolean;
};

function Modal({open, onClose, children, titleId, closeOnBackdropClick = true}: ModalProps) {
  const panelRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [open, onClose]);

  useEffect(() => {
    if (!open) return;
    const prev = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => {
      document.body.style.overflow = prev;
    };
  }, [open]);

  useEffect(() => {
    if (!open) return;
    const el = panelRef.current?.querySelector<HTMLElement>(
      'input, button, select, textarea, [tabindex]:not([tabindex="-1"])'
    );
    el?.focus();
  }, [open]);

  if (!open) return null;

  return createPortal(
    <div
      className="modal-backdrop"
      role="presentation"
      onClick={closeOnBackdropClick ? onClose : undefined}
    >
      <div
        className="modal-panel"
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        onClick={e => e.stopPropagation()}
        ref={panelRef}
      >
        <button
          className="modal-close-button"
          onClick={onClose}
          aria-label="Закрыть"
          data-tooltip="Закрыть окно"
        >
          ×
        </button>
        {children}
      </div>
    </div>,
    document.body
  );
}

export default Modal;