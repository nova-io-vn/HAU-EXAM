import { api } from "../../../services/api/client";
export const catalogApi = {
  subjects: () => api.get("/api/v1/subjects"),
  createSubject: (body) => api.post("/api/v1/subjects", body),
  updateSubject: (id, body) => api.put("/api/v1/subjects/" + id, body),
  deleteSubject: (id) => api.delete("/api/v1/subjects/" + id),
  chapters: (subjectId, options) =>
    api.get("/api/v1/chapters?subjectId=" + encodeURIComponent(subjectId), options),
  createChapter: (body) => api.post("/api/v1/chapters", body),
  updateChapter: (id, body) => api.put("/api/v1/chapters/" + id, body),
  deleteChapter: (id) => api.delete("/api/v1/chapters/" + id),
  topics: (chapterId, options) =>
    api.get("/api/v1/topics?chapterId=" + encodeURIComponent(chapterId), options),
  createTopic: (body) => api.post("/api/v1/topics", body),
  updateTopic: (id, body) => api.put("/api/v1/topics/" + id, body),
  deleteTopic: (id) => api.delete("/api/v1/topics/" + id),
  lecturers: (subjectId) => api.get(`/api/v1/subjects/${subjectId}/lecturers`),
  eligibleLecturers: (subjectId, keyword = "") => api.get(`/api/v1/subjects/${subjectId}/eligible-lecturers${keyword ? `?keyword=${encodeURIComponent(keyword)}` : ""}`),
  assignLecturer: (subjectId, userId) => api.post(`/api/v1/subjects/${subjectId}/lecturers`, { userId }),
  removeLecturer: (subjectId, userId) => api.delete(`/api/v1/subjects/${subjectId}/lecturers/${userId}`),
};
