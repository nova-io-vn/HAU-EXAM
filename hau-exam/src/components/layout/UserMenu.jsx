import { Link } from "react-router-dom";
import { routes } from "../../constants/routes";
import { roles } from "../../constants/roles";
import { useAuth } from "../../features/auth/hooks/useAuth";
import { useTheme } from "../../app/providers/ThemeProvider";
import { Icon } from "../ui";
import { Avatar } from "../shared/Avatar";
import {
  formatAcademicName,
  formatRoleFaculty,
} from "../../features/users/model/academic";

export function UserMenu({ compact = false }) {
  const auth = useAuth();
  const theme = useTheme();
  const name = formatAcademicName(auth.currentUser || {});
  function toggleTheme() {
    theme?.setPreference(theme.resolved === "dark" ? "light" : "dark");
  }
  const themeIcon = theme?.resolved === "dark" ? "moon" : "sun";
  const themeAction = theme?.resolved === "dark" ? "Chuyển sang giao diện sáng" : "Chuyển sang giao diện tối";
  return (
    <details className={`user-menu ${compact ? 'user-menu-compact' : ''}`}>
      <summary data-tour="profile-menu" aria-label="Mở menu tài khoản">
        <Avatar user={auth.currentUser || {}} size="sm" />
        {!compact && <span className="profile-copy">
          <strong>{name}</strong>
          <small>
            {formatRoleFaculty(auth.currentUser || {}, auth.facultyId)}
          </small>
        </span>}
        <Icon name="chevron" size={14} />
      </summary>
      <div className="user-menu-popover">
        <div className="user-menu-heading">
          <strong>Hồ sơ tài khoản</strong>
          <span>
            {formatRoleFaculty(auth.currentUser || {}, auth.facultyId)}
          </span>
        </div>
        <Link to={routes.profile}>Hồ sơ cá nhân</Link>
        <Link to={routes.systemHelp}>Trợ lý HAU QM</Link>
        <Link to={routes.help}>Trung tâm trợ giúp</Link>
        {auth.role === roles.SYSTEM_ADMIN && (
          <Link to={routes.settings}>Cài đặt hệ thống</Link>
        )}
        <div className="user-menu-theme-row">
          <span>Cài đặt giao diện</span>
          <button
            type="button"
            className="user-menu-theme-toggle"
            aria-label={themeAction}
            aria-pressed={theme?.resolved === "dark"}
            title={themeAction}
            onClick={toggleTheme}
          >
            <Icon name={themeIcon} size={16} />
          </button>
        </div>
    
      </div>
    </details>
  );
}
