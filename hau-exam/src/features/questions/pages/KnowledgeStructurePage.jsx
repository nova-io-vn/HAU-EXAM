import { useCallback, useEffect, useMemo, useState } from "react";
import { Avatar } from "../../../components/shared/Avatar";
import { PageHeader } from "../../../components/shared/PageHeader";
import { Button, Dialog, Input, Loading } from "../../../components/ui";
import { api } from "../../../services/api/client";
import { useAuth } from "../../auth/hooks/useAuth";
import { toast } from "../../notifications/store/notificationStore";
import { facultiesApi } from "../../users/api/facultiesApi";
import { formatAcademicName } from "../../users/model/academic";
import { normalizePage } from "../../users/model/userModel";
import { catalogApi } from "../api/catalogApi";
import { belongsToSelection, chaptersForSubject, topicsForChapter } from "../model/knowledgeModel";

export function KnowledgeStructurePage() {
  const auth = useAuth();
  const [subjects, setSubjects] = useState([]);
  const [selectedSubject, setSelectedSubject] = useState(null);
  const [chapters, setChapters] = useState([]);
  const [selectedChapter, setSelectedChapter] = useState(null);
  const [topicsByChapter, setTopicsByChapter] = useState({});
  const [selectedTopic, setSelectedTopic] = useState(null);
  const [assignedLecturers, setAssignedLecturers] = useState([]);
  const [eligibleLecturers, setEligibleLecturers] = useState([]);
  const [faculties, setFaculties] = useState([]);
  const [questionCount, setQuestionCount] = useState(null);
  const [assignOpen, setAssignOpen] = useState(false);
  const [assignSearch, setAssignSearch] = useState("");
  const [assignBusy, setAssignBusy] = useState("");
  const [dialog, setDialog] = useState(null);
  const [loading, setLoading] = useState(true);
  const [structureLoading, setStructureLoading] = useState(false);
  const [error, setError] = useState(null);

  const selectSubject = useCallback((subject) => {
    setSelectedSubject(subject);
    setSelectedChapter(null);
    setSelectedTopic(null);
    setChapters([]);
    setTopicsByChapter({});
    setAssignedLecturers([]);
    setEligibleLecturers([]);
    setQuestionCount(null);
    setStructureLoading(true);
    setAssignOpen(false);
    setAssignSearch("");
    setError(null);
  }, []);

  useEffect(() => {
    let active = true;
    Promise.all([catalogApi.subjects(), facultiesApi.publicList()])
      .then(([subjectData, facultyData]) => {
        if (!active) return;
        setSubjects(subjectData || []);
        setFaculties(normalizePage(facultyData).items);
        if (subjectData?.[0]) selectSubject(subjectData[0]);
      })
      .catch((reason) => active && setError(reason))
      .finally(() => active && setLoading(false));
    return () => { active = false; };
  }, [selectSubject]);

  useEffect(() => {
    if (!selectedSubject) return undefined;
    const controller = new AbortController();
    const subjectId = selectedSubject.id;
    let active = true;
    Promise.all([
      catalogApi.chapters(subjectId, { signal: controller.signal }),
      catalogApi.lecturers(subjectId),
      api.get(`/api/v1/questions?subjectId=${encodeURIComponent(subjectId)}&page=0&size=1`).catch(() => null),
    ]).then(async ([chapterData, lecturerData, questionData]) => {
      const scopedChapters = chaptersForSubject(chapterData, subjectId);
      const topicResults = await Promise.all(scopedChapters.map(async (item) => [
        item.id,
        topicsForChapter(await catalogApi.topics(item.id, { signal: controller.signal }), item.id),
      ]));
      if (!active) return;
      setChapters(scopedChapters);
      setTopicsByChapter(Object.fromEntries(topicResults));
      setAssignedLecturers(lecturerData || []);
      setQuestionCount(questionData?.totalElements ?? null);
    }).catch((reason) => {
      if (active && reason?.name !== "AbortError") setError(reason);
    }).finally(() => active && setStructureLoading(false));
    return () => {
      active = false;
      controller.abort();
    };
  }, [selectedSubject]);

  useEffect(() => {
    if (!assignOpen || !selectedSubject) return undefined;
    let active = true;
    const timer = setTimeout(() => {
      catalogApi.eligibleLecturers(selectedSubject.id, assignSearch)
        .then((data) => active && setEligibleLecturers(data || []))
        .catch((reason) => active && setError(reason));
    }, 250);
    return () => { active = false; clearTimeout(timer); };
  }, [assignOpen, assignSearch, selectedSubject]);

  const selectedTopics = selectedChapter ? topicsByChapter[selectedChapter.id] || [] : [];
  const totalTopics = useMemo(
    () => Object.values(topicsByChapter).reduce((total, items) => total + items.length, 0),
    [topicsByChapter],
  );
  const facultyNames = useMemo(
    () => new Map(faculties.map((faculty) => [faculty.code, faculty.name])),
    [faculties],
  );
  const canManage = selectedSubject && (
    auth.role === "SYSTEM_ADMIN" ||
    (auth.role === "SUBJECT_ADMIN" && auth.facultyId === selectedSubject.managingFacultyId)
  );

  function selectChapter(chapter) {
    if (chapter.subjectId !== selectedSubject?.id) return;
    setSelectedChapter(chapter);
    setSelectedTopic(null);
  }

  async function assignLecturer(userId) {
    if (!selectedSubject || assignedLecturers.some((person) => person.userId === userId)) return;
    setAssignBusy(userId);
    try {
      await catalogApi.assignLecturer(selectedSubject.id, userId);
      const assigned = eligibleLecturers.find((person) => person.userId === userId);
      if (assigned) setAssignedLecturers((current) => [...current, assigned]);
      setEligibleLecturers((current) => current.filter((person) => person.userId !== userId));
      toast.success("Đã phân công giảng viên.");
    } catch (reason) {
      setError(reason);
      toast.error(reason.message, { title: "Không thể phân công giảng viên" });
    } finally {
      setAssignBusy("");
    }
  }

  async function removeLecturer(userId) {
    if (!selectedSubject) return;
    setAssignBusy(userId);
    try {
      await catalogApi.removeLecturer(selectedSubject.id, userId);
      const removed = assignedLecturers.find((person) => person.userId === userId);
      setAssignedLecturers((current) => current.filter((person) => person.userId !== userId));
      if (assignOpen && removed) setEligibleLecturers((current) => [...current, removed]);
      toast.success("Đã xóa phân công giảng viên.");
    } catch (reason) {
      setError(reason);
      toast.error(reason.message, { title: "Không thể xóa phân công" });
    } finally {
      setAssignBusy("");
    }
  }

  async function save(event) {
    event.preventDefault();
    const values = Object.fromEntries(new FormData(event.currentTarget));
    try {
      if (dialog.type === "chapter") {
        const body = { subjectId: selectedSubject.id, code: values.code, name: values.name, ordinal: Number(values.ordinal || 0) };
        if (dialog.item) await catalogApi.updateChapter(dialog.item.id, body);
        else await catalogApi.createChapter(body);
        const next = chaptersForSubject(await catalogApi.chapters(selectedSubject.id), selectedSubject.id);
        setChapters(next);
      } else {
        const body = { chapterId: selectedChapter.id, code: values.code, name: values.name };
        if (dialog.item) await catalogApi.updateTopic(dialog.item.id, body);
        else await catalogApi.createTopic(body);
        const next = topicsForChapter(await catalogApi.topics(selectedChapter.id), selectedChapter.id);
        setTopicsByChapter((current) => ({ ...current, [selectedChapter.id]: next }));
      }
      toast.success(dialog.item ? "Đã lưu thay đổi." : dialog.type === "chapter" ? "Đã tạo chương." : "Đã tạo chủ đề.");
      setDialog(null);
    } catch (reason) {
      setError(reason);
      toast.error(reason.message, { title: "Không thể lưu cấu trúc kiến thức" });
    }
  }

  if (loading) return <section><PageHeader title="Cấu trúc kiến thức" /><Loading label="Đang tải cấu trúc kiến thức" /></section>;

  return <section className="knowledge-page">
    <PageHeader title="Cấu trúc kiến thức" description="Quản lý Môn học → Chương → Chủ đề theo đơn vị quản lý và phạm vi áp dụng." />
    <div className="knowledge-layout">
      <aside className="surface knowledge-tree">
        <header><strong>Môn học</strong>{canManage && <Button variant="ghost" onClick={() => setDialog({ type: "chapter" })}>+ Chương</Button>}</header>
        {subjects.length ? subjects.map((subject) => <div key={subject.id}>
          <button type="button" className={selectedSubject?.id === subject.id ? "tree-node selected" : "tree-node"} onClick={() => { if (selectedSubject?.id !== subject.id) selectSubject(subject); }}>
            <span className="mono">{subject.code}</span><span>{subject.name}</span>
          </button>
          {selectedSubject?.id === subject.id && <div className="tree-children">
            {chapters.map((item) => <div key={item.id}>
              <button type="button" className={selectedChapter?.id === item.id ? "tree-node selected" : "tree-node"} onClick={() => selectChapter(item)}><span>↳</span><span>{item.name}</span></button>
              {(topicsByChapter[item.id] || []).map((itemTopic) => <button type="button" className={selectedTopic?.id === itemTopic.id ? "tree-topic selected" : "tree-topic"} key={itemTopic.id} onClick={() => { selectChapter(item); setSelectedTopic(itemTopic); }}><span>•</span>{itemTopic.name}</button>)}
            </div>)}
          </div>}
        </div>) : <p>Chưa có môn học.</p>}
      </aside>
      <section className="surface knowledge-detail">
        {error && <p className="request-error" role="alert">{error.message}</p>}
        {!selectedSubject ? <p>Chọn một môn học để xem cấu trúc.</p> : structureLoading ? <Loading label={`Đang tải ${selectedSubject.name}`} /> : selectedTopic && belongsToSelection(selectedSubject, selectedChapter, selectedTopic) ? <>
          <span className="eyebrow">CHỦ ĐỀ</span><h2>{selectedTopic.name}</h2><p className="muted">Chương: {selectedChapter.name} · Môn học: {selectedSubject.name}</p>
        </> : selectedChapter && belongsToSelection(selectedSubject, selectedChapter, null) ? <>
          <header className="knowledge-detail-header"><div><span className="eyebrow">CHƯƠNG</span><h2>{selectedChapter.name}</h2><p className="muted">Môn học: {selectedSubject.name}</p></div>{canManage && <Button onClick={() => setDialog({ type: "topic" })}>+ Chủ đề</Button>}</header>
          <div className="knowledge-topic-list">{selectedTopics.length ? selectedTopics.map((item) => <article key={item.id}><div><span className="mono">{item.code}</span><strong>{item.name}</strong></div>{canManage && <Button variant="ghost" onClick={() => setDialog({ type: "topic", item })}>Sửa</Button>}</article>) : <p>Chưa có chủ đề trong chương này.</p>}</div>
        </> : <SubjectDetail subject={selectedSubject} chapters={chapters} topicsByChapter={topicsByChapter} totalTopics={totalTopics} questionCount={questionCount} assignedLecturers={assignedLecturers} facultyNames={facultyNames} canManage={canManage} assignBusy={assignBusy} onAssign={() => setAssignOpen(true)} onRemove={removeLecturer} />}
      </section>
    </div>
    <Dialog open={Boolean(dialog)} title={dialog?.type === "chapter" ? (dialog?.item ? "Sửa chương" : "Thêm chương") : (dialog?.item ? "Sửa chủ đề" : "Thêm chủ đề")} onClose={() => setDialog(null)}>
      <form onSubmit={save} className="subject-form"><Input name="code" label="Mã" required defaultValue={dialog?.item?.code || ""} /><Input name="name" label="Tên" required defaultValue={dialog?.item?.name || ""} />{dialog?.type === "chapter" && <Input name="ordinal" label="Thứ tự" type="number" defaultValue={dialog?.item?.ordinal || 0} />}<div className="form-actions"><Button type="button" variant="secondary" onClick={() => setDialog(null)}>Hủy</Button><Button type="submit">Lưu</Button></div></form>
    </Dialog>
    <Dialog open={assignOpen} title="Phân công giảng viên" subtitle="Có thể chọn nhiều giảng viên thuộc các khoa trong phạm vi áp dụng." onClose={() => setAssignOpen(false)}>
      <Input value={assignSearch} onChange={(event) => setAssignSearch(event.target.value)} placeholder="Tìm tên hoặc mã giảng viên" />
      <div className="lecturer-picker">{eligibleLecturers.map((person) => <button className="lecturer-option" type="button" disabled={Boolean(assignBusy)} key={person.userId} onClick={() => void assignLecturer(person.userId)}><Avatar user={person} /><span><strong>{formatAcademicName(person)}</strong><small>{person.lecturerCode} · {facultyNames.get(person.facultyId) || person.facultyId}</small></span><span>{assignBusy === person.userId ? "Đang gán…" : "+ Gán"}</span></button>)}{!eligibleLecturers.length && <p>Không tìm thấy giảng viên phù hợp hoặc tất cả đã được phân công.</p>}</div>
    </Dialog>
  </section>;
}

