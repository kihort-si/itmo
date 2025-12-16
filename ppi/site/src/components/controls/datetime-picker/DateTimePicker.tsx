import './DateTimePicker.scss';
import { useState, useRef, useEffect } from 'react';
import CalendarIcon from '../../../assets/elements/calendar.svg';

interface DateTimePickerProps {
  id?: string;
  name?: string;
  value: string;
  onChange: (value: string) => void;
  required?: boolean;
  maxDateTime?: string;
  label?: string;
  error?: string;
}

function DateTimePicker({ 
  id, 
  name, 
  value, 
  onChange, 
  required = false,
  maxDateTime,
  label,
  error
}: DateTimePickerProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [selectedDate, setSelectedDate] = useState<Date | null>(value ? new Date(value) : null);
  const [selectedTime, setSelectedTime] = useState({ hours: '00', minutes: '00' });
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const containerRef = useRef<HTMLDivElement>(null);

  const now = new Date();
  const maxDate = maxDateTime ? new Date(maxDateTime) : now;

  useEffect(() => {
    if (value) {
      const date = new Date(value);
      setSelectedDate(date);
      setSelectedTime({
        hours: String(date.getHours()).padStart(2, '0'),
        minutes: String(date.getMinutes()).padStart(2, '0')
      });
    }
  }, [value]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
      return () => document.removeEventListener('mousedown', handleClickOutside);
    }
  }, [isOpen]);

  const formatDisplayValue = (date: Date | null): string => {
    if (!date) return '';
    
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    
    return `${day}.${month}.${year} ${hours}:${minutes}`;
  };

  const formatISOString = (date: Date): string => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  };

  const handleDateSelect = (date: Date) => {
    const newDate = new Date(date);
    newDate.setHours(parseInt(selectedTime.hours));
    newDate.setMinutes(parseInt(selectedTime.minutes));
    newDate.setSeconds(0);
    newDate.setMilliseconds(0);

    const adjustedMaxDate = new Date(maxDate);
    adjustedMaxDate.setSeconds(0);
    adjustedMaxDate.setMilliseconds(0);
    
    if (newDate > adjustedMaxDate) {
      const adjustedDate = new Date(adjustedMaxDate);
      setSelectedDate(adjustedDate);
      setSelectedTime({
        hours: String(adjustedDate.getHours()).padStart(2, '0'),
        minutes: String(adjustedDate.getMinutes()).padStart(2, '0')
      });
      onChange(formatISOString(adjustedDate));
      return;
    }

    setSelectedDate(newDate);
    onChange(formatISOString(newDate));
  };

  const handleTimeChange = (type: 'hours' | 'minutes', newValue: string) => {
    if (newValue === '') {
      return;
    }

    const numValue = parseInt(newValue);
    if (isNaN(numValue)) return;

    if (type === 'hours' && (numValue < 0 || numValue > 23)) return;
    if (type === 'minutes' && (numValue < 0 || numValue > 59)) return;

    const newTime = {
      ...selectedTime,
      [type]: String(numValue).padStart(2, '0')
    };
    setSelectedTime(newTime);

    if (selectedDate) {
      const newDate = new Date(selectedDate);
      newDate.setHours(parseInt(newTime.hours));
      newDate.setMinutes(parseInt(newTime.minutes));
      newDate.setSeconds(0);
      newDate.setMilliseconds(0);

      const adjustedMaxDate = new Date(maxDate);
      adjustedMaxDate.setSeconds(0);
      adjustedMaxDate.setMilliseconds(0);
      
      if (newDate > adjustedMaxDate) {
        const adjustedDate = new Date(adjustedMaxDate);
        setSelectedTime({
          hours: String(adjustedDate.getHours()).padStart(2, '0'),
          minutes: String(adjustedDate.getMinutes()).padStart(2, '0')
        });
        onChange(formatISOString(adjustedDate));
      } else {
        onChange(formatISOString(newDate));
      }
    }
  };

  const getDaysInMonth = (date: Date) => {
    return new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate();
  };

  const getFirstDayOfMonth = (date: Date) => {
    const day = new Date(date.getFullYear(), date.getMonth(), 1).getDay();
    return day === 0 ? 6 : day - 1;
  };

  const isDateDisabled = (date: Date): boolean => {
    const dateStartOfDay = new Date(date);
    dateStartOfDay.setHours(0, 0, 0, 0);
    const maxDateStartOfDay = new Date(maxDate);
    maxDateStartOfDay.setHours(0, 0, 0, 0);
    return dateStartOfDay > maxDateStartOfDay;
  };

  const renderCalendar = () => {
    const daysInMonth = getDaysInMonth(currentMonth);
    const firstDay = getFirstDayOfMonth(currentMonth);
    const days = [];

    for (let i = 0; i < firstDay; i++) {
      days.push(<div key={`empty-${i}`} className="calendar-day empty"></div>);
    }

    for (let day = 1; day <= daysInMonth; day++) {
      const date = new Date(currentMonth.getFullYear(), currentMonth.getMonth(), day);
      const isDisabled = isDateDisabled(date);
      const isSelected = selectedDate && 
        date.getDate() === selectedDate.getDate() &&
        date.getMonth() === selectedDate.getMonth() &&
        date.getFullYear() === selectedDate.getFullYear();
      const isToday = date.toDateString() === now.toDateString();

      days.push(
        <div
          key={day}
          className={`calendar-day ${isDisabled ? 'disabled' : ''} ${isSelected ? 'selected' : ''} ${isToday ? 'today' : ''}`}
          onClick={() => !isDisabled && handleDateSelect(date)}
        >
          {day}
        </div>
      );
    }

    return days;
  };

  const navigateMonth = (direction: 'prev' | 'next') => {
    setCurrentMonth(prev => {
      const newDate = new Date(prev);
      if (direction === 'prev') {
        newDate.setMonth(prev.getMonth() - 1);
      } else {
        newDate.setMonth(prev.getMonth() + 1);
      }
      return newDate;
    });
  };

  const monthNames = [
    'Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь',
    'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь'
  ];

  const weekDays = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];

  return (
    <div className="datetime-picker" ref={containerRef}>
      {label && <label className="datetime-picker-label">{label}</label>}
      <div 
        className={`datetime-picker-input ${error ? 'error' : ''}`}
        onClick={() => setIsOpen(!isOpen)}
      >
        <img src={CalendarIcon} alt="Календарь" className="calendar-icon" />
        <input
          type="text"
          readOnly
          value={formatDisplayValue(selectedDate)}
          placeholder="Выберите дату и время"
          required={required}
          id={id}
          name={name}
        />
      </div>
      {error && <div className="datetime-picker-error">{error}</div>}

      {isOpen && (
        <div className="datetime-picker-dropdown">
          <div className="datetime-picker-calendar">
            <div className="calendar-header">
              <button 
                type="button"
                className="calendar-nav-button"
                onClick={(e) => {
                  e.stopPropagation();
                  navigateMonth('prev');
                }}
              >
                ‹
              </button>
              <span className="calendar-month-year">
                {monthNames[currentMonth.getMonth()]} {currentMonth.getFullYear()}
              </span>
              <button 
                type="button"
                className="calendar-nav-button"
                onClick={(e) => {
                  e.stopPropagation();
                  navigateMonth('next');
                }}
              >
                ›
              </button>
            </div>
            <div className="calendar-weekdays">
              {weekDays.map(day => (
                <div key={day} className="calendar-weekday">{day}</div>
              ))}
            </div>
            <div className="calendar-days">
              {renderCalendar()}
            </div>
          </div>
          <div className="datetime-picker-time">
            <label>Время:</label>
            <div className="time-inputs">
              <input
                type="number"
                min="0"
                max="23"
                value={selectedTime.hours}
                onChange={(e) => handleTimeChange('hours', e.target.value)}
                onClick={(e) => e.stopPropagation()}
                placeholder="00"
              />
              <span>:</span>
              <input
                type="number"
                min="0"
                max="59"
                value={selectedTime.minutes}
                onChange={(e) => handleTimeChange('minutes', e.target.value)}
                onClick={(e) => e.stopPropagation()}
                placeholder="00"
              />
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default DateTimePicker;

