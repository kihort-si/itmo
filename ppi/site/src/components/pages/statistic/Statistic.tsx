import '../home/Home.scss';
import './Statistic.scss';
import PrimaryButton from "../../controls/primary-button/PrimaryButton.tsx";
import CardElement from "../../controls/card-element/CardElement.tsx";
import AddIcon from '../../../assets/elements/add.svg';
import TriTovarischaCover from "../../../assets/images/books/tri-tovarischa.jpg";
import VoinaIMirCover from '../../../assets/images/books/voina-i-mir.jpg';
import MasterIMargheritaCover from '../../../assets/images/books/master-i-margarita.jpg';
import PrestuplenieINakazanieCover from '../../../assets/images/books/prestuplenie-i-nakazanie.jpg';
import AnnaKareninaCover from '../../../assets/images/books/anna-karenina.jpeg';
import PortetDorianaGreyaCover from '../../../assets/images/books/portret-doriana-greya.jpg';
import VelikiyGetsbiCover from '../../../assets/images/books/velikiy-getsbi.jpg';
import MalenkiyPrintsCover from '../../../assets/images/books/malenkiy-prints.jpg';
import IdiotCover from '../../../assets/images/books/idiot.jpg';
import NadPropastyuVoRzhiCover from '../../../assets/images/books/nad-propastyu-vo-rzhi.jpg';
import Gauge from "../../gauge/Gauge.tsx";
import VerticalAccordion from "../../controls/vertical-accordion/VerticalAccordion.tsx";
import Calendar from "../../../assets/elements/calendar.svg";
import EmptyCalendar from "../../../assets/elements/empty-calendar.svg";
import type {RootState} from "../../../redux/store.ts";
import {useSelector} from "react-redux";
import {useState} from "react";
import GoalForm from "../../controls/goal-form/GoalForm.tsx";
import Modal from "../../controls/modal/Modal.tsx";
import BookListModal from "../../controls/book-list-modal/BookListModal.tsx";
import OrwellCover from "../../../assets/images/books/1984.jpg";
import GrafMonteCristoCover from "../../../assets/images/books/graf-monte-kristo.jpg";
import {useNavigate} from "react-router-dom";
import ReadingForm from "../../controls/reading-form/ReadingForm.tsx";

interface Book {
  id: string;
  title: string;
  author: string;
  rating: number;
  cover: string;
}

