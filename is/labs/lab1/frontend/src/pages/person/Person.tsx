import {useCallback, useEffect, useRef, useState} from "react";
import PersonService, {type PersonRequestDto, type PersonResponseDto} from "../../services/PersonService.ts";
import PaginatedTable from "../../components/paginatedTable/PaginatedTable.tsx";
import PersonModal from "../../components/personModal/PersonModal.tsx";
import WebSocketService from "../../services/WebSocketService.ts";

function Person() {
  const columns: { [key: string]: string }[] = [
    {id: 'ID'},
    {name: 'Имя'},
    {eyeColor: 'Цвет глаз'},
    {hairColor: 'Цвет волос'},
    {'location.x': 'Координата X'},
    {'location.y': 'Координата Y'},
    {'location.z': 'Координата Z'},
    {'location.name': 'Название локации'},
    {passportID: 'Номер паспорта'},
    {nationality: 'Национальность'}
  ];

  const [data, setData] = useState<PersonResponseDto[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [_sortLoading, setSortLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState<'create' | 'edit'>('create');
  const [selectedPerson, setSelectedPerson] = useState<PersonResponseDto | null>(null);
  const [selectedPersonForEdit, setSelectedPersonForEdit] = useState<PersonResponseDto | null>(null);
  const tableRef = useRef<HTMLDivElement>(null);
  const buttonsRef = useRef<HTMLDivElement>(null);
  const foundedPersonRef = useRef<HTMLDivElement>(null);
  const [personIdToSearch, setPersonIdToSearch] = useState<number | undefined>();
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [_filters, setFilters] = useState<{[key: string]: string}>({});
  const [foundedPerson, setFoundedPerson] = useState<PersonResponseDto | null>(null);

  const handleWebSocketUpdate = useCallback(() => {
    console.log('Received person update via WebSocket');
    fetchPeople();
  }, []);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as Node;

      if (buttonsRef.current?.contains(target)) {
        return;
      }

      if (tableRef.current && !tableRef.current.contains(target)) {
        setSelectedPersonForEdit(null);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  useEffect(() => {
    fetchPeople();

    WebSocketService.connect();
    WebSocketService.subscribe('PERSON', handleWebSocketUpdate);

    return () => {
      WebSocketService.unsubscribe('PERSON', handleWebSocketUpdate);
    };
  }, [handleWebSocketUpdate]);

  const fetchPeople = async (sortKey?: string, sortDirection?: 'asc' | 'desc', filterValues?: {[key: string]: string}) => {
    try {
      if (sortKey && sortDirection || filterValues) {
        setSortLoading(true);
      } else {
        setLoading(true);
      }
      const people = await PersonService.getPeople(sortKey, sortDirection, filterValues);
      setData(people);
    } catch (err) {
      setError('Ошибка при загрузке людей');
      console.error(err);
    } finally {
      setLoading(false);
      setSortLoading(false);
    }
  };

  const handleCreatePerson = () => {
    setModalMode('create');
    setSelectedPerson(null);
    setIsModalOpen(true);
  };

  const handleEditPerson = () => {
    if (selectedPersonForEdit) {
      setModalMode('edit');
      setSelectedPerson(selectedPersonForEdit);
      setIsModalOpen(true);
    } else {
      alert('Выберите человека для редактирования');
    }
  };

  const handleDeletePerson = async () => {
    if (selectedPersonForEdit) {
      if (confirm(`Вы уверены, что хотите удалить "${selectedPersonForEdit.name}"?`)) {
        try {
          setSelectedPerson(selectedPersonForEdit);
          await PersonService.deletePerson(selectedPersonForEdit.id);
          setSelectedPersonForEdit(null);
          await fetchPeople();
        } catch (error) {
          console.error('Ошибка при удалении:', error);
          alert('Ошибка при удалении');
        }
      }
    } else {
      alert('Выберите человека для удаления');
    }
  }

  const handleSavePerson = async (personData: PersonRequestDto) => {
    try {

      if (modalMode === 'create') {
        await PersonService.createPerson(personData);
      } else if (modalMode === 'edit' && selectedPerson?.id) {
        await PersonService.updatePerson(selectedPerson.id, personData);
      }

      setIsModalOpen(false);
      setSelectedPersonForEdit(null);
      setSelectedPerson(null);

      await fetchPeople();
    } catch {
      throw error;
    }
  };

  const handleRowSelect = (person: PersonResponseDto | null) => {
    setSelectedPersonForEdit(person);
  };

  const handleSort = (key: string, direction: 'asc' | 'desc') => {
    fetchPeople(key, direction);
  };

  const handleFilter = (newFilters: {[key: string]: string}) => {
    setFilters(newFilters);
    fetchPeople(undefined, undefined, newFilters);
  };

  useEffect(() => {
    if (selectedPersonForEdit) {
      const timer = setTimeout(() => {
        const selectedRow = tableRef.current?.querySelector('.selected');
        if (selectedRow) {
          selectedRow.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
      }, 200);
      return () => clearTimeout(timer);
    }
  }, [selectedPersonForEdit]);

  const handleSearchById = async () => {
    if (!personIdToSearch) return;

    try {
      const person = await PersonService.getPersonById(personIdToSearch);
      setFoundedPerson(person);

      setTimeout(() => {
        const foundedMovie = foundedPersonRef.current;
        if (foundedMovie) {
          foundedMovie.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
      }, 100);
    } catch (error) {
      console.error('Ошибка при поиске фильма по ID:', error);
      alert(`Фильм с ID ${personIdToSearch} не найден`);
    }
  };

  if (loading) return <p>Загрузка...</p>;
  if (error) return <p>Ошибка: {error}</p>;

  return(
    <main>
      <div className="movie-id-search">
        <input
          type="number"
          placeholder="Введите ID человека"
          value={personIdToSearch || ''}
          onChange={(e) => setPersonIdToSearch(e.target.value ? parseInt(e.target.value) : undefined)}
          min="1"
        />
        <button
          onClick={handleSearchById}
          disabled={!personIdToSearch}
        >
          Найти
        </button>
      </div>

      <h1>Список людей:</h1>

      <PaginatedTable data={data}
                      columns={columns}
                      onRowSelect={handleRowSelect}
                      selectedItem={selectedPersonForEdit ?? undefined}
                      isFilterable={true}
                      tableRef={tableRef}
                      onSort={handleSort}
                      onFilter={handleFilter}/>

      <div className="table-buttons" ref={buttonsRef}>
        <button onClick={handleCreatePerson}>Добавить</button>
        <button
          onClick={handleEditPerson}
          disabled={!selectedPersonForEdit}
        >
          Изменить
        </button>
        <button
          onClick={handleDeletePerson}
        disabled={!selectedPersonForEdit}
        >Удалить</button>
      </div>

      <PersonModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSave={handleSavePerson}
        person={selectedPerson}
        mode={modalMode}
      />

      {foundedPerson && (
        <div className="founded-movie" ref={foundedPersonRef}>
          <h2>Найденный человек</h2>
          <div className="movie-info-grid">
            <div className="movie-info-main">
              <div className="movie-title">{foundedPerson.name}</div>
              <div className="movie-id">ID: {foundedPerson.id}</div>
              <div className="movie-date">
                {foundedPerson.passportID}
              </div>
            </div>

            <div className="movie-info-section">
              <h3>Основная информация</h3>
              <div className="info-item">
                <span>Цвет глаз:</span>
                <span>{foundedPerson.eyeColor}</span>
              </div>
              <div className="info-item">
                <span>Цвет волос:</span>
                <span>{foundedPerson.hairColor}</span>
              </div>
              <div className="info-item">
                <span>Национальность:</span>
                <span>{foundedPerson.nationality}</span>
              </div>
            </div>

            <div className="movie-info-section">
              <h3>Местоположение</h3>
              <div className="info-item">
                <span>Название:</span>
                <span>{foundedPerson.location.name}</span>
              </div>
              <div className="info-item">
                <span>X:</span>
                <span>{foundedPerson.location.x}</span>
              </div>
              <div className="info-item">
                <span>Y:</span>
                <span>{foundedPerson.location.y}</span>
              </div>
              <div className="info-item">
                <span>Z:</span>
                <span>{foundedPerson.location.z}</span>
              </div>
            </div>
          </div>
        </div>
      )}
    </main>
  )
}

export default Person;