import {useState} from "react";
import './PaginatedTable.scss';
import {formatDateTime} from "../../utils/dateUtils.ts";
import {formatCurrency} from "../../utils/currencyUtils.ts";

interface PaginatedTableProps<T> {
  columns: { [key: string]: string }[];
  data: T[];
  onRowSelect?: (item: T | null) => void;
  selectedItem?: T;
  isFilterable?: boolean;
  tableRef?: React.RefObject<HTMLDivElement | null>;
  onSort?: (key: string, direction: 'asc' | 'desc') => void;
  onFilter?: (filters: {[key: string]: string}) => void;
}

function PaginatedTable<T>({columns, data, onRowSelect, selectedItem, isFilterable, tableRef, onSort, onFilter}: PaginatedTableProps<T>) {
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage] = useState(10);
  const [sortConfig, setSortConfig] = useState<{ key: string | null, direction: 'asc' | 'desc' | null }>({
    key: null,
    direction: null,
  });
  const [filterValues, setFilterValues] = useState<{[key: string]: string}>({});

  const getNestedValue = (obj: T, path: string): unknown => {
    return path.split('.').reduce((current: unknown, key: string): unknown => {
      return current && typeof current === 'object' ? (current as Record<string, unknown>)[key] : undefined;
    }, obj);
  };

  const indexOfLastItem: number = currentPage * itemsPerPage;
  const indexOfFirstItem: number = indexOfLastItem - itemsPerPage;

  const totalPages: number = Math.ceil(data.length / itemsPerPage);

  const handleNextPage = (): void => {
    if (currentPage < totalPages) {
      setCurrentPage(prevPage => prevPage + 1);
    }
  }

  const handlePrevPage = (): void => {
    if (currentPage > 1) {
      setCurrentPage(prevPage => prevPage - 1);
    }
  }

  const handlePageClick = (pageNumber: number): void => {
    setCurrentPage(pageNumber);
  }

  const isPaginationNeeded: boolean = data.length > itemsPerPage;

  const handleSort = (key: string): void => {
    let direction: 'asc' | 'desc' = 'asc';
    if (sortConfig.key === key) {
      direction = sortConfig.direction === 'asc' ? 'desc' : 'asc';
    }
    setSortConfig({
      key: key,
      direction: direction
    });

    if (onSort) {
      onSort(key, direction);
    }
  };

  const handleFilterChange = (key: string, value: string) => {
    setFilterValues({...filterValues, [key]: value});
  };

  const applyFilter = () => {
    if (onFilter) {
      onFilter(filterValues);
    }
  };

  const clearFilters = () => {
    setFilterValues({});
    if (onFilter) {
      onFilter({});
    }
  };

  const formatValue = (key: string, value: unknown): string => {
    if (value === undefined || value === null) return "N/A";

    if (key === 'creationDate' && typeof value === 'string') {
      return formatDateTime(value);
    }

    if ((key === 'budget' && typeof value === 'number') || (key === 'totalBoxOffice' && typeof value === 'number')) {
      return formatCurrency(value);
    }

    return String(value);
  };

  return (
    <div ref={tableRef} className="table-container">
      <table>
        <thead>
        <tr>
          {columns.map((col: {[key: string]: string}, index: number) => {
            const columnKey: string = Object.keys(col)[0];
            return (
              <th key={index}>
                <div className="th-content" {...isFilterable ? {style: {minHeight: '4rem'}} : {}}>
                  <div className="sort-header" onClick={() => handleSort(columnKey)}>
                    {Object.values(col)[0]}
                    {sortConfig.key === columnKey && (sortConfig.direction === 'asc' ? ' 🔼' : ' 🔽')}
                  </div>
                  {isFilterable ?
                  <input
                    type="text"
                    className="filter-input"
                    placeholder="Фильтр..."
                    value={filterValues[columnKey] || ''}
                    onChange={(e) => handleFilterChange(columnKey, e.target.value)}
                    onClick={(e) => e.stopPropagation()}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') {
                        applyFilter();
                      }
                    }}
                  />
                  : null}
                </div>
              </th>
            );
          })}
        </tr>
        </thead>
        <tbody>
        {data.slice(indexOfFirstItem, indexOfLastItem).map((item: T, rowIndex: number) => (
          <tr key={rowIndex}
              className={selectedItem === item ? 'selected' : ''}
              onClick={() => onRowSelect?.(item)}
              style={{ cursor: onRowSelect ? 'pointer' : 'inherit' }}>
            {columns.map((col: {[key: string]: string}, colIndex: number) => {
              const key: string = Object.keys(col)[0];
              const value: unknown = getNestedValue(item, key);
              return (
                <td key={colIndex}>
                  {formatValue(key, value)}
                </td>
              );
            })}
          </tr>
        ))}
        </tbody>
      </table>

      {data.length === 0 && (
        <tr>
          <td colSpan={columns.length} style={{textAlign: 'center', padding: '20px'}}>
            По вашему запросу ничего не найдено
          </td>
        </tr>
      )}

      {isFilterable &&
        <div className="filter-controls">
          <button
            onClick={applyFilter}
            disabled={!Object.values(filterValues).some(value => value !== undefined && value !== '')}
          >
            Применить фильтры
          </button>
          <button
            onClick={clearFilters}
            disabled={!Object.values(filterValues).some(value => value !== undefined && value !== '')}
          >
            Очистить фильтры
          </button>
        </div>
      }

      {!isPaginationNeeded ? null :
        <div className="pagination">
          <button onClick={handlePrevPage} disabled={currentPage === 1}>Предыдущая</button>
          {[...Array(totalPages)].map((_, index) => (
            <button
              key={index}
              onClick={() => handlePageClick(index + 1)}
              disabled={currentPage === index + 1}
            >
              {index + 1}
            </button>
          ))}
          <button onClick={handleNextPage} disabled={currentPage === totalPages}>Следующая</button>
        </div>
      }
    </div>
  )
}

export default PaginatedTable;