function Statistic() {
  const { user, isAuthenticated } = useSelector((state: RootState) => state.auth);
  const isNewUser = user?.username === 'newuser';
  
  const { dailyGoal, dailyRead, yearlyGoal, yearlyRead } = useSelector(
    (state: RootState) => state.goals
  );
  
  const hasDailyGoal = dailyGoal > 0;
  const hasYearlyGoal = yearlyGoal > 0;

  const [isChangeDailyGoalOpen, setIsChangeDailyGoalOpen] = useState(false);
  const [isChangeYearlyGoalOpen, setIsChangeYearlyGoalOpen] = useState(false);
  const [isAddReadingOpen, setIsAddReadingOpen] = useState(false);
  const [isReadingFormOpen, setIsReadingFormOpen] = useState(false);
  const [selectedBookForReading, setSelectedBookForReading] = useState<string>('');

  const isBookInList = (books: Book[], title: string, author: string): boolean => {
    return books.some((book: Book) => book.title === title && book.author === author);
  };

  const isToday = (date: Date): boolean => {
    const today = new Date();
    return date.toDateString() === today.toDateString();
  };

  const isThisMonth = (date: Date): boolean => {
    const now = new Date();
    return date.getMonth() === now.getMonth() && date.getFullYear() === now.getFullYear();
  };

  const isThisYear = (date: Date): boolean => {
    const now = new Date();
    return date.getFullYear() === now.getFullYear();
  };

  const handleBooksSelected = (books: { id: string; title: string; author: string; rating: number; imageUrl: string }[]) => {
    if (books.length > 0) {
      const book = books[0];
      setSelectedBookForReading(book.title);
      setIsAddReadingOpen(false);
      setIsReadingFormOpen(true);
      sessionStorage.setItem('selectedBookForReading', JSON.stringify(book));
    }
  };

  const handleReadingSubmit = (readingData: {
    bookId: string;
    title: string;
    author: string;
    rating: number;
    imageUrl: string;
    startDate: string;
    endDate: string;
    pages: number;
    isFinished: boolean;
  }) => {
    const endDate = new Date(readingData.endDate);
    const book = {
      id: readingData.bookId || `temp-${Date.now()}`,
      title: readingData.title,
      author: readingData.author,
      rating: readingData.rating || 5,
      cover: readingData.imageUrl || TriTovarischaCover
    };

    if (isToday(endDate)) {
      setReadingBooks((prev: Book[]) => {
        if (!isBookInList(prev, book.title, book.author)) {
          const updated = [...prev, book];
          sessionStorage.setItem('readingBooks', JSON.stringify(updated));
          return updated;
        }
        return prev;
      });
    }

    if (isThisMonth(endDate)) {
      setReadingBooks((prev: Book[]) => {
        if (!isBookInList(prev, book.title, book.author)) {
          const updated = [...prev, book];
          sessionStorage.setItem('readingBooks', JSON.stringify(updated));
          return updated;
        }
        return prev;
      });
    }

    if (readingData.isFinished && isThisYear(endDate)) {
      setYearlyBooks((prev: Book[]) => {
        if (!isBookInList(prev, book.title, book.author)) {
          const updated = [...prev, book];
          sessionStorage.setItem('yearlyBooks', JSON.stringify(updated));
          return updated;
        }
        return prev;
      });
    }

    setIsReadingFormOpen(false);
    sessionStorage.removeItem('selectedBookForReading');
  };

  const handleAddReading = (): void => {
    setIsAddReadingOpen(true);
  }

  const handleChangeDailyGoal = (): void => {
    setIsChangeDailyGoalOpen(true);
  }

  const handleChangeYearlyGoal = (): void => {
    setIsChangeYearlyGoalOpen(true);
  }

  const navigate = useNavigate();

  const getStoredReadingBooks = (): Book[] => {
    if (isNewUser) return [];
    const stored = sessionStorage.getItem('readingBooks');
    if (stored) {
      try {
        return JSON.parse(stored);
      } catch {
        return [];
      }
    }
    const defaultBooks: Book[] = [
      { id: '1', title: "Три товарища", author: "Эрих Мария Ремарк", rating: 4.5, cover: TriTovarischaCover },
      { id: '2', title: "Граф Монте-Кристо", author: "Александр Дюма", rating: 4.5, cover: GrafMonteCristoCover },
      { id: '3', title: "1984", author: "Джордж Оруэлл", rating: 4.5, cover: OrwellCover },
    ];
    sessionStorage.setItem('readingBooks', JSON.stringify(defaultBooks));
    return defaultBooks;
  };

  const getStoredYearlyBooks = (): Book[] => {
    if (isNewUser) return [];
    const stored = sessionStorage.getItem('yearlyBooks');
    if (stored) {
      try {
        return JSON.parse(stored);
      } catch {
        return [];
      }
    }
    const defaultBooks: Book[] = [
      { id: '4', title: "Идиот", author: "Фёдор Достоевский", rating: 4.7, cover: IdiotCover },
      { id: '5', title: "Над пропастью во ржи", author: "Джером Дэвид Сэлинджер", rating: 4.3, cover: NadPropastyuVoRzhiCover },
      { id: '1', title: "Три товарища", author: "Эрих Мария Ремарк", rating: 4.5, cover: TriTovarischaCover },
      { id: '6', title: "Мастер и Маргарита", author: "Михаил Булгаков", rating: 4.8, cover: MasterIMargheritaCover },
      { id: '3', title: "1984", author: "Джордж Оруэлл", rating: 4.6, cover: OrwellCover },
      { id: '7', title: "Преступление и наказание", author: "Фёдор Достоевский", rating: 4.7, cover: PrestuplenieINakazanieCover },
      { id: '8', title: "Война и мир", author: "Лев Толстой", rating: 4.9, cover: VoinaIMirCover },
      { id: '2', title: "Граф Монте-Кристо", author: "Александр Дюма", rating: 4.8, cover: GrafMonteCristoCover },
      { id: '9', title: "Анна Каренина", author: "Лев Толстой", rating: 4.6, cover: AnnaKareninaCover },
      { id: '10', title: "Портрет Дориана Грея", author: "Оскар Уайльд", rating: 4.5, cover: PortetDorianaGreyaCover },
      { id: '11', title: "Великий Гэтсби", author: "Фрэнсис Скотт Фицджеральд", rating: 4.4, cover: VelikiyGetsbiCover },
      { id: '12', title: "Маленький принц", author: "Антуан де Сент-Экзюпери", rating: 4.9, cover: MalenkiyPrintsCover },
    ];
    sessionStorage.setItem('yearlyBooks', JSON.stringify(defaultBooks));
    return defaultBooks;
  };

  const [readingBooks, setReadingBooks] = useState<Book[]>(getStoredReadingBooks());
  const [yearlyBooks, setYearlyBooks] = useState<Book[]>(getStoredYearlyBooks());

  const handleBookClick = (book: Book) => {
    navigate('/book', {
      state: {
        id: book.id,
        title: book.title,
        author: book.author,
        rating: book.rating,
        coverUrl: book.cover,
        isMine: isAuthenticated,
        isEditMode: false
      }
    });
  };

  const handleAddReadingForBook = (book: Book) => {
    setSelectedBookForReading(book.title);
    const bookData = {
      id: book.id,
      title: book.title,
      author: book.author,
      rating: book.rating,
      imageUrl: book.cover
    };
    sessionStorage.setItem('selectedBookForReading', JSON.stringify(bookData));
    setIsReadingFormOpen(true);
  };

  return (
    <>
      <main>
        <div className="top-container">
          <div className="container-title-with-button">
            <h2>Дневная статистика</h2>
            <PrimaryButton label={"Внести прочитанное"} onClick={handleAddReading}/>
          </div>
          {readingBooks.length > 2 ? (
            <VerticalAccordion
              header={
                <div className="statistics-container">
                  <div className="statistics-summary">
                    <span>Сегодня прочитано {dailyRead} страниц</span>
                    {dailyRead > 0 && <span>Книги, которые вы читали сегодня:</span>}
                  </div>

                  <div className="statistics-details">
                    <div className="reading-books-container">
                      {readingBooks.slice(0, 2).map((book: Book) => (
                        <CardElement
                          key={book.id}
                          title={book.title}
                          description={book.author}
                          starsCount={book.rating}
                          imageUrl={book.cover}
                          button={true}
                          buttonLabel={"Внести прочитанное"}
                          buttonIconUrl={AddIcon}
                          buttonChanged={false}
                          onClick={() => handleBookClick(book)}
                          onButtonClick={() => handleAddReadingForBook(book)}
                        />
                      ))}
                    </div>
                    <div className="statistics-graphs-container">
                      {hasDailyGoal ? (
                        <>
                          <Gauge value={Math.round((dailyRead / dailyGoal) * 100)} bgColor={"none"}/>
                          <span>{dailyRead} страниц из {dailyGoal}</span>
                          <PrimaryButton label={"Изменить дневную цель"} onClick={handleChangeDailyGoal}/>
                        </>
                      ) : (
                        <>
                          <Gauge value={0} bgColor={"none"}/>
                          <span>Цель не задана</span>
                          <PrimaryButton label={"Добавить дневную цель"} onClick={handleChangeDailyGoal}/>
                        </>
                      )}
                    </div>
                  </div>
                </div>
              }
              content={
                <div className="reading-books-container">
                  {readingBooks.slice(2).map((book: Book) => (
                    <CardElement
                      key={book.id}
                      title={book.title}
                      description={book.author}
                      starsCount={book.rating}
                      imageUrl={book.cover}
                      button={true}
                      buttonLabel={"Внести прочитанное"}
                      buttonIconUrl={AddIcon}
                      buttonChanged={false}
                      onClick={() => handleBookClick(book)}
                      onButtonClick={() => handleAddReadingForBook(book)}
                    />
                  ))}
                </div>
              }>
            </VerticalAccordion>
          ) : (
            <div className="statistics-container container">
              <div className="statistics-summary">
                <span>Сегодня прочитано {dailyRead} страниц</span>
                {dailyRead > 0 && <span>Книги, которые вы читали сегодня:</span>}
              </div>

              <div className="statistics-details">
                {readingBooks.length > 0 ? (
                  <div className="reading-books-container">
                    {readingBooks.map((book: Book) => (
                      <CardElement
                        key={book.id}
                        title={book.title}
                        description={book.author}
                        starsCount={book.rating}
                        imageUrl={book.cover}
                        button={true}
                        buttonLabel={"Внести прочитанное"}
                        buttonIconUrl={AddIcon}
                        buttonChanged={false}
                        onClick={() => handleBookClick(book)}
                        onButtonClick={() => handleAddReadingForBook(book)}
                      />
                    ))}
                  </div>
                ) : (
                  <p className="no-books-message">Вы еще не читали книги сегодня.</p>
                )}
                <div className="statistics-graphs-container">
                  {hasDailyGoal ? (
                    <>
                      <Gauge value={Math.round((dailyRead / dailyGoal) * 100)} bgColor={"none"}/>
                      <span>{dailyRead} страниц из {dailyGoal}</span>
                      <PrimaryButton label={"Изменить дневную цель"} onClick={handleChangeDailyGoal}/>
                    </>
                  ) : (
                    <>
                      <Gauge value={0} bgColor={"none"}/>
                      <span>Цель не задана</span>
                      <PrimaryButton label={"Добавить дневную цель"} onClick={handleChangeDailyGoal}/>
                    </>
                  )}
                </div>
              </div>
            </div>
          )}
        </div>
        <h2>Месячная статистика</h2>
        {readingBooks.length > 2 ? (
          <VerticalAccordion
            header={
              <div className="statistics-container">
                <div className="statistics-summary">
                  <span>В этом месяце вы прочитали {readingBooks.length} {readingBooks.length === 1 ? 'книгу' : readingBooks.length < 5 ? 'книги' : 'книг'}:</span>
                </div>

                <div className="statistics-details">
                  <div className="reading-books-container">
                    {readingBooks.slice(0, 2).map((book: Book) => (
                      <CardElement
                        key={book.id}
                        title={book.title}
                        description={book.author}
                        starsCount={book.rating}
                        imageUrl={book.cover}
                        button={true}
                        buttonLabel={"Внести прочитанное"}
                        buttonIconUrl={AddIcon}
                        buttonChanged={false}
                        onClick={() => handleBookClick(book)}
                        onButtonClick={() => handleAddReadingForBook(book)}
                      />
                    ))}
                  </div>
                  <div className="statistics-graphs-container">
                    <img className="calendar" src={Calendar} alt={"Календарь"} />
                  </div>
                </div>
              </div>
            }
            content={
              <div className="reading-books-container">
                {readingBooks.slice(2).map((book: Book) => (
                    <CardElement
                      key={book.id}
                      title={book.title}
                      description={book.author}
                      starsCount={book.rating}
                      imageUrl={book.cover}
                      button={true}
                      buttonLabel={"Внести прочитанное"}
                      buttonIconUrl={AddIcon}
                      buttonChanged={false}
                      onClick={() => handleBookClick(book)}
                      onButtonClick={() => handleAddReadingForBook(book)}
                    />
                  ))}
              </div>
            }>
          </VerticalAccordion>
        ) : (
          <div className="statistics-container container no-month">
            <div className="statistics-summary">
              <span>В этом месяце вы прочитали {readingBooks.length} {readingBooks.length === 1 ? 'книгу' : readingBooks.length > 1 && readingBooks.length < 5 ? 'книги' : 'книг'}:</span>
            </div>

            <div className="statistics-details">
              {readingBooks.length > 0 ? (
                <div className="reading-books-container">
                  {readingBooks.map((book: Book) => (
                      <CardElement
                        key={book.id}
                        title={book.title}
                        description={book.author}
                        starsCount={book.rating}
                        imageUrl={book.cover}
                        button={true}
                        buttonLabel={"Внести прочитанное"}
                        buttonIconUrl={AddIcon}
                        buttonChanged={false}
                        onClick={() => handleBookClick(book)}
                        onButtonClick={() => handleAddReadingForBook(book)}
                      />
                    ))}
                </div>
              ) : (
                <p className="no-books-message">Вы еще не прочитали книг в этом месяце.</p>
              )}
              <div className="statistics-graphs-container">
                <img className="calendar" src={readingBooks.length > 0 ? Calendar : EmptyCalendar} alt={"Календарь"} />
              </div>
            </div>
          </div>
        )}

        <h2>Годовая статистика</h2>
        <VerticalAccordion
          header={
            <div className="statistics-container">
              <div className="statistics-summary">
                <span>В этом году вы прочитали {yearlyBooks.length} {yearlyBooks.length === 1 ? 'книгу' : yearlyBooks.length > 1 && yearlyBooks.length < 5 ? 'книги' : 'книг'}:</span>
              </div>

              <div className="statistics-details">
                {yearlyBooks.length > 0 ? (
                  <div className="reading-books-container">
                    {yearlyBooks.slice(0, 2).map((book: Book) => (
                      <CardElement 
                        key={book.id}
                        title={book.title} 
                        description={book.author} 
                        starsCount={book.rating} 
                        imageUrl={book.cover}
                        button={true}
                        buttonLabel={"Внести прочитанное"}
                        buttonIconUrl={AddIcon}
                        buttonChanged={false}
                        onClick={() => handleBookClick(book)}
                        onButtonClick={() => handleAddReadingForBook(book)}
                      />
                    ))}
                  </div>
                ) : (
                  <p className="no-books-message">Вы еще не прочитали книг в этом году.</p>
                )}
                <div className="statistics-graphs-container">
                  {hasYearlyGoal ? (
                    <>
                      <Gauge value={Math.round((yearlyRead / yearlyGoal) * 100)} bgColor={"none"}/>
                      <span>{yearlyRead} книг из {yearlyGoal}</span>
                      <PrimaryButton label={"Изменить годовую цель"} onClick={handleChangeYearlyGoal}/>
                    </>
                  ) : (
                    <>
                      <Gauge value={0} bgColor={"none"}/>
                      <span>Цель не задана</span>
                      <PrimaryButton label={"Добавить дневную цель"} onClick={handleChangeYearlyGoal}/>
                    </>
                  )}
                </div>
              </div>
            </div>
          }
          content={
            yearlyBooks.length > 2 ? (
              <div className="yearly-reading-books">
                <div className="reading-books-container">
                  {yearlyBooks.slice(2, 7).map((book: Book) => (
                    <CardElement 
                      key={book.id}
                      title={book.title} 
                      description={book.author} 
                      starsCount={book.rating} 
                      imageUrl={book.cover}
                      button={true}
                      buttonLabel={"Внести прочитанное"}
                      buttonIconUrl={AddIcon}
                      buttonChanged={false}
                      onClick={() => handleBookClick(book)}
                      onButtonClick={() => handleAddReadingForBook(book)}
                    />
                  ))}
                </div>
                {yearlyBooks.length > 7 && (
                  <div className="reading-books-container">
                    {yearlyBooks.slice(7).map((book: Book) => (
                      <CardElement 
                        key={book.id}
                        title={book.title} 
                        description={book.author} 
                        starsCount={book.rating} 
                        imageUrl={book.cover}
                        button={true}
                        buttonLabel={"Внести прочитанное"}
                        buttonIconUrl={AddIcon}
                        buttonChanged={false}
                        onClick={() => handleBookClick(book)}
                        onButtonClick={() => handleAddReadingForBook(book)}
                      />
                    ))}
                  </div>
                )}
              </div>
            ) : null
          }>
        </VerticalAccordion>
      </main>

      <Modal open={isChangeDailyGoalOpen} onClose={() => setIsChangeDailyGoalOpen(false)}>
        <GoalForm type="daily" onSubmit={() => setIsChangeDailyGoalOpen(false)} />
      </Modal>

      <Modal open={isAddReadingOpen} onClose={() => setIsAddReadingOpen(false)}>
        <BookListModal
          onClose={() => setIsAddReadingOpen(false)}
          selectionMode={true}
          onBooksSelected={handleBooksSelected}
        />
      </Modal>

      <Modal open={isReadingFormOpen} onClose={() => {
        setIsReadingFormOpen(false);
        sessionStorage.removeItem('selectedBookForReading');
      }}>
        {(() => {
          const selectedBook = sessionStorage.getItem('selectedBookForReading');
          const bookData = selectedBook ? JSON.parse(selectedBook) : null;
          return (
            <ReadingForm
              bookTitle={selectedBookForReading}
              bookId={bookData?.id || ''}
              bookAuthor={bookData?.author || ''}
              bookRating={bookData?.rating || 5}
              bookImageUrl={bookData?.imageUrl || ''}
              onSubmit={handleReadingSubmit}
            />
          );
        })()}
      </Modal>

      <Modal open={isChangeYearlyGoalOpen} onClose={() => setIsChangeYearlyGoalOpen(false)}>
        <GoalForm type="yearly" onSubmit={() => setIsChangeYearlyGoalOpen(false)} />
      </Modal>
    </>
  );
}

export default Statistic;