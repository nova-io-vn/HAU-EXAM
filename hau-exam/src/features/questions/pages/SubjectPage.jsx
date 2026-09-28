import { useCallback, useEffect, useMemo, useState } from "react";
import { Button, DataTable, Dialog, Input, Loading, Select } from "../../../components/ui";
import { PageHeader } from "../../../components/shared/PageHeader";
import { useAuth } from "../../auth/hooks/useAuth";
import { toast } from "../../notifications/store/notificationStore";
import { facultiesApi } from "../../users/api/facultiesApi";
import { normalizePage } from "../../users/model/userModel";
import { catalogApi } from "../api/catalogApi";

export function SubjectPage() {
  const { facultyId, currentUser, role } = useAuth();
  const [items, setItems] = useState([]);
  const [faculties, setFaculties] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [editing, setEditing] = useState(null);
  const [scopeDraft, setScopeDraft] = useState([]);
  const [keyword, setKeyword] = useState("");
  const faculty = currentUser?.facultyName || currentUser?.faculty?.name || facultyId || "Chưa phân công";
  const isSystemAdmin = role === "SYSTEM_ADMIN";

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try { setItems(await catalogApi.subjects()); }
    catch (reason) { setError(reason); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { const timer = setTimeout(load, 0); return () => clearTimeout(timer); }, [load]);
  useEffect(() => {
    facultiesApi.publicList().then((result) => setFaculties(normalizePage(result).items)).catch(() => setFaculties([]));
  }, []);

  const facultyNames = useMemo(() => new Map(faculties.map((item) => [item.code, item.name])), [faculties]);
  const filtered = useMemo(() => items.filter((item) => `${item.code || ""} ${item.name || ""}`.toLowerCase().includes(keyword.toLowerCase().trim())), [items, keyword]);

  function openEditor(item = {}) {
    setEditing(item);
    setScopeDraft(item.participatingFacultyIds || (facultyId ? [facultyId] : []));
  }

  async function save(event) {
    event.preventDefault();
    const values = Object.fromEntries(new FormData(event.currentTarget));
    const managingFacultyId = isSystemAdmin ? values.managingFacultyId : editing.managingFacultyId;
    const body = {
      code: values.code,
      name: values.name,
      managingFacultyId,
      participatingFacultyIds: isSystemAdmin
        ? [...new Set([managingFacultyId, ...scopeDraft].filter(Boolean))]
        : editing.participatingFacultyIds,
    };
    try {
      if (editing.id) await catalogApi.updateSubject(editing.id, body);
      else await catalogApi.createSubject(body);
      toast.success(editing.id ? "Đã lưu thay đổi môn học." : "Đã tạo môn học.");
      setEditing(null);
      await load();
    } catch (reason) {
      setError(reason);
      toast.error(reason.message, { title: "Không thể lưu môn học" });
    }
  }

  return <section className="subject-admin-page">
    <PageHeader title="Môn học" description={isSystemAdmin ? "Cấu hình đơn vị quản lý và phạm vi áp dụng của môn học dùng chung." : `Quản lý môn học do Khoa ${faculty} phụ trách.`} actions={<Button onClick={() => openEditor()}>Thêm môn học</Button>} />
    <div className="subject-toolbar"><Input label="Tìm môn học" placeholder="Mã hoặc tên môn học" value={keyword} onChange={(event) => setKeyword(event.target.value)} /><span className="scope-chip">{isSystemAdmin ? "Phạm vi: toàn trường" : `Phạm vi: ${faculty}`}</span></div>
    <div className="surface subject-table-surface">{loading ? <Loading label="Đang tải môn học" /> : error ? <div className="request-error" role="alert"><strong>Không thể tải môn học</strong><p>{error.message}</p><Button variant="secondary" onClick={load}>Thử lại</Button></div> : <DataTable rows={filtered} emptyTitle="Chưa có môn học." columns={[
      { key: "code", header: "Mã môn học", render: (item) => <span className="mono">{item.code}</span> },
      { key: "name", header: "Tên môn học" },
      { key: "managingFacultyId", header: "Đơn vị quản lý", render: (item) => facultyNames.get(item.managingFacultyId) || item.managingFacultyId || "—" },
      { key: "participatingFacultyIds", header: "Phạm vi áp dụng", render: (item) => `${item.participatingFacultyIds?.length || 0} khoa` },
      { key: "createdAt", header: "Ngày tạo", render: (item) => item.createdAt ? new Intl.DateTimeFormat("vi-VN", { dateStyle: "short" }).format(new Date(item.createdAt)) : "—" },
      { key: "actions", header: "Thao tác", render: (item) => (isSystemAdmin || item.managingFacultyId === facultyId) ? <Button variant="ghost" onClick={() => openEditor(item)}>Sửa</Button> : <span className="muted">Chỉ xem</span> },
    ]} />}</div>
    <Dialog open={Boolean(editing)} title={editing?.id ? "Chỉnh sửa môn học" : "Thêm môn học"} onClose={() => setEditing(null)}>
      <form onSubmit={save} className="subject-form">
        <Input name="code" label="Mã môn học" required defaultValue={editing?.code || ""} />
        <Input name="name" label="Tên môn học" required defaultValue={editing?.name || ""} />
        {isSystemAdmin ? <>
          <Select name="managingFacultyId" label="Đơn vị quản lý" required value={editing?.managingFacultyId || ""} onChange={(event) => { const owner = event.target.value; setEditing({ ...editing, managingFacultyId: owner }); setScopeDraft((current) => [...new Set([owner, ...current].filter(Boolean))]); }} options={[{ value: "", label: "Chọn đơn vị quản lý" }, ...faculties.map((item) => ({ value: item.code, label: `${item.code} - ${item.name}` }))]} />
          <div className="faculty-scope-picker"><strong>Phạm vi áp dụng</strong><label><input type="checkbox" checked={faculties.length > 0 && faculties.every((item) => scopeDraft.includes(item.code))} onChange={(event) => setScopeDraft(event.target.checked ? faculties.map((item) => item.code) : editing?.managingFacultyId ? [editing.managingFacultyId] : [])} /> Chọn tất cả</label>{faculties.map((item) => <label key={item.code}><input type="checkbox" checked={scopeDraft.includes(item.code)} disabled={item.code === editing?.managingFacultyId} onChange={(event) => setScopeDraft((current) => event.target.checked ? [...new Set([...current, item.code])] : current.filter((code) => code !== item.code))} /> {item.code} - {item.name}</label>)}</div>
        </> : <><Input label="Đơn vị quản lý" value={faculty} disabled /><p className="form-note">Chỉ quản trị hệ thống được thay đổi đơn vị quản lý và phạm vi áp dụng.</p></>}
        <div className="form-actions"><Button type="button" variant="secondary" onClick={() => setEditing(null)}>Hủy</Button><Button type="submit">Lưu môn học</Button></div>
      </form>
    </Dialog>
  </section>;
}
