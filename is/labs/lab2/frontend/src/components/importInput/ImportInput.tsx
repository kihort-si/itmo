import {useState} from "react";
import "./ImportInput.scss";

interface ImportInputProps {
  handleFileUpload: (file: File) => void;
}

function ImportInput({handleFileUpload}: ImportInputProps) {
  const [isDragging, setIsDragging] = useState(false);

  return(
    <div className="imort-input-container">
      <div
        className={`file-upload-section ${isDragging ? 'dragging' : ''}`}
        onDragOver={(e): void => {
          e.preventDefault();
          setIsDragging(true);
        }}
        onDragLeave={(): void => setIsDragging(false)}
        onDrop={(e): void => {
          e.preventDefault();
          setIsDragging(false);
          const file = e.dataTransfer.files?.[0];
          if (file && file.type === 'application/json') {
            handleFileUpload(file);
          } else {
            alert('Пожалуйста, загрузите файл формата .json');
          }
        }}
      >
        <div className="upload-icon">📁</div>
        <p className="upload-text">
          Загрузите данные из .json файла
        </p>
        <p className="upload-subtext">
          или перетащите файл в эту область
        </p>
        <div className="file-upload-wrapper">
          <input
            type="file"
            id="file-upload"
            accept=".json"
            onChange={(e): void => {
              const file = e.target.files?.[0];
              if (file) {
                handleFileUpload(file);
              }
            }}
          />
          <label htmlFor="file-upload" className="file-upload-button">
            Выбрать файл
          </label>
        </div>
      </div>
    </div>
  )
}

export default ImportInput;