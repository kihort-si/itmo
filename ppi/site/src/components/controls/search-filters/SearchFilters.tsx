import './SearchFilters.scss';
import PrimaryButton from "../primary-button/PrimaryButton.tsx";
import SelectWithSearch from "../select-with-search/SelectWithSearch.tsx";

interface SearchFiltersProps {
  selectedTypes: string[];
  onTypeChange: (type: string) => void;
  selectedGenre: string;
  onGenreChange: (genre: string) => void;
  onReset: () => void;
}

function SearchFilters({ selectedTypes, onTypeChange, selectedGenre, onGenreChange, onReset }: SearchFiltersProps) {
  const types = [
    { id: 'books', label: 'Книги' },
    { id: 'users', label: 'Пользователи' },
    { id: 'collections', label: 'Коллекции' }
  ];

  const genres = [
    'Все жанры',
    'Классика',
    'Фантастика',
    'Детектив',
    'Роман',
    'Фэнтези'
  ];

  return (
    <div className="search-filters">
      <div className="filters-group">
        <div className="filter-types">
          {types.map(type => (
            <label key={type.id} className="checkbox-label">
              <input
                type="checkbox"
                checked={selectedTypes.includes(type.id)}
                onChange={() => onTypeChange(type.id)}
              />
              <span>{type.label}</span>
            </label>
          ))}
        </div>

        <div className="filter-genre">
          <SelectWithSearch
            value={selectedGenre}
            options={genres}
            onChange={onGenreChange}
            placeholder="Выберите жанр"
          />
        </div>
      </div>

      <PrimaryButton label="Сбросить фильтры" onClick={onReset} />
    </div>
  );
}

export default SearchFilters;