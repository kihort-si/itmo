import './ProfileForm.scss';
import Input from "../input/Input.tsx";
import PrimaryButton from "../primary-button/PrimaryButton.tsx";
import {useRef, useState} from "react";
import {useDispatch, useSelector} from "react-redux";
import {setUsername} from "../../../redux/authSlice.ts";
import type {RootState} from "../../../redux/store.ts";
import SecondaryButton from "../secondary-button/SecondaryButton.tsx";
import UserAvatar from '../../../assets/images/users/user.png';

interface ProfileFormProps {
  onSubmit?: () => void;
  titleId?: string;
}

function ProfileForm({ onSubmit, titleId }: ProfileFormProps) {
  const {user} = useSelector((state: RootState) => state.auth);
  const [username, setNewUsername] = useState(user?.username || 'user');
  const [password, setPassword] = useState('user@example.com');
  const [error, setError] = useState<string>('');
  const [newPassword, setNewPassword] = useState('');
  const dispatch = useDispatch();

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (password !== 'user') {
      setError('Неверный текущий пароль');
      return;
    }

    if (username === 'admin') {
      setError('Имя пользователя уже занято');
      return;
    }

    if (newPassword && newPassword.length < 6) {
      setError('Новый пароль должен быть не менее 6 символов');
      return;
    }

    if (newPassword && !/[A-Z]/.test(newPassword)) {
      setError('Новый пароль должен содержать хотя бы одну заглавную букву');
      return;
    }

    if (newPassword && !/[0-9]/.test(newPassword)) {
      setError('Новый пароль должен содержать хотя бы одну цифру');
      return;
    }

    dispatch(setUsername(username));

    setError('');
    onSubmit?.();
  };

  const inputRef = useRef<HTMLInputElement | null>(null);

  const handleClick = () => {
    inputRef.current?.click();
  };

  return (
    <div className="profile-form-container">
      <div className="profile-form-container-inputs">
        <h2 className="profile-form-title" id={titleId}>
        Редактировать профиль
      </h2>
        <form className="profile-form" onSubmit={handleSubmit}>
          <label htmlFor="username">Имя пользователя</label>
          <Input
            type="text"
            id="username"
            name="username"
            required
            value={username}
            onChange={(value: string) => setNewUsername(value)}
            placeholder={"username"}
          />

          <label htmlFor="password">Текущий пароль</label>
          <Input
            type="password"
            id="password"
            name="password"
            required
            onChange={(value: string) => setPassword(value)}
            placeholder={"*******"}
          />

          <label htmlFor="confirm-password">Новый пароль</label>
          <Input
            type="password"
            id="confirm-password"
            name="confirm-password"
            onChange={(value: string) => setNewPassword(value)}
            placeholder={"*******"}
          />

          {error && <div className="profile-error">{error}</div>}

          <PrimaryButton label="Сохранить" type="submit" onClick={() => {}} />
        </form>
      </div>
      <div className="profile-form-avatar-input">
        <img src={UserAvatar} alt={user?.username} />
        <SecondaryButton label={"Изменить фото"} onClick={handleClick}/>
        <input
          ref={inputRef}
          type="file"
          accept=".png .jpg .jpeg .webp"
          style={{ display: 'none' }}
        />
      </div>
    </div>
  );
}

export default ProfileForm;