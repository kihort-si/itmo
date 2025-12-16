import './Subscriptions.scss';
import CardElement from "../../controls/card-element/CardElement.tsx";
import Delete from "../../../assets/elements/delete.svg";
import {useLocation, useNavigate} from "react-router-dom";
import {useState} from "react";
import {useSelector} from "react-redux";
import type {RootState} from "../../../redux/store.ts";
import Modal from "../../controls/modal/Modal.tsx";
import Login from "../../controls/login/Login.tsx";
import Avatar1 from "../../../assets/images/users/avatar1.jpg";
import Avatar2 from "../../../assets/images/users/avatar2.jpg";
import Avatar3 from "../../../assets/images/users/avatar3.jpg";
import Avatar4 from "../../../assets/images/users/avatar4.jpg";
import Avatar9 from "../../../assets/images/users/avatar9.jpg";
import Avatar10 from "../../../assets/images/users/avatar10.jpg";
import Avatar11 from "../../../assets/images/users/avatar11.jpg";
import Avatar12 from "../../../assets/images/users/avatar12.jpg";
import ConfirmDialog from "../../controls/confirm-dialog/ConfirmDialog.tsx";
import PrimaryButton from "../../controls/primary-button/PrimaryButton.tsx";
import AddIcon from "../../../assets/elements/add.svg";
import {russianLocalWordConverter} from "../../../utils/russianLocalWordConverter.ts";

interface Subscription {
  id: number;
  username: string;
  avatar: string;
  followersCount: string;
}

function Subscriptions() {
  const location = useLocation();
  const navigate = useNavigate();
  const isAuthenticated = useSelector((state: RootState) => state.auth.isAuthenticated);
  const [userFollowStates, setUserFollowStates] = useState<Record<number, boolean>>({});
  const [showLoginModal, setShowLoginModal] = useState(false);
  
  const { username, isMine } = location.state || {
    username: 'user',
    isMine: false
  };

  const isNewUser = username === 'newuser';

  const [subscriptionsList, setSubscriptionsList] = useState<Subscription[]>(
    isNewUser ? [] : [
      { id: 9, username: "novel_ninja", avatar: Avatar9, followersCount: "10123" },
      { id: 10, username: "verse_master", avatar: Avatar10, followersCount: "14567" },
      { id: 11, username: "plot_hunter", avatar: Avatar11, followersCount: "8901" },
      { id: 12, username: "genre_guru", avatar: Avatar12, followersCount: "12345" },
      { id: 1, username: "ghost_67", avatar: Avatar1, followersCount: "12567" },
      { id: 2, username: "book_lover", avatar: Avatar2, followersCount: "8234" },
      { id: 3, username: "reader_pro", avatar: Avatar3, followersCount: "15890" },
      { id: 4, username: "lit_critic", avatar: Avatar4, followersCount: "6543" },
    ]
  );

  const handleUserClick = (subscription: Subscription) => {
    navigate('/profile', {
      state: {
        username: subscription.username,
        subscribersCount: parseInt(subscription.followersCount),
        avatarUrl: subscription.avatar
      }
    });
  };

  const [showUnsubscribeModal, setShowUnsubscribeModal] = useState(false);
  const [selectedSubscriptionId, setSelectedSubscriptionId] = useState<number | null>(null);

  const handleUnsubscribe = (id: number) => {
    setSelectedSubscriptionId(id);
    setShowUnsubscribeModal(true);
  };

  const confirmUnsubscribe = () => {
    if (selectedSubscriptionId !== null) {
      setSubscriptionsList(prev => prev.filter(sub => sub.id !== selectedSubscriptionId));
    }
    setShowUnsubscribeModal(false);
    setSelectedSubscriptionId(null);
  };

  const handleUserFollow = (subscriptionId: number) => {
    setUserFollowStates(prev => ({
      ...prev,
      [subscriptionId]: !prev[subscriptionId]
    }));
  };

  const getFollowerCount = (userId: number, baseCount: string) => {
    const isFollowed = userFollowStates[userId];
    const numericCount = parseInt(baseCount.replace(/\s/g, ''));
    const newCount = isFollowed ? numericCount + 1 : numericCount;
    const formattedCount = newCount.toLocaleString('ru-RU').replace(/,/g, ' ');
    return `${formattedCount} ${russianLocalWordConverter(newCount, 'подписчик', 'подписчика', 'подписчиков', 'подписчиков')}`;
  };

  return (
    <main>
      <div className="top-container">
        <div className="container-title-with-button">
          <h2>{isMine ? 'Мои подписки' : `Подписки ${username}`}</h2>
          <PrimaryButton label={"Вернуться назад"} onClick={() => navigate(-1)} />
        </div>

        <div className="subscriptions-container container">
          {subscriptionsList.length > 0 ? (
            <div className="subscriptions-list">
              {subscriptionsList.map((subscription) => (
                isMine ? (
                  <CardElement
                    key={subscription.id}
                    title={subscription.username}
                    description={`${subscription.followersCount} подписчиков`}
                    imageUrl={subscription.avatar}
                    button={true}
                    buttonLabel={"Отписаться"}
                    buttonIconUrl={Delete}
                    onClick={() => handleUserClick(subscription)}
                    isAuthenticated={isAuthenticated}
                    onUnauthorized={() => setShowLoginModal(true)}
                    onButtonClick={() => handleUnsubscribe(subscription.id)}
                  />
                ) : (
                    <CardElement
                      key={subscription.id}
                      title={subscription.username}
                      description={getFollowerCount(subscription.id, subscription.followersCount)}
                      imageUrl={subscription.avatar}
                      button={true}
                      buttonLabel={"Подписаться"}
                      buttonIconUrl={AddIcon}
                      buttonChanged={true}
                      buttonChangedIconUrl={Delete}
                      buttonChangedLabel={"Отписаться"}
                      onClick={() => handleUserClick(subscription)}
                      onButtonClick={() => handleUserFollow(subscription.id)}
                      isAuthenticated={isAuthenticated}
                      onUnauthorized={() => setShowLoginModal(true)}
                    />
                  )
              ))}
            </div>
          ) : (
            <p className="no-subscriptions-message">
              {isMine ? 'У вас пока нет подписок.' : `У ${username} пока нет подписок.`}
            </p>
          )}
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

      <Modal open={showUnsubscribeModal} onClose={() => setShowUnsubscribeModal(false)}>
        <ConfirmDialog
          title="Подтверждение"
          message="Вы уверены, что отписаться от пользователя?"
          onConfirm={confirmUnsubscribe}
          onCancel={() => setShowUnsubscribeModal(false)}
        />
      </Modal>
    </main>
  );
}

export default Subscriptions;

