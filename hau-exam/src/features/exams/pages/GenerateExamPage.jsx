import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { Button, Icon, Input, Loading, Select, StatusBadge } from "../../../components/ui";
import { PageHeader } from "../../../components/shared/PageHeader";
import { questionsApi } from "../../questions/api/questionsApi";
import { formatDateTime } from "../../questions/model/questionModel";
import { toast } from "../../notifications/store/notificationStore";
import { useExamResource } from "../hooks/useExamResource";
import { ExamError } from "../components/ExamError";
import { examsApi } from "../api/examsApi";
import { uuidPattern } from "../model/matrixModel";

export function GenerateExamPage() {
  const [params] = useSearchParams(); const navigate = useNavigate();
  const { data, loading, error, reload } = useExamResource("matrices");
  const [form, setForm] = useState({ name: "", examCode: "", durationMinutes: 60, matrixId: params.get("matrixId") || "", templateId: "", startCode:101, endCode:105, shuffleQuestions:true, shuffleAnswers:true, allowQuestionReplacement:true });
  const [busy, setBusy] = useState(false); const [failure, setFailure] = useState(null); const lock = useRef(false);
  const [capacityState,setCapacityState]=useState({key:'',data:null,error:null});
  const startCode=Number(form.startCode),endCode=Number(form.endCode);const versionCount=Number.isInteger(startCode)&&Number.isInteger(endCode)&&endCode>=startCode?endCode-startCode+1:0;
  const capacityKey=form.matrixId&&versionCount?`${form.matrixId}:${startCode}:${endCode}`:'';const capacity=capacityState.key===capacityKey?{loading:false,...capacityState}:{loading:Boolean(capacityKey),data:null,error:null};
  useEffect(()=>{let active=true;if(capacityKey)examsApi.capacity(form.matrixId,startCode,endCode).then(data=>{if(active)setCapacityState({key:capacityKey,data,error:null})}).catch(error=>{if(active)setCapacityState({key:capacityKey,data:null,error})});return()=>{active=false}},[capacityKey,form.matrixId,startCode,endCode]);
  async function generate(event) {
    event.preventDefault(); if (lock.current) return; setFailure(null);
    if (form.templateId && !uuidPattern.test(form.templateId)) { setFailure(new Error("Template ID phải có định dạng UUID.")); return; }
    lock.current = true; setBusy(true);
    try {
      const result = await examsApi.generate({ ...form, name: form.name.trim(), examCode: form.examCode.trim(), durationMinutes: Number(form.durationMinutes), startCode, endCode, templateId: form.templateId || null });
      toast.success("Đã tạo đề thi thành công.", { actionUrl: `/exams/${result.id}`, actionLabel: "Xem đề →" });
      navigate(`/exams/${result.id}`, { replace: true });
    } catch (reason) { setFailure(reason); toast.error(reason.message || "Vui lòng kiểm tra dữ liệu và thử lại.", { title: "Không thể tạo đề thi" }); }
    finally { lock.current = false; setBusy(false); }
  }
  const selected = data?.find(matrix => matrix.id === form.matrixId);
  const update = field => event => setForm(current => ({ ...current, [field]: event.target.type==='checkbox'?event.target.checked:event.target.value }));
  return <section><PageHeader title="Tạo đề thi" description="Sinh và lưu đề thi từ ngân hàng câu hỏi đã duyệt theo ma trận." />
    {loading ? <Loading label="Đang tải ma trận" /> : error ? <ExamError error={error} onRetry={reload} /> : <form className="editor-section exam-generation" onSubmit={generate}><fieldset disabled={busy}>
      <div className="exam-form-grid"><Input label="Tên đề thi" required value={form.name} onChange={update("name")} placeholder="VD: Kiểm tra giữa kỳ - Lập trình Java" /><Input label="Mã đề" required maxLength="50" value={form.examCode} onChange={update("examCode")} placeholder="VD: JAVA-001" /><Input label="Thời lượng (phút)" required type="number" min="1" max="600" value={form.durationMinutes} onChange={update("durationMinutes")} placeholder="VD: 60" /></div>
      <Select label="Ma trận đề" required value={form.matrixId} options={[{ value: "", label: "Chọn ma trận đề" }, ...data.map(matrix => ({ value: matrix.id, label: `${matrix.name} · ${matrix.totalQuestions} câu` }))]} onChange={update("matrixId")} />
      {selected && <p>Khoa: <strong>{selected.facultyId}</strong> · {selected.totalQuestions} câu · <Link to={`/exam-matrices/${selected.id}`}>Xem phân bố</Link></p>}
      {!data.length && <Link to="/exam-matrices/new">Tạo ma trận trước khi sinh đề thi</Link>}
      <Input label="Template ID (không bắt buộc)" value={form.templateId} onChange={event => setForm({ ...form, templateId: event.target.value.trim() })} placeholder="UUID của template đã có" />
      <section className="version-config" aria-labelledby="version-config-title"><h2 id="version-config-title">Cấu hình mã đề</h2><div className="version-range"><Input label="Mã đề bắt đầu" required type="number" min="1" value={form.startCode} onChange={update('startCode')}/><Input label="Mã đề kết thúc" required type="number" min="1" value={form.endCode} onChange={update('endCode')}/><div className="version-count"><span>Số mã đề</span><strong>{versionCount||'—'}</strong></div></div><div className="version-options"><label><input type="checkbox" checked={form.shuffleQuestions} onChange={update('shuffleQuestions')}/> Đảo thứ tự câu hỏi</label><label><input type="checkbox" checked={form.shuffleAnswers} onChange={update('shuffleAnswers')}/> Đảo thứ tự đáp án</label><label><input type="checkbox" checked={form.allowQuestionReplacement} onChange={update('allowQuestionReplacement')}/> Ưu tiên câu hỏi khác nhau giữa các mã đề</label></div><p className="matrix-note"><Icon name="info"/> Hệ thống luôn đảm bảo mỗi mã đề tuân thủ cùng ma trận đề.</p></section>
      {selected&&<section className="generation-preview"><h2>Xem trước khả năng tạo đề</h2><dl><div><dt>Mã đề</dt><dd>{startCode}–{endCode}</dd></div><div><dt>Số phiên bản</dt><dd>{versionCount}</dd></div><div><dt>Số câu/mã</dt><dd>{selected.totalQuestions}</dd></div></dl>{capacity.loading?<Loading label="Đang kiểm tra ngân hàng câu hỏi"/>:capacity.data?<><p className={capacity.data.canGenerate?'capacity-ok':'capacity-error'}><Icon name={capacity.data.canGenerate?'check':'x'}/> Ma trận: {capacity.data.canGenerate?'Hợp lệ, đủ câu hỏi APPROVED':'Không đủ câu hỏi theo bucket'}</p><p className={capacity.data.completeUniquenessPossible?'capacity-ok':'capacity-warning'}><Icon name={capacity.data.completeUniquenessPossible?'check':'info'}/> Độ trùng dự kiến: {capacity.data.estimatedReuseRate}%</p>{capacity.data.warning&&<p className="capacity-warning">{capacity.data.warning}</p>}{capacity.data.shortages?.length>0&&<ul className="capacity-shortages">{capacity.data.shortages.map(item=><li key={item.ruleId}>{item.difficulty} · Topic {item.topicId||'tất cả'}: cần {item.required}, có {item.available}, thiếu {item.missing}</li>)}</ul>}</>:capacity.error?<ExamError error={capacity.error}/>:null}</section>}
      <Button type="submit" loading={busy} disabled={busy || !selected || !form.name.trim() || !form.examCode.trim() || !capacity.data?.canGenerate}>Tạo {versionCount||''} mã đề</Button>
    </fieldset>{busy && <p role="status">Đang chọn câu hỏi và lưu đề thi…</p>}{failure && <ExamError error={failure} />}</form>}
  </section>;
}

