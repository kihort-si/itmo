import { useState } from 'react';
import './SpecialCommands.scss';
import MovieService, {type MovieResponseDto} from "../../services/MovieService.ts";
import type {PersonResponseDto} from "../../services/PersonService.ts";

const GENRES = ['COMEDY', 'ADVENTURE', 'TRAGEDY', 'HORROR'];

function SpecialCommands() {
  const [deleteGenre, setDeleteGenre] = useState('');
  const [deleteGenreResult, setDeleteGenreResult] = useState<string | null>(null);
  const [goldenPalmCount, setGoldenPalmCount] = useState('');
  const [fromGenre, setFromGenre] = useState('');
  const [toGenre, setToGenre] = useState('');
  const [totalGoldenPalms, setTotalGoldenPalms] = useState<number | null>(null);
  const [moviesWithLessGoldenPalms, setMoviesWithLessGoldenPalms] = useState<MovieResponseDto[] | null>(null);
  const [redistributeResult, setRedistributeResult] = useState<string | null>(null);
  const [lastGoldenPalmCount, setLastGoldenPalmCount] = useState<number | null>(null);
  const [screenwritersWithoutOscars, setScreenwritersWithoutOscars] = useState<PersonResponseDto[] | null>(null);

  const handleDeleteByGenre = async () => {
    try {
      const result = await MovieService.deleteOneByGenre(deleteGenre);
      setDeleteGenreResult(result.message);
      setDeleteGenre('');
    } catch (error) {
      console.error('Ошибка при удалении фильма:', error);
      setDeleteGenreResult(error instanceof Error ? error.message : 'Не удалось удалить фильм');
    }
  };

  const handleGetGoldenPalmsCount = async () => {
    try {
      const result = await MovieService.getGoldenPalmWinnersCount();
      setTotalGoldenPalms(result.totalGoldenPalms);
    } catch (error) {
      console.error('Ошибка при получении количества Золотых Пальмовых ветвей:', error);
      alert('Не удалось получить данные');
    }
  };

  const handleGetMoviesWithLessGoldenPalms = async () => {
    try {
      const result = await MovieService.getMoviesWithGoldenPalmCountLessThan(Number(goldenPalmCount));
      setMoviesWithLessGoldenPalms(result);
      setLastGoldenPalmCount(Number(goldenPalmCount));
      setGoldenPalmCount('');
    } catch (error) {
      console.error('Ошибка при получении фильмов:', error);
      alert('Не удалось получить данные');
    }
  }

  const handleGetScreenwritersWithoutOscars = async () => {
    try {
      const result = await MovieService.getScreenwritersWithoutOscars();
      setScreenwritersWithoutOscars(result);
    } catch (error) {
      console.error('Ошибка при получении сценаристов:', error);
      alert('Не удалось получить данные');
    }
  };

  const handleRedistributeOscars = async () => {
    try {
      const result = await MovieService.redistributeOscars(fromGenre, toGenre);
      setRedistributeResult(result.message);
      setFromGenre('');
      setToGenre('');
    } catch (error) {
      console.error('Ошибка при перераспределении Оскаров:', error);
      setRedistributeResult(error instanceof Error ? error.message : 'Не удалось перераспределить');
    }
  };

  return (
    <main className="special-commands">
      <h1>Специальные команды</h1>
      <div className="commands-grid">
        <div className="command-card">
          <span className="command-title">Удалить один объект жанра</span>
          <div className="command-content">
            <select
              id="delete-genre"
              value={deleteGenre}
              onChange={(e) => setDeleteGenre(e.target.value)}
            >
              <option value="">Выберите жанр</option>
              {GENRES.map(genre => (
                <option key={genre} value={genre}>{genre}</option>
              ))}
            </select>
            <button disabled={!deleteGenre} onClick={handleDeleteByGenre}>Удалить</button>
            {deleteGenreResult !== null && (
              <div className="result">
                <span>{deleteGenreResult}</span>
              </div>
            )}
          </div>
        </div>

        <div className="command-card">
          <span className="command-title">Сколько Золотых Пальмовых ветвей получили все фильмы?</span>
          <div className="command-content">
            <button onClick={handleGetGoldenPalmsCount}>Узнать</button>
            {totalGoldenPalms !== null && (
              <div className="result">
                Всего Золотых Пальмовых ветвей: <strong>{totalGoldenPalms}</strong>
              </div>
            )}
          </div>
        </div>

        <div className="command-card">
          <span className="command-title">Фильмы, у которых Золотых Пальмовых ветвей меньше, чем</span>
          <div className="command-content">
            <input
              type="number"
              min="0"
              value={goldenPalmCount}
              onChange={(e) => setGoldenPalmCount(e.target.value)}
              placeholder="Введите количество"
            />
            <button disabled={!goldenPalmCount} onClick={handleGetMoviesWithLessGoldenPalms}>Найти</button>
            {moviesWithLessGoldenPalms && moviesWithLessGoldenPalms.length === 0 && (
              <div className="result">
                <span>Нет фильмов с меньшим количеством Золотых Пальмовых ветвей</span>
              </div>
            )}
          </div>
        </div>

        <div className="command-card">
          <span className="command-title">Сценаристы, фильмы которых не получили ни одного Оскара</span>
          <div className="command-content">
            <button onClick={handleGetScreenwritersWithoutOscars}>Узнать</button>
            {screenwritersWithoutOscars && screenwritersWithoutOscars.length === 0 && (
              <div className="result">
                <span>Все сценаристы имеют фильмы с Оскарами или ни на один фильм не назначен сценарист</span>
              </div>
            )}
          </div>
        </div>

        <div className="command-card">
          <span className="command-title">Равномерно перераспределить все "Оскары", полученные фильмами одного жанра, между фильмами другого жанра</span>
          <div className="command-content">
            <div className="select-group">
              <select
                id="from-genre"
                value={fromGenre}
                onChange={(e) => setFromGenre(e.target.value)}
              >
                <option value="">Откуда</option>
                {GENRES.map(genre => (
                  <option key={genre} value={genre}>{genre}</option>
                ))}
              </select>
              <select
                id="to-genre"
                value={toGenre}
                onChange={(e) => setToGenre(e.target.value)}
              >
                <option value="">Куда</option>
                {GENRES.map(genre => (
                  <option key={genre} value={genre} disabled={genre === fromGenre}>{genre}</option>
                ))}
              </select>
            </div>
            <button disabled={!fromGenre || !toGenre} onClick={handleRedistributeOscars}>
              Перераспределить
            </button>
            {redistributeResult !== null && (
              <div className="result">
                <span>{redistributeResult}</span>
              </div>
            )}
          </div>
        </div>
      </div>
      {moviesWithLessGoldenPalms && moviesWithLessGoldenPalms.length > 0 && (
        <div className="result-section">
          <h2>Фильмы с количеством Золотых Пальмовых ветвей меньше {lastGoldenPalmCount}:</h2>
          <div className="result-table">
            <table>
              <thead>
              <tr>
                <th>Название</th>
                <th>Пальмовых ветвей</th>
              </tr>
              </thead>
              <tbody>
              {moviesWithLessGoldenPalms.map(movie => (
                <tr key={movie.id}>
                  <td>{movie.name}</td>
                  <td>{movie.goldenPalmCount ?? 0}</td>
                </tr>
              ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
      {screenwritersWithoutOscars && screenwritersWithoutOscars.length > 0 && (
        <div className="result-section">
          <h2>Сценаристы, фильмы которых не получили ни одного Оскара:</h2>
          <div className="result-table">
            <table>
              <thead>
                <tr>
                  <th>Имя сценариста</th>
                </tr>
              </thead>
              <tbody>
                {screenwritersWithoutOscars.map(screenwriter => (
                  <tr key={screenwriter.id}>
                    <td>{screenwriter.name}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </main>
  );
}

export default SpecialCommands;