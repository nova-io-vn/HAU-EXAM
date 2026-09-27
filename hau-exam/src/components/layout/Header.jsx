import { useEffect, useMemo, useState } from "react";
import { Button, Icon } from "../ui";
import { NotificationBell } from "../../features/notifications";
import { UserMenu } from "./UserMenu";
import { GlobalSearch } from "./GlobalSearch";
import { HumanChatDropdown } from "../../features/support/components/HumanChatDropdown";
import { useAuth } from "../../features/auth/hooks/useAuth";

function useCurrentDate() {
  const [now, setNow] = useState(() => new Date());
  useEffect(() => {
    const timer = window.setInterval(() => setNow(new Date()), 60_000);
    return () => window.clearInterval(timer);
  }, []);
  return useMemo(() => {
    const value = new Intl.DateTimeFormat("vi-VN", { weekday: "long", day: "numeric", month: "long", year: "numeric" }).format(now);
    return value.charAt(0).toUpperCase() + value.slice(1);
  }, [now]);
}

export function Header({ collapsed, onToggle, onMobileMenu }) {
  const date = useCurrentDate();
  const { role } = useAuth();
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
      <time className="topbar-date" dateTime={new Date().toISOString().slice(0, 10)}>{date}</time>
      <GlobalSearch role={role} />
      <NotificationBell />
      <div data-tour="human-chat"><HumanChatDropdown /></div>
      <UserMenu />
    </header>
  );
}
