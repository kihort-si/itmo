import './Sidebar.scss'

interface SidebarProps {
  activeItem: string;
  setActiveItem: (item: string) => void;
}

function Sidebar({activeItem, setActiveItem}: SidebarProps) {

  const menuItems = [
    {id: 'complaints', label: 'Жалобы'},
    {id: 'blocked-users', label: 'Заблокированные пользователи'},
    {id: 'hidden', label: 'Скрытый контент'},
    {id: 'statistics', label: 'Статистика'},
  ];

  return (
    <div className="sidebar">
      <h2>Инструменты</h2>
      <ul>
        {menuItems.map(item => (
          <li
            key={item.id}
            className={activeItem === item.id ? 'active' : ''}
            onClick={() => setActiveItem(item.id)}
          >
            {item.label}
          </li>
        ))}
      </ul>
    </div>
  );
}

export default Sidebar;