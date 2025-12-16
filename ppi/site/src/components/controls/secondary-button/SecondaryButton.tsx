import './SecondaryButton.scss';

interface SecondaryButtonProps {
  label: string;
  onClick: () => void;
  type?: "button" | "submit" | "reset";
}

function SecondaryButton({ label, onClick, type }: SecondaryButtonProps) {
  return (
    <div className="secondary-button-container">
      <button className="secondary-button"
        onClick={onClick}
        type={type}
      >
        {label}
      </button>
    </div>
  );
}

export default SecondaryButton;