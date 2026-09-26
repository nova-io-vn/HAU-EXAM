import { api } from "../../../services/api/client";
const base = "/api/v1/support/conversations";
const query = ({ page = 0, size = 20, status } = {}) => { const p = new URLSearchParams({ page, size }); if (status) p.set("status", status); return p; };
export const supportChatApi = {
  mine: (params) => api.get(`${base}/my?${query(params)}`),
  assigned: (params) => api.get(`${base}/assigned?${query(params)}`),
  adminList: (params) => api.get(`/api/v1/admin/support/conversations?${query(params)}`),
  get: (id) => api.get(`${base}/${id}`),
  messages: (id, params) => api.get(`${base}/${id}/messages?${query({ ...params, size: params?.size || 30 })}`),
  create: (subject, content = "", recipientUserId = null) => api.post(base, { subject, content, recipientUserId }),
  send: (id, content, file) => { const form = new FormData(); if (content) form.append("content", content); if (file) form.append("file", file, file.name || "support-image"); return api.post(`${base}/${id}/messages`, form); },
  read: (id) => api.patch(`${base}/${id}/read`),
  unread: () => api.get("/api/v1/support/unread-count"),
  status: (id, status) => api.patch(`/api/v1/admin/support/conversations/${id}/status`, { status }),
};
