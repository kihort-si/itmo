import {BrowserRouter as Router, Routes, Route, Navigate} from 'react-router-dom';
import './App.scss';
import Header from "./components/layout/header/Header.tsx";
import Home from "./components/pages/home/Home.tsx";
import Statistic from "./components/pages/statistic/Statistic.tsx";
import {useSelector} from "react-redux";
import type {RootState} from "./redux/store.ts";
import AdminPage from './components/pages/admin-page/AdminPage.tsx';
import Library from "./components/pages/library/Library.tsx";
import Profile from "./components/pages/profile/Profile.tsx";
import Book from "./components/pages/book/Book.tsx";
import Collection from "./components/pages/collection/Collection.tsx";
import Account from "./components/pages/account/Account.tsx";
import Followers from "./components/pages/followers/Followers.tsx";
import Subscriptions from "./components/pages/subscriptions/Subscriptions.tsx";

function App() {
  const isAuthenticated = useSelector((state: RootState) => state.auth.isAuthenticated);
  const user = useSelector((state: RootState) => state.auth.user);

  return (
    <>
      <Router>
        <Header />
        <Routes>
          <Route path="/" Component={Home}/>
          <Route path="/statistic" element={isAuthenticated ? <Statistic /> : <Navigate to="/" replace />}/>
          <Route path="/library" element={isAuthenticated ? <Library /> : <Navigate to="/" replace /> }/>
          <Route path="/admin" element={user?.username === 'admin' ? <AdminPage /> : <Navigate to="/" replace /> }/>
          <Route path="/profile" Component={Profile} />
          <Route path="/book" Component={Book} />
          <Route path="/collection" Component={Collection} />
          <Route path="/account" element={isAuthenticated ? <Account /> : <Navigate to="/" replace /> } />
          <Route path="/followers" Component={Followers} />
          <Route path="/subscriptions" Component={Subscriptions} />
        </Routes>
      </Router>
    </>
  )
}

export default App
