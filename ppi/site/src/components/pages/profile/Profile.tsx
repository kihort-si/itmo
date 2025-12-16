import PrimaryButton from "../../controls/primary-button/PrimaryButton.tsx";
import CardElement from "../../controls/card-element/CardElement.tsx";
import AddIcon from "../../../assets/elements/add.svg";
import {useLocation, useNavigate} from "react-router-dom";
import VerticalAccordion from "../../controls/vertical-accordion/VerticalAccordion.tsx";
import Delete from "../../../assets/elements/delete.svg";
import {useState} from "react";
import type {RootState} from "../../../redux/store.ts";
import {useSelector} from "react-redux";
import Login from "../../controls/login/Login.tsx";
import Modal from "../../controls/modal/Modal.tsx";
import './Profile.scss';
import {russianLocalWordConverter} from "../../../utils/russianLocalWordConverter.ts";
import SecondaryButton from "../../controls/secondary-button/SecondaryButton.tsx";
import ClassicCover from "../../../assets/images/collections/classic.jpg";
import FantasticCover from "../../../assets/images/collections/fantastic.jpg";
import DetectiveCover from "../../../assets/images/collections/detective.jpg";
import PhilosophyCover from "../../../assets/images/collections/philosophy.jpg";
import RomanceCover from "../../../assets/images/collections/romance.avif";
import TriTovarischaCover from "../../../assets/images/books/tri-tovarischa.jpg";
import MasterIMargheritaCover from "../../../assets/images/books/master-i-margarita.jpg";
import OrwellCover from "../../../assets/images/books/1984.jpg";
import PrestuplenieINakazanieCover from "../../../assets/images/books/prestuplenie-i-nakazanie.jpg";
import VoinaIMirCover from "../../../assets/images/books/voina-i-mir.jpg";
import GrafMonteCristoCover from "../../../assets/images/books/graf-monte-kristo.jpg";
import Avatar1 from "../../../assets/images/users/avatar1.jpg";
import Avatar2 from "../../../assets/images/users/avatar2.jpg";
import Avatar3 from "../../../assets/images/users/avatar3.jpg";
import Avatar4 from "../../../assets/images/users/avatar4.jpg";
import Avatar5 from "../../../assets/images/users/avatar5.jpg";
import Avatar6 from "../../../assets/images/users/avatar6.jpg";
import Avatar12 from "../../../assets/images/users/avatar12.jpg";

