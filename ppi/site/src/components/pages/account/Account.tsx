import PrimaryButton from "../../controls/primary-button/PrimaryButton.tsx";
import UserAvatar from "../../../assets/images/users/user.png";
import CardElement from "../../controls/card-element/CardElement.tsx";
import AddIcon from "../../../assets/elements/add.svg";
import Delete from "../../../assets/elements/delete.svg";
import {useState} from "react";
import TriTovarischaCover from "../../../assets/images/books/tri-tovarischa.jpg";
import RussianLitCover from "../../../assets/images/collections/russian.webp";
import FantasticCover from "../../../assets/images/collections/fantastic.jpg";
import DetectiveCover from "../../../assets/images/collections/detective.jpg";
import VoinaIMirCover from "../../../assets/images/books/voina-i-mir.jpg";
import MasterIMargheritaCover from "../../../assets/images/books/master-i-margarita.jpg";
import OrwellCover from "../../../assets/images/books/1984.jpg";
import GrafMonteCristoCover from "../../../assets/images/books/graf-monte-kristo.jpg";
import ProfileForm from "../../controls/profile-form/ProfileForm.tsx";
import Modal from "../../controls/modal/Modal.tsx";
import {useSelector} from "react-redux";
import type {RootState} from "../../../redux/store.ts";
import {useNavigate} from "react-router-dom";
import SecondaryButton from "../../controls/secondary-button/SecondaryButton.tsx";
import Avatar1 from "../../../assets/images/users/avatar1.jpg";
import Avatar2 from "../../../assets/images/users/avatar2.jpg";
import Avatar3 from "../../../assets/images/users/avatar3.jpg";
import Avatar4 from "../../../assets/images/users/avatar4.jpg";
import Avatar5 from "../../../assets/images/users/avatar5.jpg";
import Avatar6 from "../../../assets/images/users/avatar6.jpg";
import './Account.scss';
import Avatar12 from "../../../assets/images/users/avatar12.jpg";

function Account() {
  const {user} = useSelector((state: RootState) => state.auth);
  const isNewUser = user?.username === 'newuser';
  const navigate = useNavigate();
  const userCollections = (isNewUser ? [] : [
    { id: '1', title: "Классика", creator: user?.username || "user", booksCount: 30, imageUrl: TriTovarischaCover },
    { id: '2', title: "Русская литература", creator: user?.username || "user", booksCount: 12, imageUrl: RussianLitCover },
    { id: '3', title: "Фантастика", creator: user?.username || "user", booksCount: 10, imageUrl: FantasticCover },
    { id: '4', title: "Детективы", creator: user?.username || "user", booksCount: 4, imageUrl: DetectiveCover },
  ]);

  const userBooks = [
    { id: '1', title: "Три товарища", author: "Эрих Мария Ремарк", rating: 4.5, cover: TriTovarischaCover },
    { id: '2', title: "Война и мир", author: "Лев Толстой", rating: 4.9, cover: VoinaIMirCover },
    { id: '3', title: "Мастер и Маргарита", author: "Михаил Булгаков", rating: 4.8, cover: MasterIMargheritaCover },
    { id: '4', title: "1984", author: "Джордж Оруэлл", rating: 4.6, cover: OrwellCover },
    { id: '5', title: "Граф Монте-Кристо", author: "Александр Дюма", rating: 4.8, cover: GrafMonteCristoCover },
  ];

  const displayedFollowers = isNewUser ? [] : [
    { id: 1, username: "ghost_67", avatar: Avatar1, followersCount: "12567" },
    { id: 2, username: "book_lover", avatar: Avatar2, followersCount: "8234" },
    { id: 3, username: "reader_pro", avatar: Avatar3, followersCount: "15890" },
    { id: 4, username: "lit_critic", avatar: Avatar4, followersCount: "6543" }
  ];

  const displayedSubscriptions = isNewUser ? [] : [
    { id: 9, username: "novel_ninja", avatar: Avatar4, followersCount: "10123" },
    { id: 10, username: "verse_master", avatar: Avatar5, followersCount: "14567" },
    { id: 11, username: "plot_hunter", avatar: Avatar6, followersCount: "8901" },
    { id: 12, username: "genre_guru", avatar: Avatar12, followersCount: "12345" }
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
        username: user?.username || 'user',
        isMine: true
      }
    });
  };

  const handleSubscriptionsClick = () => {
    navigate('/subscriptions', {
      state: {
        username: user?.username || 'user',
        isMine: true
      }
    });
  };

  const handleCollectionClick = (collection: typeof userCollections[0]) => {
    navigate('/collection', {
      state: {
        id: collection.id,
        name: collection.title,
        isMine: true,
        coverUrl: collection.imageUrl,
        books: userBooks.slice(0, 5).map(book => ({
          id: book.id,
          title: book.title,
          author: book.author,
          rating: book.rating,
          imageUrl: book.cover
        }))
      }
    });
  };

  const [isEditProfileOpen, setIsEditProfileOpen] = useState(false);

  const handleEditProfile = (): void => {
    setIsEditProfileOpen(true);
  }

  return(
    <>
      <main>
        <div className="top-container">
          <div className="container-title-with-button">
            <h2>Профиль</h2>
            <PrimaryButton label={"Редактировать"} onClick={handleEditProfile}/>
          </div>

          <div className="profile-info container">
            <div className="profile-info-main">
              <div className="profile-info-panel">
                <span className="profile-info-name">{user?.username}</span>
                <div className="profile-avatar-container">
                  <img className="profile-avatar" alt="" src={UserAvatar}/>
                </div>
                <div className="profile-info-additional-container">
                  <div className="profile-info-additional clickable" onClick={handleSubscriberClick}>
                    <span className="profile-info-label">{isNewUser ? '0' : '12 567'} </span>
                    <span className="profile-info-sublabel">подписчиков</span>
                  </div>
                  <div className="profile-info-additional clickable" onClick={handleSubscriptionsClick}>
                    <span className="profile-info-label">{isNewUser ? '0' : '213'} </span>
                    <span className="profile-info-sublabel">подписок</span>
                  </div>
                  <div className="profile-info-additional">
                    <span className="profile-info-label">{isNewUser ? '0' : '12'} </span>
                    <span className="profile-info-sublabel">коллекций</span>
                  </div>
                </div>
              </div>

              <div className="profile-info-collections">
                <span className="profile-collections-title">Коллекции</span>
                {userCollections.length > 0 ? (
                  <div className="profile-collections-list">
                    {userCollections.map((collection) => (
                      <div key={collection.id}>
                        <CardElement
                          title={collection.title}
                          description={user?.username || "user"}
                          infoDecoration={`${collection.booksCount} книг`}
                          imageUrl={collection.imageUrl}
                          button={true}
                          buttonLabel={"Добавить в избранное"}
                          buttonIconUrl={AddIcon}
                          onClick={() => handleCollectionClick(collection)}
                          buttonChanged={true}
                          buttonChangedIconUrl={Delete}
                          buttonChangedLabel={"Удалить из избранного"}
                        />
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="no-books-message">У вас пока нет созданных коллекций.</p>
                )}
              </div>
            </div>
            <div className="profile-info-followers">
              <div className="profile-section-header">
                <span className="profile-section-title">Подписчики</span>
                {!isNewUser && displayedFollowers.length > 0 && (
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
      </main>

    <Modal open={isEditProfileOpen} onClose={() => setIsEditProfileOpen(false)}>
      <ProfileForm onSubmit={() => setIsEditProfileOpen(false)} />
    </Modal>
  </>
  )
}

export default Account;