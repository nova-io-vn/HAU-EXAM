import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { routes } from "../../constants/routes";
import { roles } from "../../constants/roles";
import { useAuth } from "../../features/auth/hooks/useAuth";
import { useUiPreferences } from "../../app/providers/ThemeProvider";
import { Icon } from "../ui";
import { Avatar } from "../shared/Avatar";
import { authApi } from "../../features/auth/api/authApi";
import { authStore } from "../../stores/authStore";
import { ChangePasswordDialog } from "../../features/auth/components/ChangePasswordDialog";
import {
  formatAcademicName,
  formatRoleFaculty,
} from "../../features/users/model/academic";

const themes = [
  { value: "light", label: "Sáng", icon: "sun" },
  { value: "dark", label: "Tối", icon: "moon" },
  { value: "system", label: "Tự động", icon: "settings" },
];

const cursorEffects = [
  { value: "none", label: "Tắt", icon: "minus" },
  { value: "glow", label: "Ánh sáng", icon: "sparkles" },
  { value: "stars", label: "Sao nhỏ", icon: "sparkles" },
  { value: "particles", label: "Hạt", icon: "grid" },
  { value: "trail", label: "Vệt mềm", icon: "network" },
];

export function UserMenu({ compact = false }) {
  const auth = useAuth();
  const preferences = useUiPreferences();
  const navigate = useNavigate();
  const [passwordOpen, setPasswordOpen] = useState(false);
  const [submenu, setSubmenu] = useState(null);
  const name = formatAcademicName(auth.currentUser || {});

  async function logout() {
    const refreshToken = authStore.getRefreshToken();
    try { if (refreshToken) await authApi.logout(refreshToken); } catch { /* Local logout still prevents reuse in this browser. */ }
    authStore.clear();
    navigate(routes.login, { replace: true });
  }

  const toggleSubmenu = value => setSubmenu(current => current === value ? null : value);

  return (
    <>
      <details className={`user-menu ${compact ? "user-menu-compact" : ""}`}>
        <summary data-tour="profile-menu" aria-label="Mở menu tài khoản">
          <Avatar user={auth.currentUser || {}} size="sm" />
          {!compact && <span className="profile-copy">
            <strong>{name}</strong>
            <small>{formatRoleFaculty(auth.currentUser || {}, auth.facultyId)}</small>
          </span>}
          <Icon name="chevron" size={14} />
        </summary>
        <div className="user-menu-popover">
          <div className="user-menu-heading">
            <strong>Hồ sơ tài khoản</strong>
            <span>{formatRoleFaculty(auth.currentUser || {}, auth.facultyId)}</span>
          </div>

          <Link to={routes.profile}><Icon name="user" variant="primary" size={16} />Hồ sơ cá nhân</Link>
          <button type="button" className="user-menu-action" onClick={() => setPasswordOpen(true)}><Icon name="settings" variant="info" size={16} />Đổi mật khẩu</button>

          <div className="user-menu-section">
            <button type="button" className="user-menu-submenu-trigger" aria-expanded={submenu === "theme"} onClick={() => toggleSubmenu("theme")}>
              <span><Icon name="sun" variant="primary" size={16} />Cài đặt giao diện</span><Icon name="chevron" size={14} />
            </button>
            {submenu === "theme" && <div className="user-menu-options" role="radiogroup" aria-label="Chế độ giao diện">
              <small>Chế độ giao diện</small>
              {themes.map(option => <button type="button" role="radio" aria-checked={preferences?.preference === option.value} className={preferences?.preference === option.value ? "is-selected" : ""} key={option.value} onClick={() => preferences?.setPreference(option.value)}><Icon name={option.icon} variant="primary" size={15} />{option.label}<Icon name="check" variant="success" size={14} /></button>)}
            </div>}

            <button type="button" className="user-menu-submenu-trigger" aria-expanded={submenu === "cursor"} onClick={() => toggleSubmenu("cursor")}>
              <span><Icon name="sparkles" variant="info" size={16} />Hiệu ứng con trỏ</span><Icon name="chevron" size={14} />
            </button>
            {submenu === "cursor" && <div className="user-menu-options" role="radiogroup" aria-label="Hiệu ứng con trỏ">
              {cursorEffects.map(option => <button type="button" role="radio" aria-checked={preferences?.cursorEffect === option.value} className={preferences?.cursorEffect === option.value ? "is-selected" : ""} key={option.value} onClick={() => preferences?.setCursorEffect(option.value)}><Icon name={option.icon} variant="info" size={15} />{option.label}<Icon name="check" variant="success" size={14} /></button>)}
            </div>}

            <div className="user-menu-kute-row">
              <span><Icon name="sparkles" variant="purple" size={16} />Trợ lý Kute</span>
              <button type="button" className="user-menu-switch" role="switch" aria-label="Hiển thị Kute" aria-checked={preferences?.kuteVisible !== false} onClick={() => preferences?.setKuteVisible(!(preferences?.kuteVisible !== false))}><span /></button>
            </div>
          </div>

          <div className="user-menu-section user-menu-links">
            {(auth.role === roles.SYSTEM_ADMIN || auth.role === roles.SUBJECT_ADMIN) && <Link to={routes.telegram}><Icon name="bell" variant="info" size={16} />Thông báo Telegram</Link>}
            {auth.role === roles.SYSTEM_ADMIN && <Link to={routes.settings}><Icon name="settings" variant="muted" size={16} />Cài đặt hệ thống</Link>}
          </div>

          <button type="button" className="user-menu-logout" onClick={() => void logout()}><Icon name="logout" size={16} />Đăng xuất</button>
        </div>
      </details>
      <ChangePasswordDialog open={passwordOpen} onClose={() => setPasswordOpen(false)} />
    </>
  );
}
