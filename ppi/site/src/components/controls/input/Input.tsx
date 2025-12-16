import './Input.scss';

interface InputProps {
  placeholder?: string;
  picture?: string;
  type?: string;
  id?: string;
  name?: string;
  required?: boolean;
  value?: string;
  onChange?: (value: string) => void;
  min?: string;
  max?: string;
}

function Input({ placeholder, picture, type, id, name, required, value, onChange, min, max }: InputProps) {
  return (
    <div className="input-container">
      {picture && <img src={picture} alt="icon" className="input-icon" />}
      <input
        type={type || "text"}
        id={id}
        name={name}
        required={required}
        className="custom-input"
        placeholder={placeholder || "Введите текст..."}
        value={value}
        onChange={(e) => onChange?.(e.target.value)}
        min={min}
        max={max}
      />
    </div>
  );
}

export default Input;

