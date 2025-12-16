import Input from "../../controls/input/Input.tsx";
import searchIcon from '../../../assets/elements/search.svg';
import './Home.scss';
import CardElement from "../../controls/card-element/CardElement.tsx";
import AddIcon from '../../../assets/elements/add.svg';
import TriTovarischaCover from '../../../assets/images/books/tri-tovarischa.jpg';
import VoinaIMirCover from '../../../assets/images/books/voina-i-mir.jpg';
import OrwellCover from '../../../assets/images/books/1984.jpg';
import MasterIMargheritaCover from '../../../assets/images/books/master-i-margarita.jpg';
import PrestuplenieINakazanieCover from '../../../assets/images/books/prestuplenie-i-nakazanie.jpg';
import GrafMonteCristoCover from '../../../assets/images/books/graf-monte-kristo.jpg';
import AnnaKareninaCover from '../../../assets/images/books/anna-karenina.jpeg';
import PortetDorianaGreyaCover from '../../../assets/images/books/portret-doriana-greya.jpg';
import VelikiyGetsbiCover from '../../../assets/images/books/velikiy-getsbi.jpg';
import MalenkiyPrintsCover from '../../../assets/images/books/malenkiy-prints.jpg';
import IdiotCover from '../../../assets/images/books/idiot.jpg';
import NadPropastyuVoRzhiCover from '../../../assets/images/books/nad-propastyu-vo-rzhi.jpg';
import BiograohyCover from '../../../assets/images/collections/biography.webp';
import ClassicCover from '../../../assets/images/collections/classic.jpg';
import DetectiveCover from '../../../assets/images/collections/detective.jpg';
import FantasyCover from '../../../assets/images/collections/fantasy.png';
import HistoryCover from '../../../assets/images/collections/history.avif';
import PhilosophyCover from '../../../assets/images/collections/philosophy.jpg';
import PsychologyCover from '../../../assets/images/collections/psychology.jpeg';
import ModernProseCover from '../../../assets/images/collections/modern.jpeg';
import RussianLitCover from '../../../assets/images/collections/russian.webp';
import FantasticCover from '../../../assets/images/collections/fantastic.jpg';
import RomanceCover from '../../../assets/images/collections/romance.avif';
import ForeignCover from '../../../assets/images/collections/foreign.jpg';
import Avatar1 from '../../../assets/images/users/avatar1.jpg';
import Avatar2 from '../../../assets/images/users/avatar2.jpg';
import Avatar3 from '../../../assets/images/users/avatar3.jpg';
import Avatar4 from '../../../assets/images/users/avatar4.jpg';
import Avatar5 from '../../../assets/images/users/avatar5.jpg';
import Avatar6 from '../../../assets/images/users/avatar6.jpg';
import Avatar7 from '../../../assets/images/users/avatar7.jpg';
import Avatar8 from '../../../assets/images/users/avatar8.jpg';
import Avatar9 from '../../../assets/images/users/avatar9.jpg';
import Avatar10 from '../../../assets/images/users/avatar10.jpg';
import Avatar11 from '../../../assets/images/users/avatar11.jpg';
import Avatar12 from '../../../assets/images/users/avatar12.jpg';
import HorizontalSlider from "../../controls/horizontal-slider/HorizontalSlider.tsx";
import SearchFilters from "../../controls/search-filters/SearchFilters.tsx";
import {useState} from "react";
import Stars from "../../stars/Stars.tsx";
import Delete from '../../../assets/elements/delete.svg';
import {russianLocalWordConverter} from "../../../utils/russianLocalWordConverter.ts";
import Login from "../../controls/login/Login.tsx";
import Modal from "../../controls/modal/Modal.tsx";
import type {RootState} from "../../../redux/store.ts";
import {useSelector} from "react-redux";
import {useNavigate} from "react-router-dom";

