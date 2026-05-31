import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

interface LayoutProps {
  title: string;
  navItems: { to: string; label: string }[];
}

export function Layout({ title, navItems }: LayoutProps) {
  const { user, logout } = useAuth();

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-icon">SMS</span>
          <div>
            <strong>Student Management</strong>
            <small>{title}</small>
          </div>
        </div>
        <nav className="sidebar-nav">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to.endsWith('/admin') || item.to.endsWith('/student') || item.to.endsWith('/teacher')}
              className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-footer">
          <div className="user-chip">
            <span>{user?.email}</span>
            <small>{user?.role}</small>
          </div>
          <button type="button" className="btn btn-secondary btn-block" onClick={logout}>
            Logout
          </button>
        </div>
      </aside>
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
}
