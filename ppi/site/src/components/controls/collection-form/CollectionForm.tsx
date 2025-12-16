import './CollectionForm.scss';
import Input from "../input/Input.tsx";
import PrimaryButton from "../primary-button/PrimaryButton.tsx";
import SecondaryButton from "../secondary-button/SecondaryButton.tsx";
import { useState } from "react";
import Modal from "../modal/Modal.tsx";
import BookListModal from "../book-list-modal/BookListModal.tsx";

export interface CollectionFormData {
  title: string;
  access: 'public' | 'private';
  description?: string;
  imageUrl: string;
  bookIds: string[];
}

interface CollectionFormProps {
  onSubmit: (collection: CollectionFormData) => void;
  onCancel: () => void;
  titleId?: string;
}

interface Book {
  id: string;
  title: string;
  author: string;
  rating: number;
  imageUrl: string;
}

function CollectionForm({ onSubmit, onCancel, titleId }: CollectionFormProps) {
  const [title, setTitle] = useState('');
  const [access, setAccess] = useState<'public' | 'private'>('public');
  const [description, setDescription] = useState('');
  const [imagePreview, setImagePreview] = useState<string>('');
  const [isBookListOpen, setIsBookListOpen] = useState(false);
  const [selectedBooks, setSelectedBooks] = useState<Book[]>([]);
  const [error, setError] = useState<string>('');

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setImagePreview(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (selectedBooks.length === 0) {
      setError('Добавьте хотя бы одну книгу в коллекцию');
      return;
    }

    const collectionData: CollectionFormData = {
      title,
      access,
      description: description || undefined,
      imageUrl: imagePreview || '',
      bookIds: selectedBooks.map(book => book.id)
    };

    onSubmit(collectionData);
  };

  const handleBooksSelected = (books: Book[]) => {
    setSelectedBooks(books);
    setError('');
    setIsBookListOpen(false);
  };

  return (
    <>
      <div className="collection-form-container">
        <h2 className="collection-form-title" id={titleId}>
          Создать коллекцию
        </h2>
        <form className="collection-form" onSubmit={handleSubmit}>
          <label htmlFor="title">Название*</label>
          <Input
            type="text"
            id="title"
            name="title"
            placeholder="Моя коллекция"
            required
            value={title}
            onChange={(value) => setTitle(value)}
          />

          <label htmlFor="access">Доступ*</label>
          <div className="access-options">
            <label className="radio-option">
              <input
                type="radio"
                name="access"
                value="public"
                checked={access === 'public'}
                onChange={() => setAccess('public')}
              />
              <span>Публичная</span>
            </label>
            <label className="radio-option">
              <input
                type="radio"
                name="access"
                value="private"
                checked={access === 'private'}
                onChange={() => setAccess('private')}
              />
              <span>Приватная</span>
            </label>
          </div>

          <label htmlFor="description">Описание</label>
          <textarea
            id="description"
            name="description"
            className="collection-description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Введите описание коллекции..."
          />

          <label htmlFor="cover">Обложка</label>
          <input
            type="file"
            id="cover"
            name="cover"
            accept="image/*"
            className="file-input"
            onChange={handleImageUpload}
          />

          {imagePreview && (
            <div className="image-preview">
              <img src={imagePreview} alt="Предпросмотр обложки" />
            </div>
          )}

          <div className="add-books-section">
            <SecondaryButton
              label="Добавить книги"
              onClick={() => setIsBookListOpen(true)}
            />
            {selectedBooks.length > 0 && (
              <p className="books-count">Выбрано книг: {selectedBooks.length}</p>
            )}
          </div>

          {error && <div className="collection-error">{error}</div>}

          <div className="form-buttons">
            <PrimaryButton label="Создать" type="submit" onClick={() => {}} />
            <SecondaryButton label="Отменить" type="button" onClick={onCancel} />
          </div>
        </form>
      </div>

      <Modal open={isBookListOpen} onClose={() => setIsBookListOpen(false)}>
        <BookListModal
          onClose={() => setIsBookListOpen(false)}
          onBooksSelected={handleBooksSelected}
          selectedBooks={selectedBooks}
          selectionMode={true}
        />
      </Modal>
    </>
  );
}

export default CollectionForm;