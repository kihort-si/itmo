import './SelectWithSearch.scss';
import { useState, useRef, useEffect } from 'react';
import searchIcon from '../../../assets/elements/search.svg';

interface SelectWithSearchProps {
  value: string;
  options: string[];
  onChange: (value: string) => void;
  placeholder?: string;
}

function SelectWithSearch({ value, options, onChange, placeholder = 'Выберите...' }: SelectWithSearchProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setIsOpen(false);
        setSearchQuery('');
      }
    };

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
      return () => document.removeEventListener('mousedown', handleClickOutside);
    }
  }, [isOpen]);

  const filteredOptions = options.filter(option =>
    option.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleSelect = (option: string) => {
    onChange(option);
    setIsOpen(false);
    setSearchQuery('');
  };

  const handleToggle = () => {
    setIsOpen(!isOpen);
    if (!isOpen) {
      setSearchQuery('');
    }
  };

  return (
    <div className="select-with-search" ref={containerRef}>
      <div 
        className="select-with-search-trigger" 
        onClick={handleToggle}
      >
        <span className={value ? 'selected-value' : 'placeholder'}>{value || placeholder}</span>
        <span className={`arrow ${isOpen ? 'open' : ''}`}>▼</span>
      </div>

      {isOpen && (
        <div className="select-with-search-dropdown">
          <div className="select-with-search-search">
            <img src={searchIcon} alt="Поиск" className="search-icon" />
            <input
              type="text"
              placeholder="Поиск..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              autoFocus
              onClick={(e) => e.stopPropagation()}
            />
          </div>
          <div className="select-with-search-options">
            {filteredOptions.length > 0 ? (
              filteredOptions.map((option) => (
                <div
                  key={option}
                  className={`select-with-search-option ${value === option ? 'selected' : ''}`}
                  onClick={() => handleSelect(option)}
                >
                  {option}
                </div>
              ))
            ) : (
              <div className="select-with-search-no-results">Нет результатов</div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default SelectWithSearch;

