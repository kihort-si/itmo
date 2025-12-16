import './ReadingForm.scss';
import Input from "../input/Input.tsx";
import PrimaryButton from "../primary-button/PrimaryButton.tsx";
import DateTimePicker from "../datetime-picker/DateTimePicker.tsx";
import { useState } from "react";
import {useDispatch, useSelector} from 'react-redux';
import { setDailyRead, setYearlyRead } from "../../../redux/goalsSlice.ts";
import type {RootState} from "../../../redux/store.ts";

interface ReadingFormProps {
  bookTitle: string;
  bookId?: string;
  bookAuthor?: string;
  bookRating?: number;
  bookImageUrl?: string;
  onSubmit?: (readingData: {
    bookId: string;
    title: string;
    author: string;
    rating: number;
    imageUrl: string;
    startDate: string;
    endDate: string;
    pages: number;
    isFinished: boolean;
  }) => void;
  titleId?: string;
}

function ReadingForm({ 
  bookTitle, 
  bookId = '',
  bookAuthor = '',
  bookRating = 5,
  bookImageUrl = '',
  onSubmit, 
  titleId 
}: ReadingFormProps) {
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [pages, setPages] = useState(0);
  const [isFinished, setIsFinished] = useState(false);
  const [startDateError, setStartDateError] = useState('');
  const [endDateError, setEndDateError] = useState('');
  const dispatch = useDispatch();
  const { dailyRead, yearlyRead } = useSelector(
    (state: RootState) => state.goals
  );

  const now = new Date();
  const maxDateTime = now.toISOString().slice(0, 16);

  const validateDates = (start: string, end: string) => {
    let startHasError = false;
    let endHasError = false;

    if (start) {
      const startDateObj = new Date(start);
      if (startDateObj > now) {
        setStartDateError('Дата начала не может быть в будущем');
        startHasError = true;
      } else {
        setStartDateError('');
      }
    } else {
      setStartDateError('');
    }

    if (end) {
      const endDateObj = new Date(end);
      if (endDateObj > now) {
        setEndDateError('Дата конца не может быть в будущем');
        endHasError = true;
      } else {
        if (start && !startHasError) {
          const startDateObj = new Date(start);
          if (startDateObj >= endDateObj) {
            setEndDateError('Дата конца должна быть позже даты начала');
            endHasError = true;
          } else if (!endHasError) {
            setEndDateError('');
          }
        } else if (!endHasError) {
          setEndDateError('');
        }
      }
    } else {
      setEndDateError('');
    }

    return !startHasError && !endHasError;
  };

  const handleStartDateChange = (value: string) => {
    setStartDate(value);
    validateDates(value, endDate);
  };

  const handleEndDateChange = (value: string) => {
    setEndDate(value);
    validateDates(startDate, value);
  };

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (!validateDates(startDate, endDate)) {
      return;
    }

    if (!startDate || !endDate) {
      if (!startDate) setStartDateError('Выберите дату начала чтения');
      if (!endDate) setEndDateError('Выберите дату конца чтения');
      return;
    }

    dispatch(setDailyRead(dailyRead + pages));

    if (isFinished) {
      dispatch(setYearlyRead(yearlyRead + 1));
    }

    if (onSubmit) {
      onSubmit({
        bookId,
        title: bookTitle,
        author: bookAuthor,
        rating: bookRating,
        imageUrl: bookImageUrl,
        startDate,
        endDate,
        pages,
        isFinished
      });
    }
  };

  return (
    <div className="reading-form-container">
      <h2 className="reading-form-title" id={titleId}>
        {bookTitle}
      </h2>
      <form className="reading-form" onSubmit={handleSubmit}>
        <DateTimePicker
          id="startDate"
          name="startDate"
          value={startDate}
          onChange={handleStartDateChange}
          required
          maxDateTime={maxDateTime}
          label="Начало чтения"
          error={startDateError}
        />

        <DateTimePicker
          id="endDate"
          name="endDate"
          value={endDate}
          onChange={handleEndDateChange}
          required
          maxDateTime={maxDateTime}
          label="Конец чтения"
          error={endDateError}
        />

        <label htmlFor="pages">Количество страниц</label>
        <Input
          type="number"
          id="pages"
          name="pages"
          required
          value={pages.toString()}
          onChange={(value) => setPages(Number(value))}
          min="1"
        />

        <div className="checkbox-container">
          <input
            type="checkbox"
            id="isFinished"
            checked={isFinished}
            onChange={(e) => setIsFinished(e.target.checked)}
          />
          <label htmlFor="isFinished">Отметить как законченную</label>
        </div>

        <PrimaryButton label="Сохранить" type="submit" onClick={() => {}} />
      </form>
    </div>
  );
}

export default ReadingForm;