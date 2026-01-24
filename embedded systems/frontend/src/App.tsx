import { BrowserRouter as Router, Routes, Route, Link, useLocation } from 'react-router-dom';
import ModulesPage from './pages/ModulesPage';
import ControlPage from './pages/ControlPage';
import './App.css';

function Navigation() {
  const location = useLocation();
  
  return (
    <nav className="nav">
      <div className="nav-container">
        <h1 className="nav-title">Smart Greenhouse</h1>
        <div className="nav-links">
          <Link 
            to="/modules" 
            className={location.pathname === '/modules' ? 'nav-link active' : 'nav-link'}
          >
            Информация об устройствах
          </Link>
          <Link 
            to="/control" 
            className={location.pathname === '/control' ? 'nav-link active' : 'nav-link'}
          >
            Управление
          </Link>
        </div>
      </div>
    </nav>
  );
}

function App() {
  return (
    <Router>
      <div className="app">
        <Navigation />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<ModulesPage />} />
            <Route path="/modules" element={<ModulesPage />} />
            <Route path="/control" element={<ControlPage />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;

