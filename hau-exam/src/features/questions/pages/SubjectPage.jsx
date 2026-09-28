import { useCallback, useEffect, useMemo, useState } from "react";
import { Button, DataTable, Dialog, Input, Loading } from "../../../components/ui";
import { PageHeader } from "../../../components/shared/PageHeader";
import { useAuth } from "../../auth/hooks/useAuth";
import { toast } from "../../notifications/store/notificationStore";
import { catalogApi } from "../api/catalogApi";

export function SubjectPage() {
  const { facultyId, currentUser } = useAuth();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [editing, setEditing] = useState(null);
  const [keyword, setKeyword] = useState("");
  const faculty = currentUser?.facultyName || currentUser?.faculty?.name || facultyId || "Chưa phân công";

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try { setItems(await catalogApi.subjects()); }
    catch (reason) { setError(reason); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { const timer = setTimeout(load, 0); return () => clearTimeout(timer); }, [load]);

  const filtered = useMemo(() => items.filter((item) => `${item.code || ""} ${item.name || ""}`.toLowerCase().includes(keyword.toLowerCase().trim())), [items, keyword]);

  async function save(event) {
    event.preventDefault();
    const values = Object.fromEntries(new FormData(event.currentTarget));
    const body = {
      code: values.code,
      name: values.name,
      managingFacultyId: editing?.managingFacultyId || facultyId,
      participatingFacultyIds: editing?.participatingFacultyIds || (facultyId ? [facultyId] : []),
    };
    try {
      if (editing?.id) await catalogApi.updateSubject(editing.id, body);
      else await catalogApi.createSubject(body);
      toast.success(editing?.id ? "Đã lưu thay đổi môn học." : "Đã tạo môn học.");
      setEditing(null);
      await load();
    } catch (reason) {
      setError(reason);
      toast.error(reason.message, { title: "Không thể lưu môn học" });
    }
  }

  return <section className="subject-admin-page">
    <PageHeader title="Môn học" description={`Quản lý môn học do Khoa ${faculty} phụ trách.`} actions={<Button onClick={() => setEditing({})}>Thêm môn học</Button>} />
    <div className="subject-toolbar"><Input label="Tìm môn học" placeholder="Mã hoặc tên môn học" value={keyword} onChange={(event) => setKeyword(event.target.value)} /><span className="scope-chip">Đơn vị quản lý: {faculty}</span></div>
    <div className="surface subject-table-surface">{loading ? <Loading label="Đang tải môn học" /> : error ? <div className="request-error" role="alert"><strong>Không thể tải môn học</strong><p>{error.message}</p><Button variant="secondary" onClick={load}>Thử lại</Button></div> : <DataTable rows={filtered} emptyTitle="Chưa có môn học." columns={[
      { key: "code", header: "Mã môn học", render: (item) => <span className="mono">{item.code}</span> },
      { key: "name", header: "Tên môn học" },
      { key: "managingFacultyId", header: "Đơn vị quản lý", render: (item) => item.managingFacultyId || "—" },
      { key: "createdAt", header: "Ngày tạo", render: (item) => item.createdAt ? new Intl.DateTimeFormat("vi-VN", { dateStyle: "short" }).format(new Date(item.createdAt)) : "—" },
      { key: "actions", header: "Thao tác", render: (item) => item.managingFacultyId === facultyId ? <Button variant="ghost" onClick={() => setEditing(item)}>Sửa</Button> : <span className="muted">Chỉ xem</span> },
    ]} />}</div>
    <Dialog open={Boolean(editing)} title={editing?.id ? "Chỉnh sửa môn học" : "Thêm môn học"} onClose={() => setEditing(null)}>
      <form onSubmit={save} className="subject-form">
        <Input name="code" label="Mã môn học" required defaultValue={editing?.code || ""} />
        <Input name="name" label="Tên môn học" required defaultValue={editing?.name || ""} />
        <Input label="Đơn vị quản lý" value={faculty} disabled />
        <div className="form-actions"><Button type="button" variant="secondary" onClick={() => setEditing(null)}>Hủy</Button><Button type="submit">Lưu môn học</Button></div>
      </form>
    </Dialog>
  </section>;
}
