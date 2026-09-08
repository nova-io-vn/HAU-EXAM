import DOMPurify from 'dompurify';
import { StatusBadge } from '../../../components/ui';
import { difficultyLabels, enumLabel, questionTypeLabels, sourceLabels } from '../../../utils/enumLabels';

export function QuestionPreview({ question }) {
  if (!question) return null;
  const content = DOMPurify.sanitize(question.content || '');
  return <article className="question-preview"><header><div><span className={`source-label source-${question.source || 'MANUAL'}`}>{enumLabel(question.source || 'MANUAL', sourceLabels)}</span>{question.status ? <StatusBadge status={question.status} /> : <span>Chưa lưu</span>}</div><p>{question.subjectName || question.subject?.name || question.subjectId || 'Chưa xác định môn học'} · {enumLabel(question.difficulty, difficultyLabels)} · {enumLabel(question.type, questionTypeLabels)}</p></header>{question.imageUrl && <img className="question-image" src={question.imageUrl} alt="Minh họa câu hỏi" />}<div className="question-preview-content" dangerouslySetInnerHTML={{ __html: content }} /><ol className="option-preview">{(question.options || []).map(option => <li key={option.id || option.label} className={option.correct ? 'is-correct' : ''}><strong>{option.label}</strong><span dangerouslySetInnerHTML={{ __html: DOMPurify.sanitize(option.content || '') }} />{option.correct && <small> · Đáp án đúng</small>}{option.imageUrl && <img src={option.imageUrl} alt={`Minh họa đáp án ${option.label}`} />}</li>)}</ol>{question.explanation && <section className="explanation"><strong>Giải thích</strong><p>{question.explanation}</p></section>}</article>;
}