function Profile() {
  const isAuthenticated = useSelector((state: RootState) => state.auth.isAuthenticated);
  const [showLoginModal, setShowLoginModal] = useState(false);

  const location = useLocation();
  const {username, subscribersCount, avatarUrl} = location.state || {
    username: 'user',
    subscribersCount: 0,
    avatarUrl: undefined
  };

  const isNewUser = username === 'newuser';

  const [isSubscribed, setIsSubscribed] = useState(false);
  const [currentSubscribersCount, setCurrentSubscribersCount] = useState(isNewUser ? 0 : subscribersCount);

  const handleSubscribeProfile = () => {
    setIsSubscribed(!isSubscribed);
    setCurrentSubscribersCount((prev: number) => isSubscribed ? prev - 1 : prev + 1);
  }

  const getFormattedSubscribersCount = (): string => {
    return currentSubscribersCount.toLocaleString('ru-RU').replace(/,/g, ' ');
  };

  const getSubscribersWord = (count: number): string => {
    return russianLocalWordConverter(count, 'подписчик', 'подписчика', 'подписчиков', 'подписчиков');
  };

  const collections = isNewUser ? [] : [
    {id: '1', title: "Классика", booksCount: 30, imageUrl: ClassicCover},
    {id: '2', title: "Фантастика", booksCount: 13, imageUrl: FantasticCover},
    {id: '3', title: "Детективы", booksCount: 7, imageUrl: DetectiveCover},
    {id: '4', title: "Философия", booksCount: 27, imageUrl: PhilosophyCover},
    {id: '5', title: "Романтика", booksCount: 24, imageUrl: RomanceCover},
  ];

  const displayedFollowers = isNewUser ? [] : [
    {id: 1, username: "ghost_67", avatar: Avatar1, followersCount: "12567"},
    {id: 2, username: "book_lover", avatar: Avatar2, followersCount: "8234"},
    {id: 3, username: "reader_pro", avatar: Avatar3, followersCount: "15890"},
    {id: 4, username: "lit_critic", avatar: Avatar4, followersCount: "6543"}
  ];

  const displayedSubscriptions = isNewUser ? [] : [
    {id: 9, username: "novel_ninja", avatar: Avatar4, followersCount: "10123"},
    {id: 10, username: "verse_master", avatar: Avatar5, followersCount: "14567"},
    {id: 11, username: "plot_hunter", avatar: Avatar6, followersCount: "8901"},
    {id: 12, username: "genre_guru", avatar: Avatar12, followersCount: "12345"}
  ];

  const handleFollowerClick = (followerUsername: string, followerAvatar: string, followerFollowersCount: string) => {
    navigate('/profile', {
      state: {
        username: followerUsername,
        subscribersCount: parseInt(followerFollowersCount),
        avatarUrl: followerAvatar
      }
    });
  };

  const handleSubscriberClick = () => {
    navigate('/followers', {
      state: {
        username,
        isMine: false
      }
    });
  };

  const handleSubscriptionsClick = () => {
    navigate('/subscriptions', {
      state: {
        username,
        isMine: false
      }
    });
  };

  const books = [
    {id: 1, title: "Три товарища", author: "Эрих Мария Ремарк", rating: 4.5, cover: TriTovarischaCover},
    {id: 2, title: "Мастер и Маргарита", author: "Михаил Булгаков", rating: 4.8, cover: MasterIMargheritaCover},
    {id: 3, title: "1984", author: "Джордж Оруэлл", rating: 4.6, cover: OrwellCover},
    {id: 4, title: "Преступление и наказание", author: "Фёдор Достоевский", rating: 4.7, cover: PrestuplenieINakazanieCover},
    {id: 5, title: "Война и мир", author: "Лев Толстой", rating: 4.9, cover: VoinaIMirCover},
    {id: 6, title: "Граф Монте-Кристо", author: "Александр Дюма", rating: 4.8, cover: GrafMonteCristoCover}
  ];

  const navigate = useNavigate();

  const handleCollectionClick = (collection: typeof collections[0]) => {
    navigate('/collection', {
      state: {
        id: collection.id,
        name: collection.title,
        isMine: false,
        coverUrl: collection.imageUrl,
        books: books.slice(0, 5).map(book => ({
          id: String(book.id),
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
      <div className="top-container">
        <div className="container-title-with-button">
          <h2>Профиль</h2>
          {isAuthenticated && (
            <div>
              {isSubscribed ? (
                <SecondaryButton label={"Отписаться"} onClick={handleSubscribeProfile}/>
              ) : (
                <PrimaryButton label={"Подписаться"} onClick={handleSubscribeProfile}/>
              )}
            </div>
          )}
        </div>

        <div className="profile-info container">
          <div className="profile-info-main">
            <div className="profile-info-panel">
              <span className="profile-info-name">{username}</span>
              <div className="profile-avatar-container">
                <img className="profile-avatar" alt="" src={avatarUrl}/>
              </div>
              <div className="profile-info-additional-container">
                <div className="profile-info-additional clickable" onClick={handleSubscriberClick}>
                  <span className="profile-info-label">{getFormattedSubscribersCount()} </span>
                  <span className="profile-info-sublabel">{getSubscribersWord(currentSubscribersCount)}</span>
                </div>
                <div className="profile-info-additional clickable" onClick={handleSubscriptionsClick}>
                  <span className="profile-info-label">{isNewUser ? '0' : '213'} </span>
                  <span className="profile-info-sublabel">подписок</span>
                </div>
                <div className="profile-info-additional">
                  <span className="profile-info-label">{isNewUser ? '0' : '5'} </span>
                  <span className="profile-info-sublabel">коллекций</span>
                </div>
              </div>
            </div>

            <div className="profile-info-collections">
              <span className="profile-collections-title">Коллекции</span>
              {collections.length > 0 ? (
                <VerticalAccordion header={
                  <div className="profile-collections-header">
                    {collections.slice(0, 4).map((collection) => (
                      <div key={collection.id}>
                        <CardElement
                          title={collection.title}
                          description={username}
                          infoDecoration={`${collection.booksCount} книг`}
                          imageUrl={collection.imageUrl}
                          button={true}
                          buttonLabel={"Добавить в избранное"}
                          onClick={() => handleCollectionClick(collection)}
                          buttonIconUrl={AddIcon}
                          buttonChanged={true}
                          buttonChangedIconUrl={Delete}
                          buttonChangedLabel={"Удалить из избранного"}
                          isAuthenticated={isAuthenticated}
                          onUnauthorized={() => setShowLoginModal(true)}
                        />
                      </div>
                    ))}
                  </div>
                }
                content={
                 <div>
                   {collections.slice(4).map((collection) => (
                     <div key={collection.id} onClick={() => handleCollectionClick(collection)}>
                       <CardElement
                         title={collection.title}
                         description={username}
                         infoDecoration={`${collection.booksCount} книг`}
                         imageUrl={collection.imageUrl}
                         button={true}
                         buttonLabel={"Добавить в избранное"}
                         buttonIconUrl={AddIcon}
                         buttonChanged={true}
                         buttonChangedIconUrl={Delete}
                         buttonChangedLabel={"Удалить из избранного"}
                         isAuthenticated={isAuthenticated}
                         onUnauthorized={() => setShowLoginModal(true)}
                       />
                     </div>
                   ))}
                 </div>
                }>
                </VerticalAccordion>
              ) : (
                <p className="no-books-message">У пользователя пока нет коллекций.</p>
              )}
            </div>
          </div>

          <div className="profile-info-followers">
            <div className="profile-section-header">
              <span className="profile-section-title">Подписчики</span>
              {!isNewUser && currentSubscribersCount > 0 && (
                <SecondaryButton
                  label="Перейти ко всем"
                  onClick={handleSubscriberClick}
                />
              )}
            </div>
            {displayedFollowers.length > 0 ? (
              <div className="profile-followers-list">
                {displayedFollowers.map((follower) => (
                  <CardElement
                    key={follower.id}
                    title={follower.username}
                    description={`${follower.followersCount} подписчиков`}
                    imageUrl={follower.avatar}
                    button={false}
                    onClick={() => handleFollowerClick(follower.username, follower.avatar, follower.followersCount)}
                  />
                ))}
              </div>
            ) : (
              <p className="no-items-message">Нет подписчиков</p>
            )}
          </div>

          <div className="profile-info-subscriptions">
            <div className="profile-section-header">
              <span className="profile-section-title">Подписки</span>
              {!isNewUser && displayedSubscriptions.length > 0 && (
                <SecondaryButton
                  label="Перейти ко всем"
                  onClick={handleSubscriptionsClick}
                />
              )}
            </div>
            {displayedSubscriptions.length > 0 ? (
              <div className="profile-subscriptions-list">
                {displayedSubscriptions.map((subscription) => (
                  <CardElement
                    key={subscription.id}
                    title={subscription.username}
                    description={`${subscription.followersCount} подписчиков`}
                    imageUrl={subscription.avatar}
                    button={false}
                    onClick={() => handleFollowerClick(subscription.username, subscription.avatar, subscription.followersCount)}
                  />
                ))}
              </div>
            ) : (
              <p className="no-items-message">Нет подписок</p>
            )}
          </div>
        </div>
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

export default Profile;