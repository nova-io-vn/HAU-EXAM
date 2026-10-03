/* eslint-disable react-hooks/set-state-in-effect */
import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, ConfirmDialog, QuestionCardSkeleton, SkeletonText } from '../../../components/ui';
import { PageHeader } from '../../../components/shared/PageHeader';
import { useAuth } from '../../auth/hooks/useAuth';
import { questionsApi } from '../api/questionsApi';
import { QuestionPreview } from '../components/QuestionPreview';
import { canArchive, canEdit, canRestore, formatDateTime } from '../model/questionModel';
import { enumLabel, questionStatusLabels, questionTypeLabels, sourceLabels } from '../../../utils/enumLabels';

const actionLabels = {
  SUBMITTED: 'Đã gửi duyệt', RESUBMITTED: 'Đã gửi lại', APPROVED: 'Đã phê duyệt',
  REJECTED: 'Đã từ chối', REVISION_REQUESTED: 'Yêu cầu chỉnh sửa',
  ARCHIVED: 'Đã lưu trữ', RESTORED: 'Đã khôi phục',
};

export function QuestionDetailPage() {
  const { id } = useParams();
  const auth = useAuth();
  const [question, setQuestion] = useState(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(null);
  const [action, setAction] = useState(null);
  const load = useCallback(async () => {
    setLoading(true); setError(null);
    try { setQuestion(await questionsApi.get(id)); } catch (reason) { setError(reason); }
    finally { setLoading(false); }
  }, [id]);
  useEffect(() => { void load(); }, [load]);
  async function confirm() {
    if (busy) return;
    setBusy(true);
    try { await questionsApi[action](id); setAction(null); await load(); }
    catch (reason) { setAction(null); setError(reason); }
    finally { setBusy(false); }
  }
  if (loading) return <section><PageHeader title="Chi tiết câu hỏi" description="Đang chuẩn bị nội dung câu hỏi." /><div className="surface question-detail-skeleton"><QuestionCardSkeleton /><SkeletonText width="80%" height={140} /><SkeletonText width="60%" height={18} /></div></section>;
  if (error) return <div className="question-state request-error" role="alert"><strong>Không thể tải câu hỏi</strong><span>{error.message}</span><Button onClick={load}>Thử lại</Button></div>;
  return <section>
    <PageHeader title="Chi tiết câu hỏi" description="Xem nội dung, nguồn và trạng thái quy trình hiện tại." actions={<div className="page-actions">
      {auth.role === 'SUBJECT_ADMIN' && <Link to={`/review/${id}`} className="button button-primary">Mở workspace xét duyệt</Link>}
      {canEdit(question, auth) && <><Link to={`/questions/${id}/edit`}><Button variant="secondary">Chỉnh sửa</Button></Link><Button onClick={() => setAction('submit')}>Gửi duyệt</Button></>}
      {canArchive(question, auth) && <Button variant="secondary" onClick={() => setAction('archive')}>Lưu trữ</Button>}
      {canRestore(question, auth) && <Button onClick={() => setAction('restore')}>Khôi phục</Button>}
    </div>} />
    <div className="surface question-detail"><QuestionPreview question={question} /><aside><h3>Thông tin</h3><dl>
      <Info label="Người tạo" value={question.createdByName || 'Không xác định'} />
      <Info label="Khoa" value={question.facultyId} />
      <Info label="Môn học" value={question.subjectName || 'Không xác định'} />
      <Info label="Chương" value={question.chapterName || 'Không xác định'} />
      <Info label="Chủ đề" value={question.topicName || 'Không xác định'} />
      <Info label="Loại" value={enumLabel(question.type, questionTypeLabels)} />
      <Info label="Nguồn" value={enumLabel(question.source, sourceLabels)} />
    </dl></aside></div>
    <ApprovalHistory entries={question.reviewHistory || []} />
    <ConfirmDialog loading={busy} open={Boolean(action)} danger={action === 'archive'}
      title={action === 'archive' ? 'Lưu trữ câu hỏi?' : action === 'restore' ? 'Khôi phục câu hỏi?' : 'Gửi câu hỏi xét duyệt?'}
      description="Question Service sẽ xác thực trạng thái và quyền sở hữu trước khi thực hiện."
      confirmLabel={action === 'archive' ? 'Lưu trữ' : action === 'restore' ? 'Khôi phục' : 'Gửi duyệt'}
      onClose={() => { if (!busy) setAction(null); }} onConfirm={confirm} />
  </section>;
}

function Info({ label, value }) { return <div><dt>{label}</dt><dd>{value || '—'}</dd></div>; }

function ApprovalHistory({ entries }) {
  return <section className="surface review-history"><h2>Lịch sử phê duyệt</h2>
    {!entries.length ? <p>Chưa có hoạt động phê duyệt.</p> : <ol>{entries.map(item => <li key={item.id}>
      <div><span className="review-history-actor"><span className="human-avatar">{item.reviewerAvatar ? <img src={item.reviewerAvatar} alt="" /> : (item.reviewerName || 'KX').split(/\s+/).slice(-2).map(value => value[0]).join('').toUpperCase()}</span><span><strong>{item.reviewerName || 'Không xác định'}</strong><small>{actionLabels[item.action] || item.action}</small></span></span><time dateTime={item.timestamp}>{formatDateTime(item.timestamp)}</time></div>
      {(item.fromStatus || item.toStatus) && <p>{item.fromStatus ? enumLabel(item.fromStatus, questionStatusLabels) : '—'} → {item.toStatus ? enumLabel(item.toStatus, questionStatusLabels) : '—'}</p>}
      {item.comment && <blockquote>{item.comment}</blockquote>}
    </li>)}</ol>}
  </section>;
}
