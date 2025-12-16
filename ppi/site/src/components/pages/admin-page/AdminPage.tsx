import AdminListElement from '../../controls/admin-list-element/AdminListElement';
import Sidebar from '../../controls/sidebar/Sidebar';
import Modal from '../../controls/modal/Modal';
import ConfirmDialog from '../../controls/confirm-dialog/ConfirmDialog';
import './AdminPage.scss';
import {useState} from "react";
import UserAvatar from '../../../assets/images/users/user.png';

interface Complaint {
  id: number;
  reason: string;
  date: string;
  user: string;
  isClosed: boolean;
  avatar: string;
  details?: string;
  targetContent?: string;
}

function AdminPage() {
  const [activeItem, setActiveItem] = useState('complaints');
  const [data, setData] = useState({
    complaints: [
      {
        id: 1,
        reason: 'Спам',
        date: '2024-06-01',
        user: 'user123',
        isClosed: false,
        avatar: UserAvatar,
        details: 'Пользователь разместил рекламные сообщения в комментариях к книге "Война и мир".',
        targetContent: 'Комментарии к книге "Война и мир"',
      },
      {
        id: 2,
        reason: 'Непристойный контент',
        date: '2024-06-02',
        user: 'user456',
        isClosed: true,
        avatar: UserAvatar,
        details: 'В отзыве использованы оскорбительные выражения и нецензурная лексика.',
        targetContent: 'Отзыв к книге "Преступление и наказание"',
      },
    ],
    blockedUsers: [
      {
        id: 1,
        user: 'spammer',
        reason: 'Спам',
        date: '2024-05-10',
        avatar: UserAvatar,
      },
    ],
    hiddenContent: [
      {
        id: 1,
        content: 'Оскорбление других пользователей в комментариях',
        user: 'bad_user',
        date: '2024-05-15',
        avatar: UserAvatar,
        fullContent: 'Полный текст скрытого контента. Этот контент был скрыт из-за нарушения правил сообщества.',
      },
    ],
    statistics: [],
  });

  const [showComplaintModal, setShowComplaintModal] = useState(false);
  const [selectedComplaint, setSelectedComplaint] = useState<Complaint | null>(null);
  const [showUnblockDialog, setShowUnblockDialog] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null);
  const [showUnhideDialog, setShowUnhideDialog] = useState(false);
  const [selectedContentId, setSelectedContentId] = useState<number | null>(null);

  const toggleComplaintStatus = (id: string, checked: boolean) => {
    setData(prevData => ({
      ...prevData,
      complaints: prevData.complaints.map(complaint =>
        complaint.id.toString() === id ? { ...complaint, isClosed: checked } : complaint
      ),
    }));
  };

  const handleViewComplaint = (complaint: Complaint) => {
    setSelectedComplaint(complaint);
    setShowComplaintModal(true);
  };

  const handleUnblockUser = (userId: number) => {
    setSelectedUserId(userId);
    setShowUnblockDialog(true);
  };

  const confirmUnblockUser = () => {
    if (selectedUserId !== null) {
      setData(prevData => ({
        ...prevData,
        blockedUsers: prevData.blockedUsers.filter(user => user.id !== selectedUserId)
      }));
      setShowUnblockDialog(false);
      setSelectedUserId(null);
    }
  };

  const handleUnhideContent = (contentId: number) => {
    setSelectedContentId(contentId);
    setShowUnhideDialog(true);
  };

  const confirmUnhideContent = () => {
    if (selectedContentId !== null) {
      setData(prevData => ({
        ...prevData,
        hiddenContent: prevData.hiddenContent.filter(content => content.id !== selectedContentId)
      }));
      setShowUnhideDialog(false);
      setSelectedContentId(null);
    }
  };

  const renderContent = () => {
    switch (activeItem) {
      case 'complaints':
        return (
          <div className="list-container">
            {data.complaints.length === 0 ? (
              <p>Нет жалоб</p>
            ) : (
              data.complaints.map((complaint) => (
                <AdminListElement
                  key={complaint.id}
                  type="complaints"
                  item={complaint}
                  onAction={() => handleViewComplaint(complaint)}
                  onToggleStatus={toggleComplaintStatus}
                />
              ))
            )}
          </div>
        );

      case 'blocked-users':
        return (
          <div className="list-container">
            {data.blockedUsers.length === 0 ? (
              <p>Нет заблокированных пользователей</p>
            ) : (
              data.blockedUsers.map((user) => (
                <AdminListElement
                  key={user.id}
                  type="blocked-users"
                  item={user}
                  onAction={() => handleUnblockUser(user.id as number)}
                />
              ))
            )}
          </div>
        );

      case 'hidden':
        return (
          <div className="list-container">
            {data.hiddenContent.length === 0 ? (
              <p>Нет скрытого контента</p>
            ) : (
              data.hiddenContent.map((content) => (
                <AdminListElement
                  key={content.id}
                  type="hidden"
                  item={content}
                  onAction={() => handleUnhideContent(content.id as number)}
                />
              ))
            )}
          </div>
        );

      case 'statistics':
        return (
          <div className="statistics-info">
            <div className="statistics-item">
              <span className="statistics-label">Зарегистрированных пользователей:</span>
              <span className="statistics-value">2 334</span>
            </div>
            <div className="statistics-item">
              <span className="statistics-label">Добавленных книг:</span>
              <span className="statistics-value">5 678</span>
            </div>
            <div className="statistics-item">
              <span className="statistics-label">Созданных коллекций:</span>
              <span className="statistics-value">1 234</span>
            </div>
            <div className="statistics-item">
              <span className="statistics-label">Активных пользователей за месяц:</span>
              <span className="statistics-value">1 123</span>
            </div>
            <div className="statistics-item">
              <span className="statistics-label">Количество жалоб:</span>
              <span className="statistics-value">{data.complaints.length}</span>
            </div>
          </div>
        );

      default:
        return null;
    }
  };

  return (
    <main>
      <div className="admin-page-container">
        <Sidebar activeItem={activeItem} setActiveItem={setActiveItem} />

        <div className="admin-page-content">{renderContent()}</div>
      </div>

      <Modal open={showComplaintModal} onClose={() => setShowComplaintModal(false)}>
        {selectedComplaint && (
          <div className="complaint-details-modal">
            <h2>Детали жалобы #{selectedComplaint.id}</h2>
            <div className="complaint-details-content">
              <div className="complaint-detail-item">
                <span className="detail-label">Причина:</span>
                <span className="detail-value">{selectedComplaint.reason}</span>
              </div>
              <div className="complaint-detail-item">
                <span className="detail-label">Дата:</span>
                <span className="detail-value">{selectedComplaint.date}</span>
              </div>
              <div className="complaint-detail-item">
                <span className="detail-label">Пользователь:</span>
                <div className="detail-user">
                  <img src={selectedComplaint.avatar} alt="Avatar" className="user-avatar-small"/>
                  <span>{selectedComplaint.user}</span>
                </div>
              </div>
              <div className="complaint-detail-item">
                <span className="detail-label">Целевой контент:</span>
                <span className="detail-value">{selectedComplaint.targetContent}</span>
              </div>
              <div className="complaint-detail-item full-width">
                <span className="detail-label">Описание:</span>
                <p className="detail-description">{selectedComplaint.details}</p>
              </div>
              <div className="complaint-detail-item">
                <span className="detail-label">Статус:</span>
                <span className={`detail-status ${selectedComplaint.isClosed ? 'closed' : 'open'}`}>
                  {selectedComplaint.isClosed ? 'Закрыта' : 'Открыта'}
                </span>
              </div>
            </div>
          </div>
        )}
      </Modal>

      <Modal open={showUnblockDialog} onClose={() => setShowUnblockDialog(false)}>
        <ConfirmDialog
          title="Разблокировка пользователя"
          message="Вы уверены, что хотите разблокировать этого пользователя? Он снова сможет использовать все функции сайта."
          onConfirm={confirmUnblockUser}
          onCancel={() => {
            setShowUnblockDialog(false);
            setSelectedUserId(null);
          }}
        />
      </Modal>

      <Modal open={showUnhideDialog} onClose={() => setShowUnhideDialog(false)}>
        <ConfirmDialog
          title="Восстановление контента"
          message="Вы уверены, что хотите восстановить этот контент? Он снова станет видимым для всех пользователей."
          onConfirm={confirmUnhideContent}
          onCancel={() => {
            setShowUnhideDialog(false);
            setSelectedContentId(null);
          }}
        />
      </Modal>
    </main>
  );
}

export default AdminPage;