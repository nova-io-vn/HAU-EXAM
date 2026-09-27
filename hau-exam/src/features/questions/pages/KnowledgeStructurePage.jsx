import { useCallback, useEffect, useMemo, useState } from "react";
import { Button, Dialog, Input, Loading } from "../../../components/ui";
import { PageHeader } from "../../../components/shared/PageHeader";
import { catalogApi } from "../api/catalogApi";
import { toast } from "../../notifications/store/notificationStore";
import { api } from "../../../services/api/client";

export function KnowledgeStructurePage() {
  const [subjects, setSubjects] = useState([]);
  const [selected, setSelected] = useState(null);
  const [chapters, setChapters] = useState([]);
  const [chapter, setChapter] = useState(null);
  const [topics, setTopics] = useState([]);
  const [topic, setTopic] = useState(null);
  const [lecturerIds, setLecturerIds] = useState([]);
  const [contacts, setContacts] = useState([]);
  const [assignOpen, setAssignOpen] = useState(false);
  const [assignSearch, setAssignSearch] = useState("");
  const [dialog, setDialog] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const loadSubjects = useCallback(async () => {
    setLoading(true);
    try {
      const data = await catalogApi.subjects();
      setSubjects(data);
      if (!selected && data[0]) setSelected(data[0]);
    } catch (reason) {
      setError(reason);
    } finally {
      setLoading(false);
    }
  }, [selected]);
  useEffect(() => {
    const timer = setTimeout(loadSubjects, 0);
    return () => clearTimeout(timer);
  }, [loadSubjects]);
  useEffect(() => {
    if (!selected) return;
    let active = true;
    const timer = setTimeout(() => {
      catalogApi
        .chapters(selected.id)
        .then((data) => {
          if (active) {
            setChapter(null);
            setTopic(null);
            setTopics([]);
            setChapters(data);
          }
        })
        .catch((reason) => {
          if (active) setError(reason);
        });
    }, 0);
    return () => {
      active = false;
      clearTimeout(timer);
    };
  }, [selected]);
  useEffect(() => {
    if (!chapter) return;
    let active = true;
    catalogApi
      .topics(chapter.id)
      .then((data) => {
        if (active) { setTopics(data); setTopic(null); }
      })
      .catch((reason) => {
        if (active) setError(reason);
      });
    return () => {
      active = false;
    };
  }, [chapter]);
  useEffect(() => {
    if (!selected) return;
    Promise.all([catalogApi.lecturers(selected.id), api.get("/api/v1/users/me/chat-contacts")])
      .then(([ids, people]) => { setLecturerIds(ids || []); setContacts(people || []); })
      .catch(setError);
  }, [selected]);
  const assignedLecturers = useMemo(() => contacts.filter((person) => lecturerIds.includes(person.userId)), [contacts, lecturerIds]);
  const availableLecturers = useMemo(() => contacts.filter((person) => person.role === "USER" && !lecturerIds.includes(person.userId) && (!assignSearch || `${person.displayName} ${person.lecturerCode}`.toLowerCase().includes(assignSearch.toLowerCase()))), [contacts, lecturerIds, assignSearch]);
  async function assignLecturer(userId) { try { await catalogApi.assignLecturer(selected.id, userId); setLecturerIds(await catalogApi.lecturers(selected.id)); setAssignOpen(false); } catch (reason) { setError(reason); } }
  async function removeLecturer(userId) { try { await catalogApi.removeLecturer(selected.id, userId); setLecturerIds(await catalogApi.lecturers(selected.id)); } catch (reason) { setError(reason); } }

  async function save(event) {
    event.preventDefault();
    const values = Object.fromEntries(new FormData(event.currentTarget));
    try {
      if (dialog.type === "chapter") {
        const body = {
          subjectId: selected.id,
          code: values.code,
          name: values.name,
          ordinal: Number(values.ordinal || 0),
        };
        if (dialog.item) await catalogApi.updateChapter(dialog.item.id, body);
        else await catalogApi.createChapter(body);
        setChapters(await catalogApi.chapters(selected.id));
      } else {
        const body = {
          chapterId: chapter.id,
          code: values.code,
          name: values.name,
        };
        if (dialog.item) await catalogApi.updateTopic(dialog.item.id, body);
        else await catalogApi.createTopic(body);
        setTopics(await catalogApi.topics(chapter.id));
      }
      toast.success(dialog.item ? "Đã lưu thay đổi." : dialog.type === "chapter" ? "Đã tạo chương thành công." : "Đã tạo chủ đề thành công.");
      setDialog(null);
    } catch (reason) {
      setError(reason);
      toast.error(reason.message || "Vui lòng thử lại.", { title: "Không thể lưu cấu trúc kiến thức" });
    }
  }
  if (loading)
    return (
      <section>
        <PageHeader title="Cấu trúc kiến thức" />
        <Loading label="Đang tải cấu trúc kiến thức" />
      </section>
    );
  return (
    <section className="knowledge-page">
      <PageHeader
        title="Cấu trúc kiến thức"
        description="Quản lý cây Môn học → Chương → Chủ đề trong phạm vi Khoa được phân công."
      />
      <div className="knowledge-layout">
        <aside className="surface knowledge-tree">
          <header>
            <strong>Môn học</strong>
            <Button
              variant="ghost"
              onClick={() => setDialog({ type: "chapter" })}
              disabled={!selected}
            >
              + Chương
            </Button>
          </header>
          {subjects.length ? (
            subjects.map((subject) => (
              <button
                type="button"
                className={
                  selected?.id === subject.id
                    ? "tree-node selected"
                    : "tree-node"
                }
                key={subject.id}
                onClick={() => setSelected(subject)}
              >
                <span className="mono">{subject.code}</span>
                <span>{subject.name}</span>
              </button>
            ))
          ) : (
            <p>Chưa có môn học.</p>
          )}
          {selected && (
            <div className="tree-children">
              {chapters.map((item) => (
                <div key={item.id}>
                  <button
                    type="button"
                    className={
                      chapter?.id === item.id
                        ? "tree-node selected"
                        : "tree-node"
                    }
                    onClick={() => setChapter(item)}
                  >
                    <span>↳</span>
                    <span>{item.name}</span>
                  </button>
                  {chapter?.id === item.id &&
                    topics.map((topic) => (
                      <button
                        type="button"
                        className="tree-topic"
                        key={topic.id}
                        onClick={() => setTopic(topic)}
                      >
                        <span>•</span>
                        {topic.name}
                      </button>
                    ))}
                </div>
              ))}
            </div>
          )}
        </aside>
        <section className="surface knowledge-detail">
          {error && (
            <p className="request-error" role="alert">
              {error.message}
            </p>
          )}
          {!selected ? (
            <p>Chọn một Môn học để xem cấu trúc.</p>
          ) : topic ? (
            <>
              <span className="eyebrow">CHỦ ĐỀ</span>
              <h2>{topic.name}</h2>
              <p className="muted">Chương: {chapter.name} · Môn học: {selected.name}</p>
            </>
          ) : !chapter ? (
            <>
              <span className="eyebrow">MÔN HỌC</span>
              <h2>{selected.name}</h2>
              <p className="muted">Mã môn: {selected.code} · Khoa: {selected.facultyId || "—"}</p>
              <p>{chapters.length} chương · {topics.length} chủ đề đang tải</p>
              <div className="knowledge-lecturers"><header><strong>Giảng viên phụ trách</strong><Button variant="ghost" onClick={() => setAssignOpen(true)}>+ Phân công giảng viên</Button></header>{assignedLecturers.length ? assignedLecturers.map((person) => <div className="knowledge-lecturer" key={person.userId}><span className="human-avatar">{person.avatarUrl ? <img src={person.avatarUrl} alt="" /> : "GV"}</span><span><strong>{person.displayName}</strong><small>{person.lecturerCode} · Khoa {person.facultyId}</small></span><Button variant="ghost" onClick={() => void removeLecturer(person.userId)}>Xóa phân công</Button></div>) : <p>Chưa có giảng viên được phân công.</p>}</div>
              <p className="muted">
                Chọn Chương trong cây bên trái để quản lý Chủ đề.
              </p>
            </>
          ) : (
            <>
              <header className="knowledge-detail-header">
                <div>
                  <span className="eyebrow">CHƯƠNG</span>
                  <h2>{chapter.name}</h2>
                  <p className="muted">Chủ đề thuộc Chương đang chọn.</p>
                </div>
                <Button onClick={() => setDialog({ type: "topic" })}>
                  + Chủ đề
                </Button>
              </header>
              <div className="knowledge-topic-list">
                {topics.length ? (
                  topics.map((item) => (
                    <article key={item.id}>
                      <div>
                        <span className="mono">{item.code}</span>
                        <strong>{item.name}</strong>
                      </div>
                      <Button
                        variant="ghost"
                        onClick={() => setDialog({ type: "topic", item })}
                      >
                        Sửa
                      </Button>
                    </article>
                  ))
                ) : (
                  <p>Chưa có Chủ đề trong Chương này.</p>
                )}
              </div>
            </>
          )}
        </section>
      </div>
      <Dialog
        open={Boolean(dialog)}
        title={dialog?.type === "chapter" ? "Thêm Chương" : "Thêm Chủ đề"}
        onClose={() => setDialog(null)}
      >
        <form onSubmit={save} className="subject-form">
          <Input
            name="code"
            label="Mã"
            required
            defaultValue={dialog?.item?.code || ""}
          />
          <Input
            name="name"
            label="Tên"
            required
            defaultValue={dialog?.item?.name || ""}
          />
          {dialog?.type === "chapter" && (
            <Input
              name="ordinal"
              label="Thứ tự"
              type="number"
              defaultValue={dialog?.item?.ordinal || 0}
            />
          )}
          <div className="form-actions">
            <Button
              type="button"
              variant="secondary"
              onClick={() => setDialog(null)}
            >
              Hủy
            </Button>
            <Button type="submit">Lưu</Button>
          </div>
        </form>
      </Dialog>
      <Dialog open={assignOpen} title="Phân công giảng viên" onClose={() => setAssignOpen(false)}><Input value={assignSearch} onChange={(event) => setAssignSearch(event.target.value)} placeholder="Tìm tên hoặc mã giảng viên" />{availableLecturers.map((person) => <button className="tree-node" type="button" key={person.userId} onClick={() => void assignLecturer(person.userId)}><span>{person.displayName}</span><small>{person.lecturerCode} · Khoa {person.facultyId}</small></button>)}{!availableLecturers.length && <p>Không tìm thấy giảng viên phù hợp.</p>}</Dialog>
    </section>
  );
}
