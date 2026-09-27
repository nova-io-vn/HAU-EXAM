import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Button, DataTable, Loading, StatusBadge } from "../../../components/ui";
import { PageHeader } from "../../../components/shared/PageHeader";
import { aiApi } from "../api/aiApi";
import { jobTypeLabels } from "../model/aiModel";

export function AdminAiJobsPage() {
  const [page, setPage] = useState(0); const [state, setState] = useState({ loading: true });
  const load = useCallback(async () => {
    try { setState({ data: await aiApi.adminJobs(page) }); }
    catch (error) { setState({ error }); }
  }, [page]);
  useEffect(() => { let active = true; aiApi.adminJobs(page).then(data => { if (active) setState({ data }); }).catch(error => { if (active) setState({ error }); }); return () => { active = false; }; }, [page]);
  useEffect(() => { const refresh = (event) => { const type = event.detail?.type || event.detail?.eventType || ""; if (type.includes("AI") || event.detail?.referenceType === "AI_JOB") load(); }; window.addEventListener("hau:realtime", refresh); return () => window.removeEventListener("hau:realtime", refresh); }, [load]);
  function changePage(next) { setState({ loading: true }); setPage(next); }
  if (state.loading) return <section className="surface ai-list"><Loading label="Đang tải AI jobs" /></section>;
  if (state.error) return <section className="surface ai-list"><p className="ai-error">{state.error.message}</p><Button onClick={load}>Thử lại</Button></section>;
  const data = state.data || { items: [] };
  return <section><PageHeader title="Theo dõi AI jobs" description="Theo dõi toàn bộ tác vụ AI đang chạy, hoàn tất hoặc thất bại trong hệ thống." actions={<Button variant="secondary" onClick={load}>Làm mới</Button>} /><div className="surface ai-list"><DataTable rows={data.items || []} rowKey="jobId" emptyTitle="Chưa có AI job" columns={[{ key: "jobId", header: "Mã job", render: job => <Link to={`/ai/jobs/${job.jobId}`}>{job.jobId}</Link> }, { key: "type", header: "Loại", render: job => jobTypeLabels[job.type] || job.type }, { key: "status", header: "Trạng thái", render: job => <StatusBadge status={job.status} /> }, { key: "requestedBy", header: "Người tạo", render: job => job.creatorName || "—" }, { key: "facultyId", header: "Khoa", render: job => job.facultyId || "—" }, { key: "createdAt", header: "Tạo lúc", render: job => job.createdAt || "—" }, { key: "updatedAt", header: "Cập nhật", render: job => job.updatedAt || "—" }]} /><footer className="ai-inline ai-pagination"><Button variant="secondary" disabled={page === 0} onClick={() => changePage(page - 1)}>Trước</Button><span>Trang {page + 1} · {data.totalElements || 0} job</span><Button variant="secondary" disabled={page + 1 >= (data.totalPages || 1)} onClick={() => changePage(page + 1)}>Sau</Button></footer></div></section>;
}
