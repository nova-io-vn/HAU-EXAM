import { notificationsApi } from "../api/notificationsApi";
import {
  normalizeNotification,
  normalizeNotificationPage,
} from "../model/notificationModel";

let state = {
  notifications: [],
  unreadCount: 0,
  connectionStatus: "disconnected",
  loading: false,
  error: null,
  toasts: [],
};
const listeners = new Set();
const seen = new Set();
const MAX_TOASTS = 5;
function publish(patch) {
  state = { ...state, ...patch };
  listeners.forEach((listener) => listener());
}
function keyOf(item) {
  return item.id || item.eventId;
}
function remember(key) {
  if (!key) return true;
  if (seen.has(key)) return false;
  seen.add(key);
  if (seen.size > 500) seen.delete(seen.values().next().value);
  return true;
}

function reconcile(serverItems, currentItems) {
  const serverKeys = new Set(serverItems.map(keyOf).filter(Boolean));
  return [...currentItems.filter(item => !serverKeys.has(keyOf(item))), ...serverItems]
    .sort((left, right) => new Date(right.createdAt || 0) - new Date(left.createdAt || 0))
    .slice(0, 50);
}

export const notificationStore = {
  getSnapshot: () => state,
  subscribe(listener) {
    listeners.add(listener);
    return () => listeners.delete(listener);
  },
  setConnectionStatus(connectionStatus) {
    publish({ connectionStatus });
  },
  async sync() {
    publish({ loading: true, error: null });
    try {
      const [list, count] = await Promise.all([
        notificationsApi.list({ page: 0, size: 20 }),
        notificationsApi.unreadCount(),
      ]);
      const page = normalizeNotificationPage(list);
      page.items.forEach((item) => remember(keyOf(item)));
      const notifications = reconcile(page.items, state.notifications);
      publish({
        notifications,
        unreadCount: Math.max(
          typeof count === "number" ? count : (count?.count ?? count?.unreadCount ?? 0),
          notifications.filter(item => !item.isRead).length,
        ),
        loading: false,
      });
    } catch (error) {
      publish({ error, loading: false });
    }
  },
  receive(message) {
    const item = normalizeNotification(message);
    if (!remember(keyOf(item))) return;
    if (typeof window !== "undefined") window.dispatchEvent(new CustomEvent("hau:realtime", { detail: item }));
    publish({
      notifications: [item, ...state.notifications].slice(0, 50),
      unreadCount: state.unreadCount + (item.isRead ? 0 : 1),
      toasts: [...state.toasts, { ...item, tone: "INFO" }].slice(-MAX_TOASTS),
    });
  },
  pushToast({ type = "SUCCESS", title, content = "", actionUrl = null, actionLabel = null, dedupeKey = null } = {}) {
    const tone = String(type || "INFO").toUpperCase();
    const fallback = { SUCCESS: "Thành công", ERROR: "Lỗi", WARNING: "Cảnh báo", INFO: "Thông tin" }[tone] || "Thông tin";
    const key = dedupeKey || `${tone}:${title || fallback}:${content}`;
    const now = Date.now();
    if (state.toasts.some(item => item.dedupeKey === key && now - item.timestamp < 1500)) return;
    const item = { id: `toast-${now}-${Math.random()}`, type: tone, tone, title: title || fallback, content, actionUrl, actionLabel, dedupeKey: key, timestamp: now, isRead: true, createdAt: new Date(now).toISOString() };
    publish({ toasts: [...state.toasts, item].slice(-MAX_TOASTS) });
  },
  async markRead(id) {
    const item = state.notifications.find(
      (notification) => notification.id === id,
    );
    if (item?.isRead) return;
    await notificationsApi.markRead(id);
    publish({
      notifications: state.notifications.map((notification) =>
        notification.id === id
          ? { ...notification, isRead: true }
          : notification,
      ),
      unreadCount: item
        ? Math.max(0, state.unreadCount - 1)
        : state.unreadCount,
    });
  },
  async markAllRead() {
    await notificationsApi.markAllRead();
    publish({
      notifications: state.notifications.map((item) => ({
        ...item,
        isRead: true,
      })),
      unreadCount: 0,
    });
  },
  dismissToast(id) {
    publish({ toasts: state.toasts.filter((item) => keyOf(item) !== id) });
  },
};

export const toast = {
  success: (content, options = {}) => notificationStore.pushToast({ type: "SUCCESS", content, ...options }),
  error: (content, options = {}) => notificationStore.pushToast({ type: "ERROR", content, ...options }),
  warning: (content, options = {}) => notificationStore.pushToast({ type: "WARNING", content, ...options }),
  info: (content, options = {}) => notificationStore.pushToast({ type: "INFO", content, ...options }),
};
