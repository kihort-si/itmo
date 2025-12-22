import './Movies.scss'
import PaginatedTable from "../../components/paginatedTable/PaginatedTable.tsx";
import MovieService, {type MovieRequestDto, type MovieResponseDto} from "../../services/MovieService.ts";
import {useCallback, useEffect, useRef, useState} from "react";
import MovieModal from "../../components/movieModal/MovieModal.tsx";
import WebSocketService from "../../services/WebSocketService.ts";
import ImportInput from "../../components/importInput/ImportInput.tsx";
import UploadFileService from "../../services/UploadFileService.ts";
import ErrorModal from "../../components/errorModal/ErrorModal.tsx";
import {formatDateTime} from "../../utils/dateUtils.ts";

function Movies() {
  const columns: { [key: string]: string }[] = [
    {id: 'ID'},
    {name: 'Название'},
    {creationDate: 'Дата создания'},
    {'coordinates.x': 'Координата X'},
    {'coordinates.y': 'Координата Y'},
    {oscarsCount: 'Количество оскаров'},
    {budget: 'Бюджет'},
    {totalBoxOffice: 'Сборы в мире'},
    {mpaaRating: 'Возрастной рейтинг'},
    {'director.name': 'Режиссер'},
    {'screenwriter.name': 'Сценарист'},
    {'operator.name': 'Оператор'},
    {length: 'Длительность'},
    {goldenPalmCount: 'Пальмовых ветвей'},
    {genre: 'Жанр'},
  ];

  const [data, setData] = useState<MovieResponseDto[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [_sortLoading, setSortLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState<'create' | 'edit'>('create');
  const [selectedMovie, setSelectedMovie] = useState<MovieResponseDto | null>(null);
  const [selectedMovieForEdit, setSelectedMovieForEdit] = useState<MovieResponseDto | null>(null);
  const tableRef = useRef<HTMLDivElement>(null);
  const buttonsRef = useRef<HTMLDivElement>(null);
  const foundedMovieRef = useRef<HTMLDivElement>(null);
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [_filters, setFilters] = useState<{[key: string]: string}>({});
  const [movieIdToSearch, setMovieIdToSearch] = useState<number | undefined>();
  const [foundedMovie, setFoundedMovie] = useState<MovieResponseDto | null>(null);
  const [errorModalOpen, setErrorModalOpen] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const handleWebSocketUpdate = useCallback(() => {
    console.log('Received movie update via WebSocket');
    fetchMovies();
  }, []);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as Node;

      if (buttonsRef.current?.contains(target)) {
        return;
      }

      if (tableRef.current && !tableRef.current.contains(target)) {
        setSelectedMovieForEdit(null);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  useEffect(() => {
    fetchMovies();
    WebSocketService.connect();
    WebSocketService.subscribe('MOVIE', handleWebSocketUpdate);

    return () => {
      WebSocketService.unsubscribe('MOVIE', handleWebSocketUpdate);
    };
  }, [handleWebSocketUpdate]);

  const fetchMovies = async (sortKey?: string, sortDirection?: 'asc' | 'desc', filterValues?: {[key: string]: string}) => {
    try {
      if (sortKey && sortDirection || filterValues) {
        setSortLoading(true);
      } else {
        setLoading(true);
      }
      const movies = await MovieService.getMovies(sortKey, sortDirection, filterValues);
      setData(movies);
      setError(null);
    } catch (err) {
      setError('Ошибка при загрузке фильмов');
      console.error(err);
    } finally {
      setLoading(false);
      setSortLoading(false);
    }
  };

  const handleCreateMovie = () => {
    setModalMode('create');
    setSelectedMovie(null);
    setIsModalOpen(true);
  };

  const handleEditMovie = () => {
    if (selectedMovieForEdit) {
      setModalMode('edit');
      setSelectedMovie(selectedMovieForEdit);
      setIsModalOpen(true);
    } else {
      alert('Выберите фильм для редактирования');
    }
  };

  const handleDeleteMovie = async () => {
    if (selectedMovieForEdit) {
      if (confirm(`Вы уверены, что хотите удалить фильм "${selectedMovieForEdit.name}"?`)) {
        try {
          setSelectedMovie(selectedMovieForEdit);
          await MovieService.deleteMovie(selectedMovieForEdit.id);
          setSelectedMovieForEdit(null);
          await fetchMovies();
        } catch (error) {
          console.error('Ошибка при удалении фильма:', error);
          alert('Ошибка при удалении фильма');
        }
      }
    } else {
      alert('Выберите фильм для удаления');
    }
  }

  const handleSaveMovie = async (movieData: MovieRequestDto) => {
    try {
      if (modalMode === 'create') {
        await MovieService.createMovie(movieData);
      } else if (selectedMovie) {
        await MovieService.updateMovie(selectedMovie.id, movieData);
      }

      setIsModalOpen(false);
      setSelectedMovieForEdit(null);
      setSelectedMovie(null);

      await fetchMovies();
    } catch (error) {
      console.error('Ошибка при сохранении фильма:', error);
      throw error;
    }
  };

  const handleRowSelect = (movie: MovieResponseDto | null) => {
    setSelectedMovieForEdit(movie);
  };

  const handleSort = (key: string, direction: 'asc' | 'desc') => {
    fetchMovies(key, direction);
  };

  const handleFilter = (newFilters: {[key: string]: string}) => {
    setFilters(newFilters);
    fetchMovies(undefined, undefined, newFilters);
  };

  useEffect(() => {
    if (selectedMovieForEdit) {
      const timer = setTimeout(() => {
        const selectedRow = tableRef.current?.querySelector('.selected');
        if (selectedRow) {
          selectedRow.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
      }, 200);
      return () => clearTimeout(timer);
    }
  }, [selectedMovieForEdit]);

  const handleSearchById = async () => {
    if (!movieIdToSearch) return;

    try {
      const movie = await MovieService.getMovieById(movieIdToSearch);
      setFoundedMovie(movie);

      setTimeout(() => {
        const foundedMovie = foundedMovieRef.current;
        if (foundedMovie) {
          foundedMovie.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
      }, 100);
    } catch (error) {
      console.error('Ошибка при поиске фильма по ID:', error);
      alert(`Фильм с ID ${movieIdToSearch} не найден`);
    }
  };

  const handleFileUpload = async (file: File): Promise<void> => {
    try {
      await UploadFileService.sendFile(file);
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Неизвестная ошибка';
      setErrorMessage(message);
      setErrorModalOpen(true);
    }
  };

  if (loading) return <p>Загрузка...</p>;
  if (error) return <p>Ошибка: {error}</p>;

  return(
    <main>
      <div className="movie-id-search">
        <input
          type="number"
          placeholder="Введите ID фильма"
          value={movieIdToSearch || ''}
          onChange={(e) => setMovieIdToSearch(e.target.value ? parseInt(e.target.value) : undefined)}
          min="1"
        />
        <button
          onClick={handleSearchById}
          disabled={!movieIdToSearch}
        >
          Найти
        </button>
      </div>

      <h1>Список фильмов:</h1>

      <PaginatedTable data={data}
                      columns={columns}
                      onRowSelect={handleRowSelect}
                      selectedItem={selectedMovieForEdit ?? undefined}
                      isFilterable={true}
                      tableRef={tableRef}
                      onSort={handleSort}
                      onFilter={handleFilter}/>

      <div className="table-buttons" ref={buttonsRef}>
          <button onClick={handleCreateMovie}>Добавить</button>
          <button
            onClick={handleEditMovie}
            disabled={!selectedMovieForEdit}
          >
            Изменить
          </button>
          <button
            onClick={handleDeleteMovie}
            disabled={!selectedMovieForEdit}
          >
            Удалить
          </button>
        </div>

      <div className="import-section">
        <ImportInput handleFileUpload={handleFileUpload}/>
      </div>

      <MovieModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSave={handleSaveMovie}
        movie={selectedMovie}
        mode={modalMode}
      />

      {foundedMovie && (
        <div className="founded-movie" ref={foundedMovieRef}>
          <h2>Найденный фильм</h2>
          <div className="movie-info-grid">
            <div className="movie-info-main">
              <div className="movie-title">{foundedMovie.name}</div>
              <div className="movie-id">ID: {foundedMovie.id}</div>
              <div className="movie-date">
                {formatDateTime(foundedMovie.creationDate)}
              </div>
              <div className="movie-genre">{foundedMovie.genre}</div>
            </div>

            <div className="movie-info-section">
              <h3>Основная информация</h3>
              <div className="info-item">
                <span>Бюджет:</span>
                <span>{foundedMovie.budget}</span>
              </div>
              <div className="info-item">
                <span>Сборы:</span>
                <span>{foundedMovie.totalBoxOffice}</span>
              </div>
              <div className="info-item">
                <span>Длительность:</span>
                <span>{foundedMovie.length}</span>
              </div>
              <div className="info-item">
                <span>Рейтинг MPAA:</span>
                <span>{foundedMovie.mpaaRating}</span>
              </div>
            </div>

            <div className="movie-info-section">
              <h3>Награды и координаты</h3>
              <div className="info-item">
                <span>Оскары:</span>
                <span>{foundedMovie.oscarsCount}</span>
              </div>
              <div className="info-item">
                <span>Пальмовые ветви:</span>
                <span>{foundedMovie.goldenPalmCount}</span>
              </div>
              <div className="info-item">
                <span>Координаты:</span>
                <span>X: {foundedMovie.coordinates.x}, Y: {foundedMovie.coordinates.y}</span>
              </div>
            </div>

            <div className="movie-info-section">
              <h3>Съёмочная группа</h3>
              <div className="info-item">
                <span>Режиссер:</span>
                <span>{foundedMovie.director?.name || 'N/A'}</span>
              </div>
              <div className="info-item">
                <span>Сценарист:</span>
                <span>{foundedMovie.screenwriter?.name || 'N/A'}</span>
              </div>
              <div className="info-item">
                <span>Оператор:</span>
                <span>{foundedMovie.operator?.name || 'N/A'}</span>
              </div>
            </div>
          </div>
        </div>
      )}

      <ErrorModal
        isOpen={errorModalOpen}
        onClose={() => setErrorModalOpen(false)}
        error={errorMessage}
      />
    </main>
  )
}

export default Movies;