function SubjectDetail({ subject, chapters, topicsByChapter, totalTopics, questionCount, assignedLecturers, facultyNames, canManage, assignBusy, onAssign, onRemove }) {
  return <>
    <span className="eyebrow">MÔN HỌC</span><h2>{subject.name}</h2>
    <dl className="subject-meta"><div><dt>Mã môn</dt><dd className="mono">{subject.code}</dd></div><div><dt>Đơn vị quản lý</dt><dd>{facultyNames.get(subject.managingFacultyId) || subject.managingFacultyId}</dd></div></dl>
    <section className="subject-scope"><strong>Phạm vi áp dụng</strong><div className="faculty-chips">{subject.participatingFacultyIds?.map((facultyId) => <span className="scope-chip" key={facultyId}>{facultyNames.get(facultyId) || facultyId}</span>)}</div></section>
    <div className="knowledge-stats"><span><strong>{chapters.length}</strong> chương</span><span><strong>{totalTopics}</strong> chủ đề</span>{questionCount !== null && <span><strong>{questionCount}</strong> câu hỏi</span>}</div>
    <section className="knowledge-lecturers"><header><strong>GIẢNG VIÊN PHỤ TRÁCH</strong>{canManage && <Button variant="ghost" onClick={onAssign}>+ Phân công giảng viên</Button>}</header>{assignedLecturers.length ? assignedLecturers.map((person) => <div className="knowledge-lecturer" key={person.userId}><Avatar user={person} /><span><strong>{formatAcademicName(person)}</strong><small>{person.lecturerCode} · {facultyNames.get(person.facultyId) || person.facultyId}</small></span>{canManage && <Button variant="ghost" loading={assignBusy === person.userId} onClick={() => void onRemove(person.userId)}>Xóa phân công</Button>}</div>) : <p>Chưa có giảng viên được phân công.</p>}</section>
    <section className="subject-structure"><strong>CẤU TRÚC KIẾN THỨC</strong>{chapters.length ? chapters.map((chapter) => <article key={chapter.id}><h3>{chapter.name}</h3><ul>{(topicsByChapter[chapter.id] || []).map((topic) => <li key={topic.id}>{topic.name}</li>)}</ul></article>) : <p>Chưa có chương.</p>}</section>
  </>;
}
