import './PrimaryButton.scss';

interface PrimaryButtonProps {
  label: string;
  onClick: () => void;
  type?: "button" | "submit" | "reset";
}

function PrimaryButton({ label, onClick, type }: PrimaryButtonProps) {
  return (
    <button className="primary-button"
      onClick={onClick}
      type={type}
    >
      {label}
    </button>
  );
}

export default PrimaryButton;