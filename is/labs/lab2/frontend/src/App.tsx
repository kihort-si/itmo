import {BrowserRouter as Router, Routes, Route} from "react-router-dom";
import './App.css';
import Movies from "./pages/movie/Movies.tsx";
import Person from "./pages/person/Person.tsx";
import Header from "./components/header/Header.tsx";
import SpecialCommands from "./pages/special-commands/SpecialCommands.tsx";

function App() {
  return (
    <Router>
      <Header/>
      <Routes>
        <Route path="/" Component={Movies} />
        <Route path="/people" Component={Person} />
        <Route path="/special-commands" Component={SpecialCommands} />
      </Routes>
    </Router>
  )
}

export default App
