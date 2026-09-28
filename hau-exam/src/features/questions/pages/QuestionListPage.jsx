import { useCallback, useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { Button, ConfirmDialog, Drawer, TableSkeleton } from "../../../components/ui";
import { PageHeader } from "../../../components/shared/PageHeader";
import { routes } from "../../../constants/routes";
import { roles } from "../../../constants/roles";
import { useAuth } from "../../auth/hooks/useAuth";
import { questionsApi } from "../api/questionsApi";
import { QuestionFilters } from "../components/QuestionFilters";
import { QuestionPagination } from "../components/QuestionPagination";
import { QuestionPreview } from "../components/QuestionPreview";
import { QuestionTable } from "../components/QuestionTableFixed";
import { useQuestionCatalogs } from "../hooks/useQuestionCatalogs";
import { normalizePage } from "../model/questionModel";
import { api } from "../../../services/api/client";

const initialFilters = {
  facultyId: "",
  subjectId: "",
  chapterId: "",
  topicId: "",
  difficulty: "",
  status: "",
  source: "",
  keyword: "",
};
export function QuestionListPage() {
  const auth = useAuth();
  const requestVersion = useRef(0);
  const isOwnerView = auth.role === roles.USER;
  const creatorId = auth.currentUser?.id;
  const [draft, setDraft] = useState(() => ({
    ...initialFilters,
    facultyId: auth.facultyId || "",
  }));
  const [query, setQuery] = useState(() => ({
    ...initialFilters,
    facultyId: auth.facultyId || "",
  }));
  const [page, setPage] = useState(0);
  const [data, setData] = useState(normalizePage());
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(null);
  const [preview, setPreview] = useState(null);
  const [confirmation, setConfirmation] = useState(null);
  const [selectedIds, setSelectedIds] = useState(() => new Set());
  const [creatorMap, setCreatorMap] = useState({});
  const catalogs = useQuestionCatalogs(draft.subjectId, draft.chapterId);
  const load = useCallback(async () => {
    const version = ++requestVersion.current;
    setLoading(true);
    setError(null);
    try {
      const result = await (isOwnerView ? questionsApi.list : questionsApi.approved)({
        ...query,
        createdBy: isOwnerView ? creatorId : undefined,
        page,
        size: 10,
      });
      if (version === requestVersion.current) { setSelectedIds(new Set()); setData(normalizePage(result)); }
    } catch (reason) {
      if (version === requestVersion.current) setError(reason);
    } finally {
      if (version === requestVersion.current) setLoading(false);
    }
  }, [creatorId, isOwnerView, page, query]);
  useEffect(() => {
    const task = setTimeout(load, 0);
    return () => clearTimeout(task);
  }, [load]);
  useEffect(() => { Promise.allSettled([api.get("/api/v1/users/me"), api.get("/api/v1/users/me/chat-contacts")]).then(([me, contacts]) => { const values = []; if (me.status === "fulfilled") values.push(me.value); if (contacts.status === "fulfilled") values.push(...contacts.value); setCreatorMap(Object.fromEntries(values.filter(Boolean).map((person) => [person.userId || person.id, person]))); }); }, []);
  useEffect(() => { const refresh = (event) => { const type = event.detail?.type || event.detail?.eventType || ""; if (type.includes("QUESTION") || type.includes("AI_JOB")) load(); }; window.addEventListener("hau:realtime", refresh); return () => window.removeEventListener("hau:realtime", refresh); }, [load]);
  const toggleSelected = (id) => setSelectedIds((old) => { const next = new Set(old); next.has(id) ? next.delete(id) : next.add(id); return next; });
  const togglePage = () => setSelectedIds((old) => {
    const next = new Set(old); const ids = data.items.map((item) => item.id);
    const all = ids.length > 0 && ids.every((id) => next.has(id));
    ids.forEach((id) => all ? next.delete(id) : next.add(id)); return next;
  });
  async function bulkAction(action) {
    if (!selectedIds.size) return;
    const reason = action === "request-revision" || action === "reject" ? window.prompt("Lý do xử lý (không bắt buộc):", "") : null;
    setBusy(true);
    try { await questionsApi.bulk(action, [...selectedIds], reason); setSelectedIds(new Set()); await load(); }
    catch (reasonError) { setError(reasonError); }
    finally { setBusy(false); }
  }
  async function confirmAction() {
    if (busy) return;
    const { action, question } = confirmation;
    setBusy(true);
    try {
      await questionsApi[action](question.id);
      setConfirmation(null);
      await load();
    } catch (reason) {
      setConfirmation(null);
      setError(reason);
    } finally {
      setBusy(false);
    }
  }
  return (
    <section>
      <PageHeader
        title={isOwnerView ? "Câu hỏi của tôi" : "Ngân hàng câu hỏi"}
        description={
          isOwnerView
            ? "Theo dõi, chỉnh sửa và gửi duyệt câu hỏi do bạn tạo."
            : "Tra cứu ngân hàng câu hỏi trong phạm vi khoa được cấp."
        }
        actions={
          isOwnerView && (
            <Link to={routes.newQuestion}>
              <Button>Tạo câu hỏi</Button>
            </Link>
          )
        }
      />
      <QuestionFilters
        filters={draft}
        facultyLocked={Boolean(auth.facultyId)}
        catalogs={catalogs}
        onChange={setDraft}
        onSubmit={(event) => {
          event.preventDefault();
          setPage(0);
          setQuery(draft);
        }}
      />
      <div className="surface question-table-surface">
        {loading ? (
          <TableSkeleton rows={6} columns={7}/>
        ) : error ? (
          <div className="question-state request-error" role="alert">
            <strong>Không thể tải câu hỏi</strong>
            <span>
              {error.status === 403
                ? "Bạn không có quyền truy cập câu hỏi trong phạm vi này."
                : error.status === 401
                  ? "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
                  : error.message}
            </span>
            {error.correlationId && (
              <small>Mã hỗ trợ: {error.correlationId}</small>
            )}
            <Button variant="secondary" onClick={load}>
              Thử lại
            </Button>
          </div>
        ) : (
          <>
            {data.items.length > 0 && <div className="question-bulk-toolbar"><label><input type="checkbox" checked={data.items.every((item) => selectedIds.has(item.id))} onChange={togglePage} /> Chọn trang</label><span>{selectedIds.size} câu đã chọn</span>{selectedIds.size > 0 && (isOwnerView ? <Button onClick={() => bulkAction("submit")}>Gửi duyệt</Button> : auth.role === roles.SUBJECT_ADMIN ? <><Button onClick={() => bulkAction("approve")}>Phê duyệt</Button><Button onClick={() => bulkAction("request-revision")}>Yêu cầu chỉnh sửa</Button><Button variant="danger" onClick={() => bulkAction("reject")}>Từ chối</Button></> : null)}</div>}
            <QuestionTable
              questions={data.items}
              auth={auth}
              page={data.page}
              pageSize={10}
              creatorMap={creatorMap}
              selectedIds={selectedIds}
              onToggle={toggleSelected}
              onPreview={setPreview}
              onAction={(action, question) =>
                setConfirmation({ action, question })
              }
            />
            <QuestionPagination {...data} onChange={setPage} />
          </>
        )}
      </div>
      <Drawer
        open={Boolean(preview)}
        title="Xem trước câu hỏi"
        onClose={() => setPreview(null)}
      >
        <QuestionPreview question={preview} />
      </Drawer>
      <ConfirmDialog
        loading={busy}
        open={Boolean(confirmation)}
        danger={confirmation?.action === "archive"}
        title={
          confirmation?.action === "archive"
            ? "Lưu trữ câu hỏi?"
            : "Gửi câu hỏi xét duyệt?"
        }
        description={
          confirmation?.action === "archive"
            ? "Câu hỏi sẽ được chuyển qua action archive của Question Service."
            : "Question Service sẽ kiểm tra workflow trước khi chuyển sang chờ duyệt."
        }
        confirmLabel={
          confirmation?.action === "archive" ? "Lưu trữ" : "Gửi duyệt"
        }
        onClose={() => {
          if (!busy) setConfirmation(null);
        }}
        onConfirm={confirmAction}
      />
    </section>
  );
}
