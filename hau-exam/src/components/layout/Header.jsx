import { useLocation } from "react-router-dom";
import { Button, Icon } from "../ui";
import { NotificationBell } from "../../features/notifications";
import { UserMenu } from "./UserMenu";
import { GlobalSearch } from "./GlobalSearch";
import { useAuth } from "../../features/auth/hooks/useAuth";
import { getRouteMeta } from "../../app/router/routeConfig";
import { HumanChatDropdown } from "../../features/support/components/HumanChatDropdown";
export function Header({ collapsed, onToggle, onMobileMenu }) {
  const { pathname } = useLocation();
  const { role, currentUser, facultyId } = useAuth();
  const page = getRouteMeta(pathname);
  const faculty =
    currentUser?.facultyName ||
    currentUser?.faculty?.name ||
    facultyId ||
    "chưa phân công";
  const breadcrumb =
    role === "SUBJECT_ADMIN"
      ? `Khoa ${faculty} / Quản lý chuyên môn`
      : role === "USER"
        ? "Không gian giảng viên"
        : "Quản trị hệ thống";
  return (
    <header className="topbar">
      <Button
        variant="ghost"
        className="desktop-toggle"
        onClick={onToggle}
        aria-label={collapsed ? "Mở rộng thanh bên" : "Thu gọn thanh bên"}
      >
        <Icon name="dashboard" size={16} />
      </Button>
      <Button
        variant="ghost"
        className="mobile-toggle"
        onClick={onMobileMenu}
        aria-label="Mở điều hướng"
      >
        <Icon name="dashboard" size={17} />
      </Button>
      <div className="breadcrumb-context">
        <span>HAU QM / {breadcrumb}</span>
        <strong>{page?.title || "Tổng quan"}</strong>
      </div>
      <GlobalSearch role={role} />
      <NotificationBell />
      <HumanChatDropdown />
      <UserMenu />
    </header>
  );
}