export function ExamsPage() {
  const navigate = useNavigate(); const { data, loading, error, reload } = useExamResource("exams");
  const [subjects, setSubjects] = useState([]); const [term, setTerm] = useState(""); const [subjectId, setSubjectId] = useState(""); const [status, setStatus] = useState(""); const [exporting, setExporting] = useState("");
  useEffect(() => { questionsApi.subjects().then(setSubjects).catch(() => setSubjects([])); }, []);
  const subjectMap = useMemo(() => new Map(subjects.map(subject => [subject.id, subject.name])), [subjects]);
  const filtered = useMemo(() => (data || []).filter(exam => {
    const haystack = `${exam.name} ${exam.examCode} ${subjectMap.get(exam.subjectId) || ""}`.toLowerCase();
    return (!term.trim() || haystack.includes(term.trim().toLowerCase())) && (!subjectId || exam.subjectId === subjectId) && (!status || exam.status === status);
  }), [data, term, subjectId, status, subjectMap]);
  async function exportExam(exam) {
    const version = Math.max(...(exam.versions || []).map(item => item.versionCode ?? item.version), 1); setExporting(exam.id);
    try { const blob = await examsApi.downloadPdf(exam.id, version); const url = URL.createObjectURL(blob); const link = document.createElement("a"); link.href = url; link.download = `${exam.examCode}-v${version}.pdf`; link.click(); URL.revokeObjectURL(url); toast.success("Đã xuất đề thi PDF."); }
    catch (reason) { toast.error(reason.message || "Vui lòng thử lại.", { title: "Không thể xuất đề thi" }); }
    finally { setExporting(""); }
  }
  return <section><PageHeader title="Quản lý đề thi" description="Danh sách đề thi và lịch sử phiên bản đã lưu trong phạm vi khoa." actions={<Link className="button button-primary" to="/exams/generate">+ Tạo đề</Link>} />
    <div className="exam-filters"><Input label="Tìm kiếm" value={term} onChange={event => setTerm(event.target.value)} placeholder="Tìm theo tên, mã hoặc từ khóa..." /><Select label="Môn học" value={subjectId} onChange={event => setSubjectId(event.target.value)} options={[{ value: "", label: "Tất cả môn học" }, ...subjects.map(subject => ({ value: subject.id, label: subject.name }))]} /><Select label="Trạng thái" value={status} onChange={event => setStatus(event.target.value)} options={[{ value: "", label: "Tất cả trạng thái" }, { value: "ACTIVE", label: "Đang sử dụng" }]} /></div>
    {loading ? <div className="surface exam-list"><Loading label="Đang tải đề thi" /></div> : error ? <ExamError error={error} onRetry={reload} /> : filtered.length ? <div className="exam-card-grid">{filtered.map(exam => { const version = Math.max(...(exam.versions || []).map(item => item.versionCode ?? item.version), 0); const selectedVersion=exam.versions?.find(item => (item.versionCode??item.version) === version);const questions = selectedVersion?.questionCount??selectedVersion?.questions?.length??0; return <article className="surface exam-card" key={exam.id}><header><div><h2>{exam.name}</h2><span>Mã quản lý: <strong>{exam.examCode}</strong></span></div><StatusBadge status={exam.status} /></header><dl><div><dt>Môn học</dt><dd>{subjectMap.get(exam.subjectId) || "Đang cập nhật"}</dd></div><div><dt>Số câu/mã</dt><dd>{questions}</dd></div><div><dt>Thời gian</dt><dd>{exam.durationMinutes} phút</dd></div><div><dt>Mã đề</dt><dd>{exam.versionStartCode===exam.versionEndCode?version:`${exam.versionStartCode}–${exam.versionEndCode}`}</dd></div><div><dt>Ngày tạo</dt><dd>{formatDateTime(exam.createdAt)}</dd></div></dl><footer><Button variant="secondary" onClick={() => navigate(`/exams/${exam.id}`)}>Xem</Button><Button variant="secondary" loading={exporting === exam.id} onClick={() => void exportExam(exam)}>Xuất</Button></footer></article>; })}</div> : <div className="surface exam-empty"><h2>Chưa có đề thi phù hợp</h2><p>Thay đổi bộ lọc hoặc tạo đề thi từ một ma trận đã sẵn sàng.</p><Link className="button button-primary" to="/exams/generate">Tạo đề thi</Link></div>}
  </section>;
}
