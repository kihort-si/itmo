import './BookForm.scss';
import Input from "../input/Input.tsx";
import PrimaryButton from "../primary-button/PrimaryButton.tsx";
import { useState } from "react";
import SecondaryButton from "../secondary-button/SecondaryButton.tsx";

export interface BookFormData {
  title: string;
  author: string;
  year: number;
  genre: string;
  pages: number;
  description?: string;
  imageUrl: string;
}

interface BookFormProps {
  onSubmit: (book: BookFormData) => void;
  onCancel: () => void;
  titleId?: string;
}

function BookForm({ onSubmit, onCancel, titleId }: BookFormProps) {
  const [title, setTitle] = useState('');
  const [author, setAuthor] = useState('');
  const [year, setYear] = useState('');
  const [genre, setGenre] = useState('');
  const [pages, setPages] = useState('');
  const [description, setDescription] = useState('');
  const [imagePreview, setImagePreview] = useState<string>('');

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

    const bookData: BookFormData = {
      title,
      author,
      year: Number(year),
      genre,
      pages: Number(pages),
      description: description || undefined,
      imageUrl: imagePreview || ''
    };

    onSubmit(bookData);
  };

  return (
    <div className="book-form-container">
      <h2 className="book-form-title" id={titleId}>
        Добавить книгу
      </h2>
      <form className="book-form" onSubmit={handleSubmit}>
        <label htmlFor="title">Название*</label>
        <Input
          type="text"
          id="title"
          name="title"
          placeholder="Десять негритят"
          required
          value={title}
          onChange={(value) => setTitle(value)}
        />

        <label htmlFor="author">Автор*</label>
        <Input
          type="text"
          id="author"
          name="author"
          placeholder="Агата Кристи"
          required
          value={author}
          onChange={(value) => setAuthor(value)}
        />

        <label htmlFor="year">Год издания*</label>
        <Input
          type="number"
          id="year"
          name="year"
          placeholder="1939"
          required
          value={year}
          onChange={(value) => setYear(value)}
          min="1000"
          max="2025"
        />

        <label htmlFor="genre">Жанр*</label>
        <Input
          type="text"
          id="genre"
          name="genre"
          placeholder="Детектив"
          required
          value={genre}
          onChange={(value) => setGenre(value)}
        />

        <label htmlFor="pages">Количество страниц*</label>
        <Input
          type="number"
          id="pages"
          name="pages"
          placeholder="256"
          required
          value={pages}
          onChange={(value) => setPages(value)}
          min="1"
        />

        <label htmlFor="description">Описание</label>
        <textarea
          id="description"
          name="description"
          className="book-description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="Всех приглашают на таинственный остров..."
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

        <div className="form-buttons">
          <PrimaryButton label="Сохранить" type="submit" onClick={() => {}} />
          <SecondaryButton label="Отменить" onClick={onCancel} />
        </div>
      </form>
    </div>
  );
}

export default BookForm;