function Home() {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedTypes, setSelectedTypes] = useState<string[]>(['books', 'users', 'collections']);
  const [selectedGenre, setSelectedGenre] = useState('Все жанры');
  const [isBookInWishlist, setIsBookInWishlist] = useState(false);
  const [isCollectionFavorited, setIsCollectionFavorited] = useState(false);
  const [userFollowStates, setUserFollowStates] = useState<Record<number, boolean>>({});
  const isAuthenticated = useSelector((state: RootState) => state.auth.isAuthenticated);
  const [showLoginModal, setShowLoginModal] = useState(false);
  const navigate = useNavigate();

  const handleTypeChange = (type: string) => {
    setSelectedTypes(prev =>
      prev.includes(type)
        ? prev.filter(t => t !== type)
        : [...prev, type]
    );
  };

  const handleResetFilters = () => {
    setSelectedTypes(['books', 'users', 'collections']);
    setSelectedGenre('Все жанры');
  };

  const topBooks = [
    { id: 1, title: "Три товарища", author: "Эрих Мария Ремарк", rating: 4.5, cover: TriTovarischaCover },
    { id: 2, title: "Мастер и Маргарита", author: "Михаил Булгаков", rating: 4.8, cover: MasterIMargheritaCover },
    { id: 3, title: "1984", author: "Джордж Оруэлл", rating: 4.6, cover: OrwellCover },
    { id: 4, title: "Преступление и наказание", author: "Фёдор Достоевский", rating: 4.7, cover: PrestuplenieINakazanieCover },
    { id: 5, title: "Война и мир", author: "Лев Толстой", rating: 4.9, cover: VoinaIMirCover },
    { id: 6, title: "Граф Монте-Кристо", author: "Александр Дюма", rating: 4.8, cover: GrafMonteCristoCover },
    { id: 7, title: "Анна Каренина", author: "Лев Толстой", rating: 4.6, cover: AnnaKareninaCover },
    { id: 8, title: "Портрет Дориана Грея", author: "Оскар Уайльд", rating: 4.5, cover: PortetDorianaGreyaCover },
    { id: 9, title: "Великий Гэтсби", author: "Фрэнсис Скотт Фицджеральд", rating: 4.4, cover: VelikiyGetsbiCover },
    { id: 10, title: "Маленький принц", author: "Антуан де Сент-Экзюпери", rating: 4.9, cover: MalenkiyPrintsCover },
    { id: 11, title: "Идиот", author: "Фёдор Достоевский", rating: 4.7, cover: IdiotCover },
    { id: 12, title: "Над пропастью во ржи", author: "Джером Дэвид Сэлинджер", rating: 4.3, cover: NadPropastyuVoRzhiCover }
  ];

  const topCollections = [
    { id: 1, title: "Классика", creator: "ghost_67", booksCount: "30 книг", cover: ClassicCover },
    { id: 2, title: "Русская литература", creator: "book_lover", booksCount: "45 книг", cover: RussianLitCover },
    { id: 3, title: "Фантастика", creator: "sci_fi_fan", booksCount: "28 книг", cover: FantasticCover },
    { id: 4, title: "Детективы", creator: "mystery_reader", booksCount: "35 книг", cover: DetectiveCover },
    { id: 5, title: "Философия", creator: "thinker_99", booksCount: "22 книги", cover: PhilosophyCover },
    { id: 6, title: "Современная проза", creator: "modern_lit", booksCount: "40 книг", cover: ModernProseCover },
    { id: 7, title: "Зарубежная классика", creator: "world_books", booksCount: "50 книг", cover: ForeignCover },
    { id: 8, title: "Фэнтези", creator: "dragon_slayer", booksCount: "33 книги", cover: FantasyCover },
    { id: 9, title: "Романтика", creator: "heart_reader", booksCount: "38 книг", cover: RomanceCover },
    { id: 10, title: "Биографии", creator: "life_stories", booksCount: "25 книг", cover: BiograohyCover },
    { id: 11, title: "Психология", creator: "mind_master", booksCount: "27 книг", cover: PsychologyCover },
    { id: 12, title: "История", creator: "historian_", booksCount: "42 книги", cover: HistoryCover }
  ];

  const topUsers = [
    { id: 1, username: "ghost_67", followers: "12567", avatar: Avatar1 },
    { id: 2, username: "book_lover", followers: "8234", avatar: Avatar2 },
    { id: 3, username: "reader_pro", followers: "15890", avatar: Avatar3 },
    { id: 4, username: "lit_critic", followers: "6543", avatar: Avatar4 },
    { id: 5, username: "page_turner", followers: "11234", avatar: Avatar5 },
    { id: 6, username: "bibliophile", followers: "9876", avatar: Avatar6 },
    { id: 7, username: "story_seeker", followers: "13456", avatar: Avatar7 },
    { id: 8, username: "word_wizard", followers: "7890", avatar: Avatar8 },
    { id: 9, username: "novel_ninja", followers: "10123", avatar: Avatar9 },
    { id: 10, username: "verse_master", followers: "14567", avatar: Avatar10 },
    { id: 11, username: "plot_hunter", followers: "8901", avatar: Avatar11 },
    { id: 12, username: "genre_guru", followers: "12345", avatar: Avatar12 }
  ];

  const filterSearchResults = () => {
    const query = searchQuery.toLowerCase().trim();
    if (!query) return { books: [], users: [], collections: [] };

    const filteredBooks = topBooks.filter(book => 
      book.title.toLowerCase().includes(query) || 
      book.author.toLowerCase().includes(query)
    );

    const filteredUsers = topUsers.filter(user => 
      user.username.toLowerCase().includes(query)
    );

    const filteredCollections = topCollections.filter(collection => 
      collection.title.toLowerCase().includes(query) || 
      collection.creator.toLowerCase().includes(query)
    );

    return {
      books: filteredBooks,
      users: filteredUsers,
      collections: filteredCollections
    };
  };

  const searchResults = filterSearchResults();
  const totalResultsCount = searchResults.books.length + searchResults.users.length + searchResults.collections.length;

  const handleUserFollow = (userId: number) => {
    setUserFollowStates(prev => ({
      ...prev,
      [userId]: !prev[userId]
    }));
  };

  const getFollowerCount = (userId: number, baseCount: string) => {
    const isFollowed = userFollowStates[userId];
    const numericCount = parseInt(baseCount.replace(/\s/g, ''));
    const newCount = isFollowed ? numericCount + 1 : numericCount;
    const formattedCount = newCount.toLocaleString('ru-RU').replace(/,/g, ' ');
    return `${formattedCount} ${russianLocalWordConverter(newCount, 'подписчик', 'подписчика', 'подписчиков', 'подписчиков')}`;
  };

  const handleAuthenticatedAction = (action: () => void) => {
    if (!isAuthenticated) {
      setShowLoginModal(true);
      return;
    }
    action();
  };

  const handleBookClick = (book: typeof topBooks[0]) => {
    navigate('/book', {
      state: {
        title: book.title,
        author: book.author,
        coverUrl: book.cover,
        genre: 'Детектив',
        year: '1939',
        rating: book.rating
      }
    });
  };

  const handleCollectionClick = (collection: typeof topCollections[0]) => () => {
    navigate('/collection', {
      state: {
        id: collection.title.toLowerCase().replace(/\s/g, '-'),
        name: collection.title,
        isMine: false,
        coverUrl: collection.cover,
        books: topBooks.slice(0, 5).map(book => ({
          id: book.title.toLowerCase().replace(/\s/g, '-'),
          title: book.title,
          author: book.author,
          rating: book.rating,
          imageUrl: book.cover
        }))
      }
    });
  }

  const handleUserClick = (user: typeof topUsers[0]) => {
    navigate('/profile', {
      state: {
        username: user.username,
        subscribersCount: parseInt(user.followers.replace(/\s/g, '')),
        avatarUrl: user.avatar
      }
    });
  };

  const handleSearchBookClick = (book: typeof topBooks[0]) => {
    navigate('/book', {
      state: {
        id: book.id.toString(),
        title: book.title,
        author: book.author,
        coverUrl: book.cover,
        rating: book.rating,
        isMine: false,
        isEditMode: false
      }
    });
  };

  const handleSearchUserClick = (user: typeof topUsers[0]) => {
    navigate('/profile', {
      state: {
        username: user.username,
        subscribersCount: parseInt(user.followers.replace(/\s/g, '')),
        avatarUrl: user.avatar
      }
    });
  };

  const handleSearchCollectionClick = (collection: typeof topCollections[0]) => {
    navigate('/collection', {
      state: {
        id: collection.id.toString(),
        name: collection.title,
        isMine: false,
        coverUrl: collection.cover,
        books: topBooks.slice(0, 5).map(book => ({
          id: book.id.toString(),
          title: book.title,
          author: book.author,
          rating: book.rating,
          imageUrl: book.cover
        }))
      }
    });
  };

  return (
    <main>
      <div className="search-container">
        <h2>Поиск</h2>
        <Input placeholder="Название книги, автор, коллекция, пользователь..." picture={searchIcon} value={searchQuery} onChange={setSearchQuery} />

        {searchQuery && (
          <>
            <SearchFilters
              selectedTypes={selectedTypes}
              onTypeChange={handleTypeChange}
              selectedGenre={selectedGenre}
              onGenreChange={setSelectedGenre}
              onReset={handleResetFilters}
            />

            {selectedTypes.length > 0 && (
              <div className="search-results container">
                <div className="search-results-content">
                  <div className="results-count">
                    Найдено результатов: {totalResultsCount}
                  </div>

                  {selectedTypes.includes('books') && searchResults.books.length > 0 && (
                    <>
                      {searchResults.books.map((book) => (
                        <div key={book.id} className="search-result-row">
                          <div className="search-result-info" onClick={() => handleSearchBookClick(book)} style={{ cursor: 'pointer' }}>
                            <img src={book.cover} alt="Book cover"/>
                            <div>
                              <p className="search-result-title">{book.title}</p>
                              <p className="search-result-author">{book.author}</p>
                            </div>
                          </div>
                          <div className="search-result-actions">
                            <Stars count={Math.round(book.rating)}/>
                            <button
                              className={isBookInWishlist ? 'active' : ''}
                              onClick={(e) => {
                                e.stopPropagation();
                                handleAuthenticatedAction(() => setIsBookInWishlist(!isBookInWishlist));
                              }}
                            >
                              <img src={isBookInWishlist ? Delete : AddIcon} alt=""/>
                              <span>{isBookInWishlist ? 'Удалить из вишлиста' : 'Добавить в вишлист'}</span>
                            </button>
                          </div>
                        </div>
                      ))}
                    </>
                  )}

                  {selectedTypes.includes('users') && searchResults.users.length > 0 && (
                    <>
                      {searchResults.users.map((user) => {
                        const isFollowed = userFollowStates[user.id] || false;
                        const followerCount = isFollowed 
                          ? parseInt(user.followers.replace(/\s/g, '')) + 1 
                          : parseInt(user.followers.replace(/\s/g, ''));
                        const formattedCount = followerCount.toLocaleString('ru-RU').replace(/,/g, ' ');
                        
                        return (
                          <div key={user.id} className="search-result-row">
                            <div className="search-result-info" onClick={() => handleSearchUserClick(user)} style={{ cursor: 'pointer' }}>
                              <img src={user.avatar} alt="User avatar"/>
                              <div>
                                <p className="search-result-title">{user.username}</p>
                                <p className="search-result-author">{formattedCount} подписчиков</p>
                              </div>
                            </div>
                            <div className="search-result-actions">
                              <button
                                className={isFollowed ? 'active' : ''}
                                onClick={(e) => {
                                  e.stopPropagation();
                                  handleAuthenticatedAction(() => handleUserFollow(user.id));
                                }}
                              >
                                <img src={isFollowed ? Delete : AddIcon} alt=""/>
                                <span>{isFollowed ? 'Отписаться' : 'Подписаться'}</span>
                              </button>
                            </div>
                          </div>
                        );
                      })}
                    </>
                  )}

                  {selectedTypes.includes('collections') && searchResults.collections.length > 0 && (
                    <>
                      {searchResults.collections.map((collection) => (
                        <div key={collection.id} className="search-result-row">
                          <div className="search-result-info" onClick={() => handleSearchCollectionClick(collection)} style={{ cursor: 'pointer' }}>
                            <img src={collection.cover} alt="Collection cover"/>
                            <div>
                              <p className="search-result-title">{collection.title}</p>
                              <p className="search-result-author">{collection.creator}</p>
                            </div>
                          </div>
                          <div className="search-result-actions">
                            {collection.booksCount}
                            <button
                              className={isCollectionFavorited ? 'active' : ''}
                              onClick={(e) => {
                                e.stopPropagation();
                                handleAuthenticatedAction(() => setIsCollectionFavorited(!isCollectionFavorited));
                              }}
                            >
                              <img src={isCollectionFavorited ? Delete : AddIcon} alt=""/>
                              <span>{isCollectionFavorited ? 'Удалить из избранного' : 'Добавить в избранное'}</span>
                            </button>
                          </div>
                        </div>
                      ))}
                    </>
                  )}

                  {totalResultsCount === 0 && (
                    <div className="no-results-message">
                      По вашему запросу ничего не найдено
                    </div>
                  )}
                </div>
              </div>
            )}
          </>
        )}
      </div>
      <div className="top-container">
        <h2>Топ-12 книг за неделю</h2>
        <HorizontalSlider>
          {topBooks.map((book) => (
            <CardElement
              key={book.id}
              title={book.title}
              description={book.author}
              starsCount={book.rating}
              imageUrl={book.cover}
              button={true}
              buttonLabel={"Добавить в вишлист"}
              onClick={() => handleBookClick(book)}
              buttonIconUrl={AddIcon}
              buttonChanged={true}
              buttonChangedIconUrl={Delete}
              buttonChangedLabel={"Удалить из вишлиста"}
              isAuthenticated={isAuthenticated}
              onUnauthorized={() => setShowLoginModal(true)}
            />
          ))}
        </HorizontalSlider>
      </div>
      <div className="top-container">
        <h2>Топ-12 коллекций за неделю</h2>
        <HorizontalSlider>
          {topCollections.map((collection) => (
            <CardElement
              key={collection.id}
              title={collection.title}
              description={collection.creator}
              infoDecoration={collection.booksCount}
              imageUrl={collection.cover}
              button={true}
              onClick={handleCollectionClick(collection)}
              buttonLabel={"Добавить в избранное"}
              buttonIconUrl={AddIcon}
              buttonChanged={true}
              buttonChangedIconUrl={Delete}
              buttonChangedLabel={"Удалить из избранного"}
              isAuthenticated={isAuthenticated}
              onUnauthorized={() => setShowLoginModal(true)}
            />
          ))}
        </HorizontalSlider>
      </div>
      <div className="top-container">
        <h2>Топ-12 пользователей за неделю</h2>
        <HorizontalSlider>
          {topUsers.map((user) => (
            <CardElement
              key={user.id}
              title={user.username}
              description={getFollowerCount(user.id, user.followers)}
              imageUrl={user.avatar}
              button={true}
              buttonLabel={"Подписаться"}
              onClick={() => handleUserClick(user)}
              buttonIconUrl={AddIcon}
              buttonChanged={true}
              buttonChangedIconUrl={Delete}
              buttonChangedLabel={"Отписаться"}
              onButtonClick={() => handleUserFollow(user.id)}
              isButtonActive={userFollowStates[user.id] || false}
              isAuthenticated={isAuthenticated}
              onUnauthorized={() => setShowLoginModal(true)}
            />
          ))}
        </HorizontalSlider>
      </div>

      <Modal
        open={showLoginModal}
        onClose={() => setShowLoginModal(false)}
      >
        <Login
          type="login"
          onSubmit={() => setShowLoginModal(false)}
        />
      </Modal>
    </main>
  );
}

export default Home;