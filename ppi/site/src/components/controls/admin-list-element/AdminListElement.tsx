import './AdminListElement.scss'
import PrimaryButton from "../primary-button/PrimaryButton.tsx";

interface AdminListElementProps {
  type: 'complaints' | 'blocked-users' | 'hidden' | "statistics";
  item: {
    id: string | number;
    reason?: string;
    date?: string;
    user?: string;
    isClosed?: boolean;
    avatar?: string;
    content?: string;
  };
  onAction: () => void;
  onToggleStatus?: (id: string, isClosed: boolean) => void;
}

const AdminListElement = ({type, item, onAction, onToggleStatus}: AdminListElementProps) => {
  const {reason, date, user, isClosed, avatar, content} = item;

  const renderContent = () => {
    switch (type) {
      case 'complaints':
        return (
          <div className="list-info">
            <span className="list-reason">{reason}</span>
            <span className="list-date">{date}</span>
            <div className="list-user">
              <img src={avatar} alt="Avatar" className="user-avatar"/>
              <span>{user}</span>
            </div>
            <div className="list-status-checkbox">
              <label>
                <input
                  type="checkbox"
                  checked={isClosed}
                  onChange={(e) => onToggleStatus && onToggleStatus(item.id.toString(), e.target.checked)}
                />
                <span className={`status-text ${isClosed ? 'closed' : 'open'}`}>
                                    {isClosed ? 'Закрыта' : 'Открыта'}
                                </span>
              </label>
            </div>
          </div>
        );
      case 'blocked-users':
        return (
          <div className="list-info">
            <span className="list-reason">{reason || 'Без причины'}</span>
            <div className="list-user">
              <img src={avatar} alt="Avatar" className="user-avatar"/>
              <span>{user}</span>
            </div>
            <span className="list-date">{date}</span>

          </div>
        );

      case 'hidden':
        return (
          <div className="list-info">
            <span className="list-content">{content}</span>
            <span className="list-date">{date}</span>
            <div className="list-user">
              <img src={avatar} alt="Avatar" className="user-avatar"/>
              <span>{user}</span>
            </div>
          </div>
        );

      default:
        return null;
    }
  };

  return (
    <div className="admin-list-element">
      {renderContent()}
      <PrimaryButton onClick={onAction} label={
        type === 'complaints' ? 'Перейти к контенту' :
          type === 'blocked-users' ? 'Разблокировать' :
            type === 'hidden' ? 'Показать' : ''
      }>
      </PrimaryButton>
    </div>
  );
};

export default AdminListElement;