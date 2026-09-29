import { api } from "../../../services/api/client";

function queryString(params) {
  const query = new URLSearchParams(
    Object.entries(params).filter(
      ([, value]) => value !== "" && value !== undefined && value !== null,
    ),
  );
  return query.size ? `?${query}` : "";
}
export const questionsApi = {
  // Use existing list filters so counts retain the server's user/faculty scope.
  statusCounts: async () => Object.fromEntries(await Promise.all(
    ["APPROVED", "PENDING_REVIEW", "NEED_REVISION", "DRAFT", "REJECTED", "ARCHIVED"].map(async (status) => {
      const result = await api.get(`/api/v1/questions${queryString({ status, page: 0, size: 1 })}`);
      return [status, result.totalElements ?? 0];
    }),
  )),
  uploadImage: (file, kind = "question") => {
    const body = new FormData();
    body.append("file", file);
    return api.post(
      `/api/v1/questions/images?kind=${encodeURIComponent(kind)}`,
      body,
    );
  },
  list: async (params) => {
    const result = await api.get(`/api/v1/questions${queryString(params)}`);
    return { ...result, items: await catalogNames(result.items) };
  },
  approved: async (params) => {
    const result = await api.get(`/api/v1/questions/approved${queryString(params)}`);
    return { ...result, items: await catalogNames(result.items) };
  },
  get: async (id) =>
    (await catalogNames([await api.get(`/api/v1/questions/${id}`)]))[0],
  create: (question) => api.post("/api/v1/questions", question),
  update: (id, question) => api.put(`/api/v1/questions/${id}`, question),
  submit: (id) => api.post(`/api/v1/questions/${id}/submit`),
  archive: (id) => api.post(`/api/v1/questions/${id}/archive`),
  approve: (id, reason) =>
    api.post(`/api/v1/questions/${id}/approve`, { reason: reason || null }),
  reject: (id, reason) =>
    api.post(`/api/v1/questions/${id}/reject`, { reason }),
  requestRevision: (id, reason) =>
    api.post(`/api/v1/questions/${id}/request-revision`, { reason }),
  bulk: (action, ids, reason) =>
    api.post(`/api/v1/questions/bulk/${action}`, { ids, reason: reason || null }),
  subjects: () => api.get("/api/v1/subjects"),
  chapters: (subjectId) =>
    api.get(`/api/v1/chapters${queryString({ subjectId })}`),
  topics: (chapterId) => api.get(`/api/v1/topics${queryString({ chapterId })}`),
  knowledgeItems: (topicId) =>
    api.get(`/api/v1/knowledge-items${queryString({ topicId })}`),
};

// Display names are resolved from catalog endpoints, not assumed response fields.
async function catalogNames(items = []) {
  if (!items.length) return items;
  const creatorIds = [...new Set(items.map((q) => q.createdBy).filter(Boolean))];
  const results = await Promise.allSettled([
    questionsApi.subjects(),
    api.get(`/api/v1/users/directory?ids=${creatorIds.map((id) => encodeURIComponent(id)).join(",")}`),
    ...[...new Set(items.map((q) => q.subjectId))].map((id) =>
      questionsApi.chapters(id),
    ),
    ...[...new Set(items.map((q) => q.chapterId))].map((id) =>
      questionsApi.topics(id),
    ),
  ]);
  const names = new Map(
    results
      .filter((_, index) => index !== 1)
      .flatMap((result) => (result.status === "fulfilled" ? result.value : []))
      .map((item) => [item.id, item.name]),
  );
  const people = new Map(results[1].status === "fulfilled" ? results[1].value.map((item) => [item.userId, item]) : []);
  return items.map((q) => ({
    ...q,
    subjectName: names.get(q.subjectId),
    chapterName: names.get(q.chapterId),
    topicName: names.get(q.topicId),
    authorName: people.get(q.createdBy)?.fullName || people.get(q.createdBy)?.displayName,
    createdByName: people.get(q.createdBy)?.fullName || people.get(q.createdBy)?.displayName,
    lecturerCode: people.get(q.createdBy)?.lecturerCode,
    creatorAvatar: people.get(q.createdBy)?.avatarUrl,
  }));
}
