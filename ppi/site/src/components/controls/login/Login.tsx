import './Login.scss';
import Input from "../input/Input.tsx";
import PrimaryButton from "../primary-button/PrimaryButton.tsx";
import {useState} from "react";
import { useDispatch } from 'react-redux';
import {login, signUp} from "../../../redux/authSlice.ts";
import {setDailyGoal, setYearlyGoal, setDailyRead, setYearlyRead} from "../../../redux/goalsSlice.ts";

interface LoginProps {
  type: 'login' | 'signup';
  onSubmit?: () => void;
  titleId?: string;
}

function Login({ type: initialType, onSubmit, titleId }: LoginProps) {
  const [type, setType] = useState<'login' | 'signup'>(initialType);
  const [error, setError] = useState<string>('');
  const dispatch = useDispatch();

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');

    const formData = new FormData(e.currentTarget);

    if (type === 'login') {
      const identifier = formData.get('identifier') as string;
      const password = formData.get('password') as string;

      if (identifier === 'user' && password === 'user') {
        dispatch(login({ username: 'user', email: 'user@example.com' }));
        dispatch(setDailyGoal(100));
        dispatch(setDailyRead(56));
        dispatch(setYearlyGoal(35));
        dispatch(setYearlyRead(12));
        onSubmit?.();
      } else if (identifier === 'admin' && password === 'admin') {
        dispatch(login({ username: 'admin', email: 'admin@example.com' }));
        dispatch(setDailyGoal(100));
        dispatch(setDailyRead(56));
        dispatch(setYearlyGoal(35));
        dispatch(setYearlyRead(12));
        onSubmit?.();
      } else if (identifier === 'newuser' && password === 'newuser') {
        dispatch(login({ username: 'newuser', email: 'newuser@example.com' }));
        dispatch(setDailyGoal(0));
        dispatch(setDailyRead(0));
        dispatch(setYearlyGoal(0));
        dispatch(setYearlyRead(0));
        onSubmit?.();
      } else {
        setError('Неверное имя пользователя или пароль');
      }
    } else {
      const email = formData.get('email') as string;
      const username = formData.get('username') as string;
      const password = formData.get('password') as string;
      const confirmPassword = formData.get('confirm-password') as string;

      if (password !== confirmPassword) {
        setError('Пароли не совпадают');
        return;
      }

      dispatch(signUp({ username, email }));
      onSubmit?.();
    }
  };


  return (
    <div className="login-container">
      <h2 className="login-title" id={titleId}>{type === 'login' ? 'Войти' : 'Регистрация'}</h2>
      <form
        className="login-form"
        onSubmit={handleSubmit}
      >
        {type === 'signup' && (
          <>
            <label htmlFor="email">Электронная почта</label>
            <Input type="email" id="email" name="email" required placeholder="example@mail.com" />

            <label htmlFor="username">Имя пользователя</label>
            <Input type="text" id="username" name="username" required placeholder="username" />
          </>
        )}

        {type === 'login' && (
          <>
            <label htmlFor="identifier">Имя пользователя или электронная почта</label>
            <Input type="text" id="identifier" name="identifier" required placeholder="example@mail.com" />
          </>
        )}

        <label htmlFor="password">Пароль</label>
        <Input type="password" id="password" name="password" required placeholder="********" />

        {type === 'signup' && (
          <>
            <label htmlFor="confirm-password">Подтвердите пароль</label>
            <Input type="password" id="confirm-password" name="confirm-password" required placeholder="********" />
          </>
        )}

        {error && <div className="login-error">{error}</div>}

        <PrimaryButton
          label={type === 'login' ? 'Войти' : 'Зарегистрироваться'}
          type="submit"
          onClick={() => {}}
        />

      </form>

      <div className="login-footer">
        {type === 'login' ? (
          <>
            <span>Еще нет аккаунта?</span>
            <button onClick={() => setType('signup')} className="login-switch-button">Зарегистрироваться</button>
          </>
        ) : (
          <>
            <span>Уже есть аккаунт?</span>
            <button onClick={() => setType('login')} className="login-switch-button">Войти</button>
          </>
        )}
      </div>
    </div>
  );
}

export default Login;
