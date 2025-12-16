import './BookListModal.scss';
import CardElement from "../card-element/CardElement.tsx";
import TriTovarischaCover from "../../../assets/images/books/tri-tovarischa.jpg";
import { useState } from "react";
import PrimaryButton from "../primary-button/PrimaryButton.tsx";
import OrwellCover from "../../../assets/images/books/1984.jpg";
import GrafMonteCristoCover from "../../../assets/images/books/graf-monte-kristo.jpg";

interface BookListModalProps {
  onClose?: () => void;
  titleId?: string;
  onBooksSelected?: (books: Book[]) => void;
  selectedBooks?: Book[];
  selectionMode?: boolean;
}

interface Book {
  id: string;
  title: string;
  author: string;
  rating: number;
  imageUrl: string;
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
function BookListModal({ onClose: _onClose, titleId, onBooksSelected, selectedBooks = [], selectionMode = false }: BookListModalProps) {
  const [localSelectedBooks, setLocalSelectedBooks] = useState<Book[]>(selectedBooks);

  const books: Book[] = [
    { id: '1', title: "Три товарища", author: "Эрих Мария Ремарк", rating: 4.5, imageUrl: TriTovarischaCover },
    { id: '2', title: "Граф Монте-Кристо", author: "Александр Дюма", rating: 4.8, imageUrl: GrafMonteCristoCover },
    { id: '3', title: "1984", author: "Джордж Оруэлл", rating: 4.6, imageUrl: OrwellCover },
  ];

  const handleBookClick = (book: Book) => {
    if (selectionMode) {
      setLocalSelectedBooks(prev => {
        const isSelected = prev.some(b => b.id === book.id);
        return isSelected
          ? prev.filter(b => b.id !== book.id)
          : [...prev, book];
      });
    }
  };

  const handleConfirm = () => {
    if (onBooksSelected) {
      onBooksSelected(localSelectedBooks);
    }
  };

  return (
    <div className="book-list-modal-container">
      <h2 className="book-list-modal-title" id={titleId}>
        Выберите книги
      </h2>
      <div className="book-list">
        {books.map((book) => (
          <div
            key={book.id}
            onClick={() => handleBookClick(book)}
            className={localSelectedBooks.some(b => b.id === book.id) ? 'selected-book' : ''}
          >
            <CardElement
              title={book.title}
              description={book.author}
              imageUrl={book.imageUrl}
              button={false}
            />
          </div>
        ))}
      </div>
      {selectionMode && (
        <div className="modal-actions">
          <PrimaryButton
            label={`Добавить (${localSelectedBooks.length})`}
            onClick={handleConfirm}
            type="button"
          />
        </div>
      )}
    </div>
  );
}

export default BookListModal;