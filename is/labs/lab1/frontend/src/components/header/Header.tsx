import {Link, useLocation} from "react-router-dom";
import "./Header.scss";

function Header() {
  const location = useLocation();
  const currentPathname = location.pathname;
  return <header>
    <div className="page-buttons">
      {currentPathname === '/' ? (
        <>
          <button><Link to="/people">Перейти к людям</Link></button>
          <button><Link to="/special-commands">Специальные команды</Link></button>
        </>
      ) : currentPathname === '/people' ? (
        <>
          <button><Link to="/">Перейти к фильмам</Link></button>
          <button><Link to="/special-commands">Специальные команды</Link></button>
        </>
      ) : (
        <>
          <button><Link to="/">Перейти к фильмам</Link></button>
          <button><Link to="/people">Перейти к людям</Link></button>
        </>
      )}
    </div>
  </header>;
}

export default Header;