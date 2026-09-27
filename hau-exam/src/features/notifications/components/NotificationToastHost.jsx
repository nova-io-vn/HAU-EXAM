import { useEffect } from "react";
import { useNotifications } from "../hooks/useNotifications";
import { notificationStore } from "../store/notificationStore";
import { notificationTarget } from "../model/notificationModel";
import { useNavigate } from "react-router-dom";

export function NotificationToastHost() {
  const { toasts } = useNotifications();
  const navigate = useNavigate();
  useEffect(() => {
    const timers = toasts.map((item) =>
      setTimeout(
        () => notificationStore.dismissToast(item.id || item.eventId),
        5000,
      ),
    );
    return () => timers.forEach(clearTimeout);
  }, [toasts]);
  return (
    <div
      className="toast-region"
      aria-live="polite"
      aria-label="Thông báo realtime"
    >
      {toasts.map((item) => (
        <div className={`notification-toast toast-${String(item.tone || item.type || "info").toLowerCase()}`} key={item.id || item.eventId} role={item.tone === "ERROR" ? "alert" : "status"}>
          <span className="notification-toast-icon" aria-hidden="true">{{SUCCESS:"✓",ERROR:"×",WARNING:"!",INFO:"i"}[item.tone || item.type] || "i"}</span>
          <div>
            <strong>{item.title}</strong>
            <span>{item.content}</span>
            {notificationTarget(item) && <button type="button" className="notification-toast-link" onClick={() => { notificationStore.dismissToast(item.id || item.eventId); navigate(notificationTarget(item)); }}>{item.actionLabel || "Mở nội dung liên quan →"}</button>}
          </div>
          <button
            type="button"
            onClick={() =>
              notificationStore.dismissToast(item.id || item.eventId)
            }
            aria-label="Đóng thông báo"
          >
            ×
          </button>
        </div>
      ))}
    </div>
  );
}
