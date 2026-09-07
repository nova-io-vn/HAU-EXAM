import { useEffect, useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { navigationByRole, bottomNavigation } from "../../constants/navigation";
import { routes } from "../../constants/routes";
import { authStore } from "../../stores/authStore";
import { useAuth } from "../../features/auth/hooks/useAuth";
import { questionsApi } from "../../features/questions/api/questionsApi";
import { Icon } from "../ui";

const roleLabel = {
  SYSTEM_ADMIN: "Quản trị viên hệ thống",
  SUBJECT_ADMIN: "Quản trị viên chuyên môn",
  USER: "Giảng viên",
};
export function Sidebar({ collapsed, mobileOpen, onClose }) {
  const { role, facultyId, currentUser } = useAuth();
  const navigate = useNavigate();
  const [pending, setPending] = useState(null);
  useEffect(() => {
    if (role !== "SUBJECT_ADMIN") return;
    let active = true;
    questionsApi
      .list({ status: "PENDING_REVIEW", page: 0, size: 1 })
      .then((result) => {
        if (active) setPending(result.totalElements ?? 0);
      })
      .catch(() => {
        if (active) setPending(null);
      });
    return () => {
      active = false;
    };
  }, [role]);
  const groups = navigationByRole[role] || [];
  const faculty =
    currentUser?.facultyName ||
    currentUser?.faculty?.name ||
    facultyId ||
    "Chưa phân công";
  function logout() {
    authStore.clear();
    navigate(routes.login, { replace: true });
  }
  function link(item) {
    return (
      <NavLink
        key={item.to}
        to={item.to}
        end={item.end ?? true}
        onClick={onClose}
        data-tour={item.tour}
        title={collapsed ? item.label : undefined}
        className={({ isActive }) => (isActive ? "active" : "")}
      >
        <span className="nav-icon">
          <Icon name={item.icon} />
        </span>
        {!collapsed && <span className="nav-label">{item.label}</span>}
        {!collapsed && item.badge === "pending" && pending > 0 && (
          <span
            className="nav-count"
            aria-label={`${pending} câu hỏi chờ duyệt`}
          >
            {pending > 99 ? "99+" : pending}
          </span>
        )}
      </NavLink>
    );
  }
  return (
    <>
      <button
        className={`sidebar-backdrop ${mobileOpen ? "is-open" : ""}`}
        type="button"
        aria-label="Đóng điều hướng"
        onClick={onClose}
      />
      <aside
        className={`sidebar ${mobileOpen ? "is-open" : ""}`}
        aria-label="Điều hướng chính"
      >
        <div className="brand">
          <span className="brand-mark">H</span>
          {!collapsed && (
            <span className="brand-copy">
              <strong>HAU QM</strong>
              <small>Hệ thống Quản lý Khảo thí</small>
            </span>
          )}
        </div>
        {!collapsed && (
          <div className="sidebar-context">
            <span className="context-dot" />
            <span>
              {role === "SUBJECT_ADMIN"
                ? `Khoa ${faculty}`
                : roleLabel[role] || "Hệ thống"}
            </span>
          </div>
        )}
        <nav className="sidebar-nav">
          {groups.map((group) => (
            <div className="nav-group" key={group.section}>
              <p className="nav-section-label">
                {collapsed ? "" : group.section}
              </p>
              {group.items.map(link)}
            </div>
          ))}
        </nav>
        <div className="sidebar-bottom">
          {bottomNavigation.map(link)}
          <button type="button" onClick={logout}>
            <span className="nav-icon">
              <Icon name="logout" />
            </span>
            {!collapsed && <span>Đăng xuất</span>}
          </button>
        </div>
      </aside>
    </>
  );
}
