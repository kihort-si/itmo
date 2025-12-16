import './Header.scss';
import MainLogo from '../../../assets/elements/logo.svg'
import {useDispatch, useSelector} from "react-redux";
import {logout} from "../../../redux/authSlice.ts";
import PrimaryButton from "../../controls/primary-button/PrimaryButton.tsx";
import SecondaryButton from "../../controls/secondary-button/SecondaryButton.tsx";
import {Link, useNavigate} from "react-router-dom";
import homeButton from "../../../assets/elements/homeButton.svg";
import libraryButton from "../../../assets/elements/libraryButton.svg";
import statisticsButton from "../../../assets/elements/statisticsButton.svg";
import settingsButton from "../../../assets/elements/settingsButton.svg";
import type {RootState} from '../../../redux/store';
import Modal from "../../controls/modal/Modal.tsx";
import Login from "../../controls/login/Login.tsx";
import {useEffect, useState} from "react";
import profileButton from "../../../assets/elements/profileButton.svg";

function Header() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const isAuthenticated = useSelector((state: RootState) => state.auth.isAuthenticated);

  const [modalOpen, setModalOpen] = useState(false);
  const [authType, setAuthType] = useState<'login'|'signup'>('login');
  const [isAdmin, setIsAdmin] = useState(false);
  const user = useSelector((state: RootState) => state.auth.user);

  useEffect(() => {
    if (user?.username === 'admin') {
      setIsAdmin(true);
    } else {
      setIsAdmin(false);
    }
  }, [user]);

  const handleSignIn = () => {
    setAuthType('signup');
    setModalOpen(true);
  }

  const handleLogin = () => {
    setAuthType('login');
    setModalOpen(true);
  }

  const handleLogout = () => {
    dispatch(logout());
    navigate('/');
  };

  return (
    <>
      <header>
        <div className="header-container">
          <Link to="/">
            <img className="main-logo" src={MainLogo} alt="logo" />
          </Link>
          {isAuthenticated ? (
            <div className="header-buttons">
              <Link to="/" className="header-button">
                <img src={homeButton} alt={"Домой"}/>
                <span className="header-button-text">Домой</span>
              </Link>
              <Link to="/library" className="header-button">
                <img src={libraryButton} alt={"Библиотека"}/>
                <span className="header-button-text">Библиотека</span>
              </Link>
              <Link to="/statistic" className="header-button">
                <img src={statisticsButton} alt={"Статистика"}/>
                <span className="header-button-text">Статистика</span>
              </Link>
              <Link to="/account" className="header-button">
                <img src={profileButton} alt={"Профиль"} className="profile-avatar-image"/>
                <span className="header-button-text">Профиль</span>
              </Link>
              {isAdmin && (
                <Link to="/admin" className="header-button">
                  <img src={settingsButton} alt={"Админ"}/>
                  <span className="header-button-text">Панель админа</span>
                </Link>
              )}
              <PrimaryButton label={"Выйти"} onClick={handleLogout} />
            </div>
          ) : (
            <div className="login-signup-buttons">
              <SecondaryButton label={"Зарегистрироваться"} onClick={handleSignIn} />
              <PrimaryButton label={"Войти"} onClick={handleLogin} />
            </div>
          )}
        </div>
      </header>

      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        titleId="auth-dialog-title"
        closeOnBackdropClick={true}
      >
        <Login
          type={authType}
          titleId="auth-dialog-title"
          onSubmit={() => {
            setModalOpen(false);
          }}
        />
      </Modal>
    </>
  )
}

export default Header;