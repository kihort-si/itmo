import {useSelector} from "react-redux";
import type {RootState} from "../../../redux/store.ts";
import {useState} from "react";
import {useLocation, useNavigate} from "react-router-dom";
import PrimaryButton from "../../controls/primary-button/PrimaryButton.tsx";
import SecondaryButton from "../../controls/secondary-button/SecondaryButton.tsx";
import Input from "../../controls/input/Input.tsx";
import Change from "../../../assets/elements/change-pink.svg";
import Delete from "../../../assets/elements/delete-pink.svg";
import './Book.scss';
import Stars from "../../stars/Stars.tsx";
import Modal from "../../controls/modal/Modal.tsx";
import ConfirmDialog from "../../controls/confirm-dialog/ConfirmDialog.tsx";
import ReadingForm from "../../controls/reading-form/ReadingForm.tsx";
import CollectionListModal from "../../controls/collection-list-modal/CollectionListModal.tsx";

function Book() {
  const isAuthenticated = useSelector((state: RootState) => state.auth.isAuthenticated);
  const location = useLocation();
  const navigate = useNavigate();
  const [isEditMode, setEditMode] = useState(location.state?.isEditMode || false);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [showReadingForm, setShowReadingForm] = useState(false);
  const [showCollectionForm, setShowCollectionForm] = useState(false);
  const [userRating, setUserRating] = useState(0);

  const [formData, setFormData] = useState({
    title: location.state?.title || 'Книга',
    author: location.state?.author || 'Автор',
    coverUrl: location.state?.coverUrl || undefined,
    genre: location.state?.genre || 'Жанр',
    year: location.state?.year || 1924,
    rating: location.state?.rating || 5,
    isMine: location.state?.isMine || false,
    pages: '256',
    description: 'Описание книги...'
  });

  const handleSaveReview = () => {
    navigate(-1);
  }

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setEditMode(false);

    const updatedBook = {
      id: location.state?.id,
      title: formData.title,
      author: formData.author,
      rating: formData.rating,
      imageUrl: formData.coverUrl
    };

    sessionStorage.setItem('updatedBook', JSON.stringify(updatedBook));
    navigate(-1);
  }

  const confirmDelete = () => {
    setShowDeleteDialog(false);
    sessionStorage.setItem('deletedBookId', location.state?.id || '');
    navigate(-1);
  }

  const handleAddToCollection = () => {
    setShowCollectionForm(true);
  }

  const handleMarkAsRead = () => {
    setShowReadingForm(true);
  }

  const handleDelete = () => {
    setShowDeleteDialog(true);
  }

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({...prev, [field]: value}));
  }

  const getReadingStats = () => {
    const bookId = location.state?.id || formData.title;

    const idHash = bookId.toString().split('').reduce((acc: number, char: string) => acc + char.charCodeAt(0), 0);

    const totalPages = parseInt(formData.pages) || 256;

    const pagesReadPercentage = 10 + (idHash % 85);
    const pagesRead = Math.floor((totalPages * pagesReadPercentage) / 100);

    const daysAgo = 1 + (idHash % 30);
    const lastReadDate = new Date();
    lastReadDate.setDate(lastReadDate.getDate() - daysAgo);

    const formatDate = (date: Date, daysAgo: number) => {
      const day = date.getDate();
      const month = date.toLocaleString('ru-RU', {month: 'long'});
      const year = date.getFullYear();

      let daysAgoStr = '';
      if (daysAgo === 1 || daysAgo === 21) {
        daysAgoStr = `${daysAgo} день`;
      } else if ((daysAgo >= 2 && daysAgo <= 4) || (daysAgo >= 22 && daysAgo <= 24)) {
        daysAgoStr = `${daysAgo} дня`;
      } else {
        daysAgoStr = `${daysAgo} дней`;
      }

      return `${daysAgoStr} назад (${day} ${month} ${year})`;
    };

    return {
      pagesRead,
      totalPages,
      percentage: Math.round((pagesRead / totalPages) * 100),
      lastRead: formatDate(lastReadDate, daysAgo)
    };
  };

  const readingStats = formData.isMine && isAuthenticated && !isEditMode ? getReadingStats() : null;

  return (
    <main>
      <div className="book-page-container container">
        <div className="book-info-panel">
          <button
            className="page-close-button"
            onClick={() => navigate(-1)}
            aria-label="Вернуться назад"
            data-tooltip="Закрыть окно"
          >
            ×
          </button>
          <div className="book-info-title">
            <h2 className="book-title">{formData.title}</h2>
            {formData.isMine && isAuthenticated && (
              <div>
                {!isEditMode && (
                  <button onClick={() => setEditMode(true)}><img src={Change}/></button>
                )}
                <button onClick={handleDelete}><img src={Delete}/></button>
              </div>
            )}
          </div>

          <form className="book-form" onSubmit={handleSubmit}>
            <div className="book-info-body">
              <div className="book-info-fields">
                {isEditMode ? (
                  <label htmlFor="author">Автор*</label>
                ) : (
                  <label htmlFor="author">Автор</label>
                )}
                {isEditMode ? (
                  <Input
                    type="text"
                    id="author"
                    name="author"
                    placeholder="Агата Кристи"
                    required
                    value={formData.author}
                    onChange={(value) => handleInputChange('author', value)}
                  />
                ) : (
                  <span className="field-value">{formData.author}</span>
                )}

                {isEditMode ? (
                  <label htmlFor="year">Год издания*</label>
                ) : (
                  <label htmlFor="year">Год издания</label>
                )}
                {isEditMode ? (
                  <Input
                    type="number"
                    id="year"
                    name="year"
                    placeholder="1939"
                    required
                    value={formData.year}
                    onChange={(value) => handleInputChange('year', value)}
                    min="1000"
                    max="2025"
                  />
                ) : (
                  <span className="field-value">{formData.year}</span>
                )}

                {isEditMode ? (
                  <label htmlFor="genre">Жанр*</label>
                ) : (
                  <label htmlFor="genre">Жанр</label>
                )}
                {isEditMode ? (
                  <Input
                    type="text"
                    id="genre"
                    name="genre"
                    placeholder="Детектив"
                    required
                    value={formData.genre}
                    onChange={(value) => handleInputChange('genre', value)}
                  />
                ) : (
                  <span className="field-value">{formData.genre}</span>
                )}

                {isEditMode ? (
                  <label htmlFor="pages">Количество страниц*</label>
                ) : (
                  <label htmlFor="pages">Количество страниц</label>
                )}
                {isEditMode ? (
                  <Input
                    type="number"
                    id="pages"
                    name="pages"
                    placeholder="256"
                    required
                    value={formData.pages}
                    onChange={(value) => handleInputChange('pages', value)}
                    min="1"
                  />
                ) : (
                  <span className="field-value">{formData.pages}</span>
                )}

                <label htmlFor="description">Описание</label>
                {isEditMode ? (
                  <textarea
                    id="description"
                    name="description"
                    className="book-description"
                    value={formData.description}
                    onChange={(e) => handleInputChange('description', e.target.value)}
                    placeholder="Всех приглашают на таинственный остров..."
                  />
                ) : (
                  <p className="field-value">{formData.description}</p>
                )}

                {isEditMode && (
                  <>
                    <label htmlFor="cover">Обложка</label>
                    <input
                      type="file"
                      id="cover"
                      name="cover"
                      accept="image/*"
                      className="file-input"
                    />
                  </>
                )}

                {isAuthenticated && !isEditMode && (
                  <div className="book-action-buttons">
                    <PrimaryButton label={"Отметить прочитанное"} onClick={handleMarkAsRead} type="button"/>
                    <SecondaryButton label={"Добавить в коллекцию"} onClick={handleAddToCollection} type="button"/>
                    <div>
                      <label htmlFor="review">Оставить отзыв</label>
                      <textarea
                        id="review"
                        name="review"
                        className="book-description"
                        placeholder="Очень интересная книга..."
                      />
                    </div>
                  </div>
                )}
              </div>

              <div className="book-info-cover">
                <div className="book-info-rating">
                  <span>Рейтинг: </span>
                  <span>{formData.rating}</span>
                </div>
                <img
                  className="book-cover"
                  src={formData.coverUrl}
                  alt={`Обложка книги ${formData.title}`}
                />
                {isAuthenticated && (
                  <div className="book-info-save-review">
                    {!isEditMode && (
                      <div className="book-info-save-stars">
                        <Stars count={userRating} onChange={setUserRating}/>
                      </div>
                    )}
                    {!isEditMode && (
                      <PrimaryButton label={"Сохранить отзыв"} onClick={handleSaveReview}/>
                    )}
                  </div>
                )}
              </div>
            </div>
            {isEditMode && (
              <div className="book-edit-buttons">
                <PrimaryButton label={"Сохранить изменения"} type="submit" onClick={() => {
                }}/>
                <SecondaryButton label={"Отмена"} type="button" onClick={() => setEditMode(false)}/>
              </div>
            )}
          </form>

          {readingStats && (
            <div className="book-reading-stats">
              <label>Статистика чтения</label>
              <div className="reading-stats-content">
                <div className="reading-progress">
                  <div className="progress-bar">
                    <div
                      className="progress-fill"
                      style={{width: `${readingStats.percentage}%`}}
                    ></div>
                  </div>
                  <div className="progress-text">
                    <span className="pages-read">{readingStats.pagesRead}</span>
                    <span className="pages-separator"> из </span>
                    <span className="pages-total">{readingStats.totalPages}</span>
                    <span className="pages-label"> страниц</span>
                    <span className="percentage"> ({readingStats.percentage}%)</span>
                  </div>
                </div>
                <div className="last-read-info">
                  <span className="last-read-label">Последний раз читалась:</span>
                  <span className="last-read-date">{readingStats.lastRead}</span>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      <Modal open={showDeleteDialog} onClose={() => setShowDeleteDialog(false)}>
        <ConfirmDialog
          title="Удаление книги"
          message="Вы уверены, что хотите удалить эту книгу?"
          onConfirm={confirmDelete}
          onCancel={() => setShowDeleteDialog(false)}
        />
      </Modal>

      <Modal open={showReadingForm} onClose={() => setShowReadingForm(false)}>
        <ReadingForm
          bookTitle={formData.title}
          bookId={location.state?.id?.toString() || ''}
          bookAuthor={formData.author}
          bookRating={formData.rating}
          bookImageUrl={formData.coverUrl || ''}
          onSubmit={(readingData) => {
            const endDate = new Date(readingData.endDate);
            const today = new Date();
            const isToday = endDate.toDateString() === today.toDateString();
            const isThisMonth = endDate.getMonth() === today.getMonth() && endDate.getFullYear() === today.getFullYear();
            const isThisYear = endDate.getFullYear() === today.getFullYear();

            const book = {
              id: readingData.bookId || location.state?.id?.toString() || `temp-${Date.now()}`,
              title: readingData.title,
              author: readingData.author,
              rating: readingData.rating || formData.rating,
              cover: readingData.imageUrl || formData.coverUrl || ''
            };

            const isBookInList = (books: typeof book[], title: string, author: string): boolean => {
              return books.some(b => b.title === title && b.author === author);
            };

            if (isToday || isThisMonth) {
              const stored = sessionStorage.getItem('readingBooks');
              const currentBooks = stored ? JSON.parse(stored) : [];
              if (!isBookInList(currentBooks, book.title, book.author)) {
                sessionStorage.setItem('readingBooks', JSON.stringify([...currentBooks, book]));
              }
            }

            if (readingData.isFinished && isThisYear) {
              const stored = sessionStorage.getItem('yearlyBooks');
              const currentBooks = stored ? JSON.parse(stored) : [];
              if (!isBookInList(currentBooks, book.title, book.author)) {
                sessionStorage.setItem('yearlyBooks', JSON.stringify([...currentBooks, book]));
              }
            }

            setShowReadingForm(false);
          }}
        />
      </Modal>

      <Modal open={showCollectionForm} onClose={() => setShowCollectionForm(false)}>
        <CollectionListModal
          onCollectionSelected={(collectionIds) => {
            console.log('Книга добавлена в коллекции:', collectionIds);
            setShowCollectionForm(false);
          }}
        />
      </Modal>
    </main>
  );
}

export default Book;