import './ConfirmDialog.scss';
import PrimaryButton from "../primary-button/PrimaryButton.tsx";
import SecondaryButton from "../secondary-button/SecondaryButton.tsx";

interface ConfirmDialogProps {
  title: string;
  message: string;
  onConfirm: () => void;
  onCancel: () => void;
  titleId?: string;
}

function ConfirmDialog({ title, message, onConfirm, onCancel, titleId }: ConfirmDialogProps) {
  return (
    <div className="confirm-dialog-container">
      <h2 className="confirm-dialog-title" id={titleId}>
        {title}
      </h2>
      <p className="confirm-dialog-message">{message}</p>
      <div className="confirm-dialog-buttons">
        <SecondaryButton label="Да" onClick={onConfirm} />
        <PrimaryButton label="Нет" onClick={onCancel} />
      </div>
    </div>
  );
}

export default ConfirmDialog;