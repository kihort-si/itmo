import React, { useState, useEffect } from 'react';
import './MovieModal.scss';
import type {MovieRequestDto, MovieResponseDto} from "../../services/MovieService.ts";
import PersonService, { type PersonResponseDto } from "../../services/PersonService.ts";

interface MovieModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (movie: MovieRequestDto) => Promise<void>;
  movie?: MovieResponseDto | null;
  mode: 'create' | 'edit';
}

interface FormData {
  name: string;
  oscarsCount: string;
  budget?: string;
  totalBoxOffice?: string;
  mpaaRating: string;
  length: string;
  goldenPalmCount: string;
  genre?: string;
  directorId: string;
  screenwriterId?: string;
  operatorId?: string;
  coordinates: {
    x: string;
    y: string;
  };
}

interface FormErrors {
  [key: string]: string;
}

const MPAA_RATINGS = ['G', 'PG', 'PG_13', 'R', 'NC_17'];
const GENRES = ['COMEDY', 'ADVENTURE', 'TRAGEDY', 'HORROR'];

const MovieModal: React.FC<MovieModalProps> = ({ isOpen, onClose, onSave, movie, mode }) => {
  const [formData, setFormData] = useState<FormData>({
    name: '',
    budget: '',
    totalBoxOffice: '',
    mpaaRating: '',
    length: '',
    goldenPalmCount: '',
    genre: '',
    directorId: '',
    operatorId: '',
    screenwriterId: '',
    oscarsCount: '',
    coordinates: {
      x: '',
      y: ''
    }
  });

  const [errors, setErrors] = useState<FormErrors>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [people, setPeople] = useState<PersonResponseDto[]>([]);
  const [focusedField, setFocusedField] = useState<string | null>(null);

  useEffect(() => {
    const fetchPeople = async () => {
      try {
        const peopleData = await PersonService.getPeople();
        setPeople(peopleData);
      } catch (error) {
        console.error('Ошибка при загрузке фильмов:', error);
      }
    };

    if (isOpen) {
      fetchPeople();
    }
  }, [isOpen]);

  useEffect(() => {
    if (movie && mode === 'edit') {
      setFormData({
        name: movie.name || '',
        oscarsCount: movie.oscarsCount?.toString() || '',
        budget: movie.budget?.toString() || '',
        totalBoxOffice: movie.totalBoxOffice?.toString() || '',
        mpaaRating: movie.mpaaRating || '',
        length: movie.length?.toString() || '',
        goldenPalmCount: movie.goldenPalmCount?.toString() || '',
        genre: movie.genre || '',
        directorId: movie.director?.id?.toString() || '',
        screenwriterId: movie.screenwriter?.id?.toString() || '',
        operatorId: movie.operator?.id?.toString() || '',
        coordinates: {
          x: movie.coordinates?.x?.toString() || '',
          y: movie.coordinates?.y?.toString() || ''
        }
      });
    } else {
      setFormData({
        name: '',
        oscarsCount: '',
        budget: '',
        totalBoxOffice: '',
        mpaaRating: '',
        length: '',
        goldenPalmCount: '',
        genre: '',
        directorId: '',
        screenwriterId: '',
        operatorId: '',
        coordinates: {
          x: '',
          y: ''
        }
      });
    }
    setErrors({});
  }, [movie, mode, isOpen]);

  const validateForm = (): boolean => {
    const newErrors: FormErrors = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Введите название фильма';
    }

    if (!formData.oscarsCount) {
      newErrors.oscarsCount = 'Введите количество оскаров';
    }

    if (formData.oscarsCount && parseInt(formData.oscarsCount) < 0) {
      newErrors.oscarsCount = 'Количество не может быть отрицательным';
    }

    if (formData.budget && parseInt(formData.budget) <= 0) {
      newErrors.budget = 'Бюджет должен быть положительным';
    }

    if (formData.budget && !parseInt(formData.budget)) {
      newErrors.budget = 'Бюджет должен быть целым числом';
    }

    if (formData.totalBoxOffice && parseInt(formData.totalBoxOffice) <= 0) {
      newErrors.totalBoxOffice = 'Сборы должны быть положительными';
    }

    if (formData.totalBoxOffice && !parseInt(formData.totalBoxOffice)) {
      newErrors.totalBoxOffice = 'Сборы должны быть целым числом';
    }

    if (!formData.mpaaRating) {
      newErrors.mpaaRating = 'Выберите возрастной рейтинг';
    }

    if (!formData.directorId) {
      newErrors.directorId = 'Укажите режиссера';
    }

    if (!formData.length) {
      newErrors.length = 'Введите длительность фильма';
    }

    if (formData.length && parseInt(formData.length) <= 0) {
      newErrors.length = 'Длительность должна быть больше нуля';
    }

    if (!formData.goldenPalmCount) {
      newErrors.goldenPalmCount = 'Введите количество Золотых Пальмовых ветвей';
    }

    if (formData.goldenPalmCount && parseInt(formData.goldenPalmCount) < 0) {
      newErrors.goldenPalmCount = 'Количество не может быть отрицательным';
    }

    if (!formData.coordinates.x) {
      newErrors['coordinates.x'] = 'X координата обязательна';
    }

    if (formData.coordinates.x && parseFloat(formData.coordinates.x) > 347) {
      newErrors['coordinates.x'] = 'X должен быть меньше 347';
    }

    if (!formData.coordinates.y) {
      newErrors['coordinates.y'] = 'Y координата обязательна';
    }

    if (formData.coordinates.y && !parseInt(formData.coordinates.y)) {
      newErrors['coordinates.y'] = 'Y должен быть целым числом';
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
      const movieData: MovieRequestDto = {
        name: formData.name,
        ...(formData.budget && formData.budget.trim() && { budget: parseInt(formData.budget) }),
        ...(formData.totalBoxOffice && formData.totalBoxOffice.trim() && { totalBoxOffice: parseInt(formData.totalBoxOffice) }),
        ...(formData.mpaaRating && { mpaaRating: formData.mpaaRating }),
        ...(formData.length && formData.length.trim() && { length: parseInt(formData.length) }),
        ...(formData.goldenPalmCount && formData.goldenPalmCount.trim() && { goldenPalmCount: parseInt(formData.goldenPalmCount) }),
        ...(formData.oscarsCount && formData.oscarsCount.trim() && { oscarsCount: parseInt(formData.oscarsCount) }),
        ...(formData.genre && { genre: formData.genre }),
        ...(formData.directorId && { directorId: parseInt(formData.directorId) }),
        ...(formData.screenwriterId && formData.screenwriterId.trim() && { screenwriterId: parseInt(formData.screenwriterId) }),
        ...(formData.operatorId && formData.operatorId.trim() && { operatorId: parseInt(formData.operatorId) }),
        coordinates: {
          x: parseFloat(formData.coordinates.x),
          y: parseInt(formData.coordinates.y)
        }
      };

      await onSave(movieData);
      onClose();
    } catch (error) {
      console.error('Ошибка при сохранении:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleInputChange = (field: string, value: string) => {
    const cleanValue = (field === 'budget' || field === 'totalBoxOffice')
      ? value.replace(/[^\d]/g, '')
      : value;

    if (field.includes('.')) {
      const [parent, child] = field.split('.');
      setFormData(prev => {
        if (parent === 'coordinates') {
          return {
            ...prev,
            coordinates: {
              ...prev.coordinates,
              [child]: cleanValue
            }
          };
        }
        return prev;
      });
    } else {
      setFormData(prev => ({
        ...prev,
        [field]: cleanValue
      }));
    }

    if (errors[field]) {
      setErrors(prev => ({
        ...prev,
        [field]: ''
      }));
    }
  };

  const getDisplayValue = (field: 'budget' | 'totalBoxOffice'): string => {
    const value = formData[field];
    if (!value) return '';

    if (focusedField === field) {
      return value;
    }

    return formatCurrency(value);
  };

  const formatCurrency = (value: string): string => {
    if (!value) return '';
    const number = parseFloat(value.replace(/\s/g, ''));
    if (isNaN(number)) return '';
    return number.toLocaleString('ru-RU') + ' $';
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{mode === 'create' ? 'Добавить фильм' : 'Редактировать фильм'}</h2>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="name">Название *</label>
              <input
                id="name"
                type="text"
                value={formData.name}
                onChange={(e) => handleInputChange('name', e.target.value)}
                className={errors.name ? 'error' : ''}
                placeholder="Введите название фильма"
              />
              {errors.name && <span className="error-message">{errors.name}</span>}
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="coordinates.x">Координата X *</label>
              <input
                id="coordinates.x"
                type="number"
                step="0.01"
                value={formData.coordinates.x}
                onChange={(e) => handleInputChange('coordinates.x', e.target.value)}
                className={errors['coordinates.x'] ? 'error' : ''}
                placeholder="Введите X координату"
              />
              {errors['coordinates.x'] && <span className="error-message">{errors['coordinates.x']}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="coordinates.y">Координата Y *</label>
              <input
                id="coordinates.y"
                type="number"
                step="0.01"
                value={formData.coordinates.y}
                onChange={(e) => handleInputChange('coordinates.y', e.target.value)}
                className={errors['coordinates.y'] ? 'error' : ''}
                placeholder="Введите Y координату"
              />
              {errors['coordinates.y'] && <span className="error-message">{errors['coordinates.y']}</span>}
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="budget">Бюджет</label>
              <input
                id="budget"
                type="text"
                value={getDisplayValue('budget')}
                onChange={(e) => handleInputChange('budget', e.target.value)}
                onFocus={() => setFocusedField('budget')}
                onBlur={() => setFocusedField(null)}
                className={errors.budget ? 'error' : ''}
                placeholder="Введите бюджет"
              />
              {errors.budget && <span className="error-message">{errors.budget}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="totalBoxOffice">Сборы в мире</label>
              <input
                id="totalBoxOffice"
                type="text"
                value={getDisplayValue('totalBoxOffice')}
                onChange={(e) => handleInputChange('totalBoxOffice', e.target.value)}
                onFocus={() => setFocusedField('totalBoxOffice')}
                onBlur={() => setFocusedField(null)}
                className={errors.totalBoxOffice ? 'error' : ''}
                placeholder="Введите сборы"
              />
              {errors.totalBoxOffice && <span className="error-message">{errors.totalBoxOffice}</span>}
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="mpaaRating">Возрастной рейтинг *</label>
              <select
                id="mpaaRating"
                value={formData.mpaaRating}
                onChange={(e) => handleInputChange('mpaaRating', e.target.value)}
                className={errors.mpaaRating ? 'error' : ''}
              >
                <option value="">Выберите рейтинг</option>
                {MPAA_RATINGS.map(rating => (
                  <option key={rating} value={rating}>{rating}</option>
                ))}
              </select>
              {errors.mpaaRating && <span className="error-message">{errors.mpaaRating}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="genre">Жанр</label>
              <select
                id="genre"
                value={formData.genre}
                onChange={(e) => handleInputChange('genre', e.target.value)}
                className={errors.genre ? 'error' : ''}
              >
                <option value="">Выберите жанр</option>
                {GENRES.map(genre => (
                  <option key={genre} value={genre}>{genre}</option>
                ))}
              </select>
              {errors.genre && <span className="error-message">{errors.genre}</span>}
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="directorId">Режиссер *</label>
              <select
                id="directorId"
                value={formData.directorId}
                onChange={(e) => handleInputChange('directorId', e.target.value)}
                className={errors.directorId ? 'error' : ''}
              >
                <option value="">Выберите режиссера</option>
                {people.map(person => (
                  <option key={person.id} value={person.id}>{person.name}</option>
                ))}
              </select>
              {errors.directorId && <span className="error-message">{errors.directorId}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="screenwriterId">Сценарист</label>
              <select
                id="screenwriterId"
                value={formData.screenwriterId}
                onChange={(e) => handleInputChange('screenwriterId', e.target.value)}
                className={errors.screenwriterId ? 'error' : ''}
              >
                <option value="">Выберите сценариста</option>
                {people.map(person => (
                  <option key={person.id} value={person.id}>{person.name}</option>
                ))}
              </select>
              {errors.screenwriterId && <span className="error-message">{errors.screenwriterId}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="operatorId">Оператор</label>
              <select
                id="operatorId"
                value={formData.operatorId}
                onChange={(e) => handleInputChange('operatorId', e.target.value)}
                className={errors.operatorId ? 'error' : ''}
              >
                <option value="">Выберите оператора</option>
                {people.map(person => (
                  <option key={person.id} value={person.id}>{person.name}</option>
                ))}
              </select>
              {errors.operatorId && <span className="error-message">{errors.operatorId}</span>}
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="length">Длительность (мин) *</label>
              <input
                id="length"
                type="number"
                step="1"
                min="1"
                value={formData.length}
                onChange={(e) => handleInputChange('length', e.target.value)}
                className={errors.length ? 'error' : ''}
                placeholder="Введите длительность"
              />
              {errors.length && <span className="error-message">{errors.length}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="oscarsCount">Оскары *</label>
              <input
                id="oscarsCount"
                type="number"
                step="1"
                min="0"
                value={formData.oscarsCount}
                onChange={(e) => handleInputChange('oscarsCount', e.target.value)}
                className={errors.oscarsCount ? 'error' : ''}
                placeholder="Введите количество"
              />
              {errors.oscarsCount && <span className="error-message">{errors.oscarsCount}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="goldenPalmCount">Золотые пальмовые ветви *</label>
              <input
                id="goldenPalmCount"
                type="number"
                step="1"
                min="0"
                value={formData.goldenPalmCount}
                onChange={(e) => handleInputChange('goldenPalmCount', e.target.value)}
                className={errors.goldenPalmCount ? 'error' : ''}
                placeholder="Введите количество"
              />
              {errors.goldenPalmCount && <span className="error-message">{errors.goldenPalmCount}</span>}
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

export default MovieModal;