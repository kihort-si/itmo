import './Collection.scss';
import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import type { RootState } from '../../../redux/store.ts';
import CardElement from '../../controls/card-element/CardElement.tsx';
import PrimaryButton from '../../controls/primary-button/PrimaryButton.tsx';
import SecondaryButton from '../../controls/secondary-button/SecondaryButton.tsx';
import Modal from '../../controls/modal/Modal.tsx';
import ConfirmDialog from '../../controls/confirm-dialog/ConfirmDialog.tsx';
import BookListModal from '../../controls/book-list-modal/BookListModal.tsx';
import Change from '../../../assets/elements/change-pink.svg';
import Delete from '../../../assets/elements/delete-pink.svg';
import DefaultCover from '../../../assets/images/books/tri-tovarischa.jpg';

interface Book {
  id: string;
  title: string;
  author: string;
  rating?: number;
  imageUrl: string;
}

function Collection() {
  const location = useLocation();
  const navigate = useNavigate();
  const isAuthenticated = useSelector((state: RootState) => state.auth.isAuthenticated);
  const [isEditMode, setEditMode] = useState(false);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [showDeleteBookDialog, setShowDeleteBookDialog] = useState(false);
  const [showBookListModal, setShowBookListModal] = useState(false);
  const [selectedBookId, setSelectedBookId] = useState<string | null>(null);

  const [collectionData, setCollectionData] = useState({
    id: location.state?.id || '',
    name: location.state?.name || 'Коллекция',
    isMine: location.state?.isMine || false,
    coverUrl: location.state?.coverUrl || DefaultCover,
  });

  const [originalData, setOriginalData] = useState(collectionData);

  const handleEditMode = (enabled: boolean) => {
    if (enabled) {
      setOriginalData(collectionData);
      setEditMode(true);
    } else {
      setCollectionData(originalData);
      setEditMode(false);
    }
  };

  const [books, setBooks] = useState<Book[]>(location.state?.books || []);

  const handleDeleteCollection = () => {
    setShowDeleteDialog(true);
  };

  const confirmDeleteCollection = () => {
    setShowDeleteDialog(false);
    sessionStorage.setItem('deletedCollectionId', collectionData.id);
    navigate('/library');
  };

  const handleDeleteBook = (bookId: string) => {
    setSelectedBookId(bookId);
    setShowDeleteBookDialog(true);
  };

  const confirmDeleteBook = () => {
    if (selectedBookId) {
      setBooks(prev => prev.filter(book => book.id !== selectedBookId));
    }
    setShowDeleteBookDialog(false);
    setSelectedBookId(null);
  };

  const handleAddBook = () => {
    setShowBookListModal(true);
  };

  const handleBooksSelected = (selectedBooks: Book[]) => {
    setBooks(prev => [...prev, ...selectedBooks]);
    setShowBookListModal(false);
  };

  const handleBookClick = (book: Book) => {
    navigate('/book', {
      state: {
        id: book.id,
        title: book.title,
        author: book.author,
        rating: book.rating,
        coverUrl: book.imageUrl,
        isMine: collectionData.isMine,
        isEditMode: false
      }
    });
  };

  const handleSaveChanges = () => {
    setEditMode(false);

    const updatedCollection = {
      id: collectionData.id,
      name: collectionData.name,
      coverUrl: collectionData.coverUrl,
    };

    sessionStorage.setItem('updatedCollection', JSON.stringify(updatedCollection));
    navigate('/library');
  };

  const handleCoverChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setCollectionData(prev => ({ ...prev, coverUrl: reader.result as string }));
      };
      reader.readAsDataURL(file);
    }
  };

  return (
    <main>
      <div className="collection-page-container">
        <button
          className="page-close-button"
          onClick={() => navigate(-1)}
          aria-label="Вернуться назад"
          data-tooltip="Закрыть окно"
        >
          ×
        </button>
        <div className="collection-header">
          {isEditMode ? (
            <input
              type="text"
              className="collection-title-input"
              value={collectionData.name}
              onChange={(e) => setCollectionData(prev => ({ ...prev, name: e.target.value }))}
              autoFocus
            />
          ) : (
            <h2 className="collection-title">{collectionData.name}</h2>
          )}

          {collectionData.isMine && isAuthenticated && !isEditMode && (
            <div className="collection-actions">
              <button type="button" onClick={() => handleEditMode(!isEditMode)}>
                <img src={Change} alt="Изменить" />
              </button>
            </div>
          )}
        </div>

        {isEditMode && (
          <div className="collection-cover-edit">
            <img src={collectionData.coverUrl} alt="Обложка коллекции" className="collection-cover-preview" />
            <label htmlFor="cover-upload" className="cover-upload-label">
              Изменить обложку
            </label>
            <input
              type="file"
              id="cover-upload"
              accept="image/*"
              onChange={handleCoverChange}
              className="cover-upload-input"
            />
          </div>
        )}

        {collectionData.isMine && isAuthenticated && (
          <div className="collection-buttons">
            {isEditMode ? (
              <>
                <PrimaryButton label="Сохранить изменения" onClick={handleSaveChanges} type="button" />
                <SecondaryButton label="Отмена" onClick={() => handleEditMode(false)} type="button" />
              </>
            ) : (
              <>
                <PrimaryButton label="Добавить книгу" onClick={handleAddBook} type="button" />
                <SecondaryButton label="Удалить коллекцию" onClick={handleDeleteCollection} type="button" />
              </>
            )}
          </div>
        )}

        <div className="collection-books-grid">
          {books.map((book) => (
            <div key={book.id} className="collection-book-item">
              {collectionData.isMine && isAuthenticated && !isEditMode && (
                <button
                  type="button"
                  className="delete-book-button"
                  onClick={(e) => {
                    e.stopPropagation();
                    handleDeleteBook(book.id);
                  }}
                >
                  <img src={Delete} alt="Удалить из коллекции" />
                </button>
              )}
              <div onClick={() => handleBookClick(book)}>
                <CardElement
                  title={book.title}
                  description={book.author}
                  starsCount={book.rating}
                  imageUrl={book.imageUrl}
                  button={false}
                />
              </div>
            </div>
          ))}
        </div>
      </div>

      <Modal open={showDeleteDialog} onClose={() => setShowDeleteDialog(false)}>
        <ConfirmDialog
          title="Удаление коллекции"
          message="Вы уверены, что хотите удалить эту коллекцию?"
          onConfirm={confirmDeleteCollection}
          onCancel={() => setShowDeleteDialog(false)}
        />
      </Modal>

      <Modal open={showDeleteBookDialog} onClose={() => setShowDeleteBookDialog(false)}>
        <ConfirmDialog
          title="Удаление книги из коллекции"
          message="Вы уверены, что хотите удалить эту книгу из коллекции?"
          onConfirm={confirmDeleteBook}
          onCancel={() => setShowDeleteBookDialog(false)}
        />
      </Modal>

      <Modal open={showBookListModal} onClose={() => setShowBookListModal(false)}>
        <BookListModal
          onBooksSelected={handleBooksSelected}
          selectionMode={true}
        />
      </Modal>
    </main>
  );
}

export default Collection;