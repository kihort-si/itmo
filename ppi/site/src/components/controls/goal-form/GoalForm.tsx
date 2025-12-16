import './GoalForm.scss';
import Input from "../input/Input.tsx";
import PrimaryButton from "../primary-button/PrimaryButton.tsx";
import { useState } from "react";
import { useDispatch, useSelector } from 'react-redux';
import { setDailyGoal, setYearlyGoal } from "../../../redux/goalsSlice.ts";
import type { RootState } from '../../../redux/store.ts';

interface GoalFormProps {
  type: 'daily' | 'yearly';
  onSubmit?: () => void;
  titleId?: string;
}

function GoalForm({ type, onSubmit, titleId }: GoalFormProps) {
  const goals = useSelector((state: RootState) => state.goals);
  const currentGoal = type === 'daily' ? goals.dailyGoal : goals.yearlyGoal;
  const hasGoal = currentGoal > 0;
  
  const [goal, setGoal] = useState(hasGoal ? currentGoal : '');
  const dispatch = useDispatch();

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (type === 'daily') {
      dispatch(setDailyGoal(Number(goal)));
    } else {
      dispatch(setYearlyGoal(Number(goal)));
    }

    onSubmit?.();
  };

  return (
    <div className="goal-form-container">
      <h2 className="goal-form-title" id={titleId}>
        {hasGoal 
          ? (type === 'daily' ? 'Изменить дневную цель' : 'Изменить годовую цель')
          : (type === 'daily' ? 'Добавить дневную цель' : 'Добавить годовую цель')
        }
      </h2>
      <form className="goal-form" onSubmit={handleSubmit}>
        <label htmlFor="goal">
          {type === 'daily' ? 'Страниц в день' : 'Книг в год'}
        </label>
        <Input
          type="number"
          id="goal"
          name="goal"
          required
          value={goal.toString()}
          onChange={(value: string) => setGoal(value ? Number(value) : '')}
          min="1"
          placeholder={type === 'daily' ? 'Введите количество страниц' : 'Введите количество книг'}
        />
        <PrimaryButton label="Сохранить" type="submit" onClick={() => {}} />
      </form>
    </div>
  );
}

export default GoalForm;