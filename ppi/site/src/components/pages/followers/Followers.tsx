import './Followers.scss';
import CardElement from "../../controls/card-element/CardElement.tsx";
import AddIcon from "../../../assets/elements/add.svg";
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
import Avatar5 from "../../../assets/images/users/avatar5.jpg";
import Avatar6 from "../../../assets/images/users/avatar6.jpg";
import Avatar7 from "../../../assets/images/users/avatar7.jpg";
import Avatar8 from "../../../assets/images/users/avatar8.jpg";
import {russianLocalWordConverter} from "../../../utils/russianLocalWordConverter.ts";
import PrimaryButton from "../../controls/primary-button/PrimaryButton.tsx";

interface Follower {
  id: number;
  username: string;
  avatar: string;
  followersCount: string;
}

function Followers() {
  const location = useLocation();
  const navigate = useNavigate();
  const isAuthenticated = useSelector((state: RootState) => state.auth.isAuthenticated);
  const [showLoginModal, setShowLoginModal] = useState(false);
  const [userFollowStates, setUserFollowStates] = useState<Record<number, boolean>>({});
  
  const { username, isMine } = location.state || {
    username: 'user',
    isMine: false
  };

  const isNewUser = username === 'newuser';

  const followers: Follower[] = isNewUser ? [] : [
    { id: 1, username: "ghost_67", avatar: Avatar1, followersCount: "12567" },
    { id: 2, username: "book_lover", avatar: Avatar2, followersCount: "8234" },
    { id: 3, username: "reader_pro", avatar: Avatar3, followersCount: "15890" },
    { id: 4, username: "lit_critic", avatar: Avatar4, followersCount: "6543" },
    { id: 5, username: "page_turner", avatar: Avatar5, followersCount: "11234" },
    { id: 6, username: "bibliophile", avatar: Avatar6, followersCount: "9876" },
    { id: 7, username: "story_seeker", avatar: Avatar7, followersCount: "13456" },
    { id: 8, username: "word_wizard", avatar: Avatar8, followersCount: "7890" },
  ];

  const handleUserClick = (follower: Follower) => {
    navigate('/profile', {
      state: {
        username: follower.username,
        subscribersCount: parseInt(follower.followersCount),
        avatarUrl: follower.avatar
      }
    });
  };

  const handleUserFollow = (followerId: number) => {
    setUserFollowStates(prev => ({
      ...prev,
      [followerId]: !prev[followerId]
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
          <h2>{isMine ? 'Мои подписчики' : `Подписчики ${username}`}</h2>
          <PrimaryButton label={"Вернуться назад"} onClick={() => navigate(-1)} />
        </div>

        <div className="followers-container container">
          {followers.length > 0 ? (
            <div className="followers-list">
              {followers.map((follower) => (
                <CardElement
                  key={follower.id}
                  title={follower.username}
                  description={getFollowerCount(follower.id, follower.followersCount)}
                  imageUrl={follower.avatar}
                  button={true}
                  buttonLabel={"Подписаться"}
                  buttonIconUrl={AddIcon}
                  buttonChanged={true}
                  buttonChangedIconUrl={Delete}
                  buttonChangedLabel={"Отписаться"}
                  onClick={() => handleUserClick(follower)}
                  onButtonClick={() => handleUserFollow(follower.id)}
                  isAuthenticated={isAuthenticated}
                  onUnauthorized={() => setShowLoginModal(true)}
                />
              ))}
            </div>
          ) : (
            <p className="no-followers-message">
              {isMine ? 'У вас пока нет подписчиков.' : `У ${username} пока нет подписчиков.`}
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
    </main>
  );
}

export default Followers;

