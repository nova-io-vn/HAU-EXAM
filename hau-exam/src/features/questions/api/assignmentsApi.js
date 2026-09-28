import { api } from "../../../services/api/client";

export const assignmentsApi = {
  list: () => api.get("/api/v1/question-assignments"),
  get: (id) => api.get(`/api/v1/question-assignments/${id}`),
  create: (body) => api.post("/api/v1/question-assignments", body),
  update: (id, body) => api.put(`/api/v1/question-assignments/${id}`, body),
};
