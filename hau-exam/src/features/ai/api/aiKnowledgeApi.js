import { api } from "../../../services/api/client";

const base = "/api/v1/admin/ai-policy/documents";

export const aiKnowledgeApi = {
  list: () => api.get(base),
  upload: ({ title, description, file }) => {
    const body = new FormData();
    body.append("title", title);
    if (description?.trim()) body.append("description", description.trim());
    body.append("file", file);
    return api.post(base, body);
  },
  setEnabled: (id, enabled) =>
    api.patch(`${base}/${id}?enabled=${encodeURIComponent(enabled)}`),
  reprocess: (id) => api.post(`${base}/${id}/reprocess`, {}),
  remove: (id) => api.delete(`${base}/${id}`),
};
