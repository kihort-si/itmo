import React, {useEffect, useState} from "react";
import type {PersonRequestDto, PersonResponseDto} from "../../services/PersonService.ts";
import "./PersonModal.scss";

interface PersonModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (person: PersonRequestDto) => Promise<void>;
  person?: PersonResponseDto | null;
  mode: 'create' | 'edit';
}

interface FormData {
  name: string;
  eyeColor?: string;
  hairColor?: string;
  location: {
    x: string;
    y: string;
    z: string;
    name: string;
  };
  passportID: string;
  nationality: string;
}

interface FormErrors {
  [key: string]: string;
}

const COLORS = ['GREEN', 'BLUE', 'YELLOW', 'ORANGE'];
const COUNTRIES = ['UNITED_KINGDOM', 'GERMANY', 'THAILAND']

const PersonModal: React.FC<PersonModalProps> = ({ isOpen, onClose, onSave, person, mode }) => {
  const [formData, setFormData] = useState<FormData>({
    name: '',
    eyeColor: '',
    hairColor: '',
    location: {
      x: '',
      y: '',
      z: '',
      name: ''
    },
    passportID: '',
    nationality: ''
  });

  const [errors, setErrors] = useState<FormErrors>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (person && mode === 'edit') {
      setFormData({
        name: person.name || '',
        eyeColor: person.eyeColor || '',
        hairColor: person.hairColor || '',
        location: {
          x: person.location?.x?.toString() || '',
          y: person.location?.y?.toString() || '',
          z: person.location?.z?.toString() || '',
          name: person.location?.name || ''
        },
        passportID: person.passportID || '',
        nationality: person.nationality || ''
      });
    } else {
      setFormData({
        name: '',
        eyeColor: '',
        hairColor: '',
        location: {
          x: '',
          y: '',
          z: '',
          name: ''
        },
        passportID: '',
        nationality: ''
      });
    }
    setErrors({});
  }, [person, mode, isOpen]);

  const validateForm = (): boolean => {
    const newErrors: FormErrors = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Имя обязательно';
    }

    if (!formData.nationality.trim()) {
      newErrors.nationality = 'Национальность обязательна';
    }

    if (!formData.location.x) {
      newErrors['location.x'] = 'X координата обязательна';
    }

    if (!formData.location.y) {
      newErrors['location.y'] = 'Y координата обязательна';
    }

    if (!formData.location.z) {
      newErrors['location.z'] = 'Z координата обязательна';
    }

    if (!formData.location.name.trim()) {
      newErrors['location.name'] = 'Введите название локации';
    }

    if (!formData.passportID) {
      newErrors.passportID = 'Введите номер паспорта';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setIsSubmitting(true);

    try {
      const personData: PersonRequestDto = {
        name: formData.name,
        nationality: formData.nationality,
        location: {
          x: parseFloat(formData.location.x),
          y: parseFloat(formData.location.y),
          z: parseFloat(formData.location.z),
          name: formData.location.name
        },
        ...(formData.eyeColor && { eyeColor: formData.eyeColor }),
        ...(formData.hairColor && { hairColor: formData.hairColor }),
        ...(formData.passportID && { passportID: formData.passportID })
      };

      await onSave(personData);
      onClose();
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Неизвестная ошибка';

      if (errorMessage.includes('PassportID')) {
        setErrors(prev => ({
          ...prev,
          passportID: errorMessage
        }));
      } else if (errorMessage.includes('именем') && errorMessage.includes('локацией')) {
        setErrors(prev => ({
          ...prev,
          name: errorMessage
        }));
      } else {
        setErrors(prev => ({
          ...prev,
          name: errorMessage
        }));
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleInputChange = (field: string, value: string) => {
    if (field.includes('.')) {
      const [parent, child] = field.split('.');
      setFormData(prev => {
        if (parent === 'location') {
          return {
            ...prev,
            location: {
              ...prev.location,
              [child]: value
            }
          };
        }
        return prev;
      });
    } else {
      setFormData(prev => ({
        ...prev,
        [field]: value
      }));
    }

    if (errors[field]) {
      setErrors(prev => ({
        ...prev,
        [field]: ''
      }));
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{mode === 'create' ? 'Добавить человека' : 'Редактировать человека'}</h2>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="name">Имя *</label>
              <input
                id="name"
                type="text"
                value={formData.name}
                onChange={(e) => handleInputChange('name', e.target.value)}
                className={errors.name ? 'error' : ''}
                placeholder="Введите имя"
              />
              {errors.name && <span className="error-message">{errors.name}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="passportID">Номер паспорта</label>
              <input
                id="passportID"
                type="text"
                maxLength={47}
                value={formData.passportID}
                onChange={(e) => handleInputChange('passportID', e.target.value)}
                className={errors.passportID ? 'error' : ''}
                placeholder="Введите номер паспорта"
              />
              {errors.passportID && <span className="error-message">{errors.passportID}</span>}
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="eyeColor">Цвет глаз</label>
              <select
                id="eyeColor"
                value={formData.eyeColor}
                onChange={(e) => handleInputChange('eyeColor', e.target.value)}
              >
                <option value="">Выберите цвет глаз</option>
                {COLORS.map(color => (
                  <option key={color} value={color}>{color}</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="hairColor">Цвет волос</label>
              <select
                id="hairColor"
                value={formData.hairColor}
                onChange={(e) => handleInputChange('hairColor', e.target.value)}
              >
                <option value="">Выберите цвет волос</option>
                {COLORS.map(color => (
                  <option key={color} value={color}>{color}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="nationality">Национальность *</label>
              <select
                id="nationality"
                value={formData.nationality}
                onChange={(e) => handleInputChange('nationality', e.target.value)}
                className={errors['nationality'] ? 'error' : ''}
              >
                <option value="">Выберите национальность</option>
                {COUNTRIES.map(country => (
                  <option key={country} value={country}>{country}</option>
                ))}
              </select>
              {errors['nationality'] && <span className="error-message">{errors['nationality']}</span>}
            </div>
          </div>

          <h3 className="section-title">Местоположение</h3>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="location.x">Координата X *</label>
              <input
                id="location.x"
                type="number"
                step="0.01"
                value={formData.location.x}
                onChange={(e) => handleInputChange('location.x', e.target.value)}
                className={errors['location.x'] ? 'error' : ''}
                placeholder="Введите X координату"
              />
              {errors['location.x'] && <span className="error-message">{errors['location.x']}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="location.y">Координата Y *</label>
              <input
                id="location.y"
                type="number"
                step="0.01"
                value={formData.location.y}
                onChange={(e) => handleInputChange('location.y', e.target.value)}
                className={errors['location.y'] ? 'error' : ''}
                placeholder="Введите Y координату"
              />
              {errors['location.y'] && <span className="error-message">{errors['location.y']}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="location.z">Координата Z *</label>
              <input
                id="location.z"
                type="number"
                step="0.01"
                value={formData.location.z}
                onChange={(e) => handleInputChange('location.z', e.target.value)}
                className={errors['location.z'] ? 'error' : ''}
                placeholder="Введите Z координату"
              />
              {errors['location.z'] && <span className="error-message">{errors['location.z']}</span>}
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="location.name">Название локации *</label>
              <input
                id="location.name"
                type="text"
                value={formData.location.name}
                onChange={(e) => handleInputChange('location.name', e.target.value)}
                className={errors['location.name'] ? 'error' : ''}
                placeholder="Введите название локации"
              />
              {errors['location.name'] && <span className="error-message">{errors['location.name']}</span>}
            </div>
          </div>

          <span className="caption">* - обязательные поля</span>

          <div className="modal-footer">
            <button type="button" onClick={onClose} className="btn-cancel">
              Отменить
            </button>
            <button type="submit" disabled={isSubmitting} className="btn-save">
              {isSubmitting ? 'Сохранение...' : 'Сохранить'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default PersonModal;