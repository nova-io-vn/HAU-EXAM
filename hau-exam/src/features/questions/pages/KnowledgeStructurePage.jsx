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
import { chaptersForSubject, topicsForChapter } from "../model/knowledgeModel";
import "./knowledge-structure.css";

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
        <header><strong>Môn học</strong></header>
        {subjects.length ? subjects.map((subject) => <div key={subject.id}>
          <button type="button" className={selectedSubject?.id === subject.id ? "tree-node selected" : "tree-node"} onClick={() => { if (selectedSubject?.id !== subject.id) selectSubject(subject); }}>
            <span className="mono">{subject.code}</span><span>{subject.name}</span>
          </button>
        </div>) : <p>Chưa có môn học.</p>}
      </aside>
      <section className="surface knowledge-detail">
        {error && <p className="request-error" role="alert">{error.message}</p>}
        {!selectedSubject ? <p>Chọn một môn học để xem cấu trúc.</p> : structureLoading ? <Loading label={`Đang tải ${selectedSubject.name}`} /> : <SubjectDetail subject={selectedSubject} chapters={chapters} topicsByChapter={topicsByChapter} totalTopics={totalTopics} questionCount={questionCount} assignedLecturers={assignedLecturers} facultyNames={facultyNames} canManage={canManage} assignBusy={assignBusy} selectedChapter={selectedChapter} selectedTopic={selectedTopic} onSelectChapter={selectChapter} onSelectTopic={(chapter, topic) => { selectChapter(chapter); setSelectedTopic(topic); }} onAddChapter={() => setDialog({ type: "chapter" })} onEditChapter={(item) => setDialog({ type: "chapter", item })} onAddTopic={(chapter) => { selectChapter(chapter); setDialog({ type: "topic" }); }} onEditTopic={(chapter, item) => { selectChapter(chapter); setDialog({ type: "topic", item }); }} onAssign={() => setAssignOpen(true)} onRemove={removeLecturer} />}
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

function SubjectDetail({ subject, chapters, topicsByChapter, totalTopics, questionCount, assignedLecturers, facultyNames, canManage, assignBusy, selectedChapter, selectedTopic, onSelectChapter, onSelectTopic, onAddChapter, onEditChapter, onAddTopic, onEditTopic, onAssign, onRemove }) {
  return <>
    <header className="knowledge-subject-header"><div><span className="eyebrow">MÔN HỌC</span><h2>{subject.name}</h2></div>{canManage && <Button onClick={onAddChapter}>+ Chương</Button>}</header>
    <dl className="subject-meta"><div><dt>Mã môn</dt><dd className="mono">{subject.code}</dd></div><div><dt>Đơn vị quản lý</dt><dd>{facultyNames.get(subject.managingFacultyId) || subject.managingFacultyId}</dd></div></dl>
    <section className="subject-scope"><strong>Phạm vi áp dụng</strong><div className="faculty-chips">{subject.participatingFacultyIds?.map((facultyId) => <span className="scope-chip" key={facultyId}>{facultyNames.get(facultyId) || facultyId}</span>)}</div></section>
    <div className="knowledge-stats"><span><strong>{chapters.length}</strong> chương</span><span><strong>{totalTopics}</strong> chủ đề</span>{questionCount !== null && <span><strong>{questionCount}</strong> câu hỏi</span>}</div>
    <section className="subject-structure knowledge-subject-structure">
      <header><div><strong>CẤU TRÚC KIẾN THỨC</strong><p>Chương và các mục con của môn học được hiển thị tại đây.</p></div></header>
      <div className="knowledge-chapter-list">{chapters.length ? chapters.map((chapter, chapterIndex) => {
        const chapterNumber = Number(chapter.ordinal) > 0 ? Number(chapter.ordinal) : chapterIndex + 1;
        const topics = topicsByChapter[chapter.id] || [];
        return <article className={selectedChapter?.id === chapter.id ? "knowledge-chapter selected" : "knowledge-chapter"} key={chapter.id}>
          <div className="knowledge-chapter-row">
            <button type="button" className="knowledge-structure-select" onClick={() => onSelectChapter(chapter)}><span className="knowledge-order">Chương {chapterNumber}</span><span><strong>{chapter.name}</strong><small className="mono">{chapter.code}</small></span></button>
            {canManage && <div className="knowledge-structure-actions"><Button variant="ghost" onClick={() => onEditChapter(chapter)}>Sửa</Button><Button variant="ghost" onClick={() => onAddTopic(chapter)}>+ Chủ đề</Button></div>}
          </div>
          <div className="knowledge-topic-tree">{topics.length ? topics.map((topic, topicIndex) => <div className={selectedTopic?.id === topic.id ? "knowledge-topic-row selected" : "knowledge-topic-row"} key={topic.id}>
            <button type="button" onClick={() => onSelectTopic(chapter, topic)}><span className="knowledge-order">{chapterNumber}.{topicIndex + 1}</span><span>{topic.name}</span></button>
            {canManage && <Button variant="ghost" onClick={() => onEditTopic(chapter, topic)}>Sửa</Button>}
          </div>) : <p>Chưa có chủ đề trong chương này.</p>}</div>
        </article>;
      }) : <p>Chưa có chương.</p>}</div>
    </section>
    <section className="knowledge-lecturers"><header><strong>GIẢNG VIÊN PHỤ TRÁCH</strong>{canManage && <Button variant="ghost" onClick={onAssign}>+ Phân công giảng viên</Button>}</header>{assignedLecturers.length ? assignedLecturers.map((person) => <div className="knowledge-lecturer" key={person.userId}><Avatar user={person} /><span><strong>{formatAcademicName(person)}</strong><small>{person.lecturerCode} · {facultyNames.get(person.facultyId) || person.facultyId}</small></span>{canManage && <Button variant="ghost" loading={assignBusy === person.userId} onClick={() => void onRemove(person.userId)}>Xóa phân công</Button>}</div>) : <p>Chưa có giảng viên được phân công.</p>}</section>
  </>;
}
