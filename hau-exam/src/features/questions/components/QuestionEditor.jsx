import { useEffect, useId, useState } from 'react';
import { Button, ConfirmDialog, Drawer, Icon, Input, RichTextEditor, Select } from '../../../components/ui';
import { difficultyLabels, questionTypeLabels } from '../../../utils/enumLabels';
import { difficulties, optionLabels, questionTypes } from '../model/questionModel';
import { useQuestionCatalogs } from '../hooks/useQuestionCatalogs';
import { questionsApi } from '../api/questionsApi';
import { toast } from '../../notifications/store/notificationStore';
import { QuestionPreview } from './QuestionPreview';

const imageTypes = ['image/jpeg', 'image/png', 'image/webp'];

function ImagePicker({ label, value, onUpload, onRemove, disabled, uploading, compact = false }) {
  const inputId = `image-${useId().replaceAll(':', '')}`;
  const [localPreview, setLocalPreview] = useState('');
  useEffect(() => () => { if (localPreview) URL.revokeObjectURL(localPreview); }, [localPreview]);

  async function choose(event) {
    const file = event.target.files?.[0];
    event.target.value = '';
    if (!file) return;
    if (!imageTypes.includes(file.type)) return onUpload(null, new Error('Tệp ảnh phải là JPEG, PNG hoặc WEBP.'));
    if (file.size > 5 * 1024 * 1024) return onUpload(null, new Error('Ảnh phải nhỏ hơn 5 MB.'));
    if (localPreview) URL.revokeObjectURL(localPreview);
    const previewUrl = URL.createObjectURL(file);
    setLocalPreview(previewUrl);
    const uploaded = await onUpload(file);
    if (!uploaded) {
      URL.revokeObjectURL(previewUrl);
      setLocalPreview('');
    }
  }

  function remove() {
    if (localPreview) URL.revokeObjectURL(localPreview);
    setLocalPreview('');
    onRemove();
  }

  const preview = localPreview || value;
  return <div className={`image-picker-compact ${compact ? 'is-option-image' : 'is-question-image'} ${preview ? 'has-image' : ''}`}>
    <input className="sr-only" id={inputId} type="file" accept={imageTypes.join(',')} onChange={choose} disabled={disabled || uploading} />
    {!preview && <Button type="button" variant="secondary" className="image-picker-trigger" disabled={disabled || uploading} onClick={() => document.getElementById(inputId)?.click()} aria-label={label} title={label}><Icon name="image" />{compact ? <span className="sr-only">{label}</span> : <span>Thêm ảnh</span>}</Button>}
    {preview && <div className="image-picker-preview">
      <div className="image-preview-frame"><img src={preview} alt={`Xem trước ${label.toLowerCase()}`} />{uploading && <span className="image-upload-overlay">Đang tải ảnh…</span>}</div>
      <div className="image-preview-actions"><Button type="button" variant="secondary" disabled={disabled || uploading} onClick={() => document.getElementById(inputId)?.click()}>Thay ảnh</Button><Button type="button" variant="ghost" disabled={disabled || uploading} onClick={remove}>Xóa</Button></div>
    </div>}
  </div>;
}

export function QuestionEditor({ form, onChange, onSubmit, onCancel, saving, editing = false, readonly = false, error }) {
  const [preview, setPreview] = useState(false);
  const [uploadingTarget, setUploadingTarget] = useState('');
  const [uploadError, setUploadError] = useState('');
  const [deleteIndex, setDeleteIndex] = useState(null);
  const catalogs = useQuestionCatalogs(form.subjectId, form.chapterId);
  const setField = (field, value) => onChange({ ...form, [field]: value });
  const setOption = (index, field, value) => onChange({ ...form, options: form.options.map((option, i) => i === index ? { ...option, [field]: value } : option) });
  const setOptionImage = (index, url, storageKey = '') => onChange({ ...form, options: form.options.map((option, i) => i === index ? { ...option, imageUrl: url, storageKey } : option) });

  function changeType(type) {
    const options = type === 'TRUE_FALSE'
      ? [{ label: 'A', content: 'Đúng', imageUrl: '' }, { label: 'B', content: 'Sai', imageUrl: '' }]
      : form.type === 'TRUE_FALSE' ? optionLabels.map(label => ({ label, content: '', imageUrl: '' })) : form.options;
    onChange({ ...form, type, options, correctAnswer: type === 'MULTIPLE_CHOICE' ? [] : '' });
  }

  function choose(label, checked) {
    setField('correctAnswer', form.type === 'MULTIPLE_CHOICE'
      ? (checked ? [...(form.correctAnswer || []), label] : (form.correctAnswer || []).filter(value => value !== label))
      : label);
  }

  function removeOption(index) {
    const remaining = form.options.filter((_, i) => i !== index);
    const labelMap = new Map(remaining.map((option, i) => [option.label, String.fromCharCode(65 + i)]));
    const options = remaining.map((option, i) => ({ ...option, label: String.fromCharCode(65 + i) }));
    const correctAnswer = Array.isArray(form.correctAnswer)
      ? form.correctAnswer.filter(label => labelMap.has(label)).map(label => labelMap.get(label))
      : labelMap.get(form.correctAnswer) || '';
    onChange({ ...form, options, correctAnswer });
    setDeleteIndex(null);
  }

  async function uploadImage(file, kind = 'question', optionIndex = null, validationError) {
    if (validationError) {
      setUploadError(validationError.message);
      toast.error(validationError.message, { title: 'Không thể thêm ảnh' });
      return false;
    }
    if (!file) return false;
    const target = optionIndex === null ? 'question' : `option-${optionIndex}`;
    setUploadingTarget(target);
    setUploadError('');
    try {
      const result = await questionsApi.uploadImage(file, kind);
      const url = result.secureUrl || result.url;
      if (optionIndex === null) onChange({ ...form, imageUrl: url, storageKey: result.publicId });
      else setOptionImage(optionIndex, url, result.publicId);
      toast.success('Đã thêm ảnh.');
      return true;
    } catch (reason) {
      const message = reason?.message || 'Không thể tải ảnh lên. Vui lòng thử lại.';
      setUploadError(message);
      toast.error(message, { title: 'Không thể tải ảnh lên' });
      return false;
    } finally {
      setUploadingTarget('');
    }
  }

  return <>
    <form className="question-editor question-workspace" onSubmit={onSubmit}>
      {(catalogs.error || uploadError) && <div>{catalogs.error && <p role="alert" className="editor-error">{catalogs.error.message}</p>}{uploadError && <p role="alert" className="editor-error">{uploadError}</p>}</div>}
      <fieldset disabled={readonly || saving}>
        <section className="editor-section question-content-section" aria-labelledby="question-content-heading">
          <div className="workspace-section-heading"><div><span>01</span><h2 id="question-content-heading">Nội dung</h2></div></div>
          <label className="field question-content-field"><span className="sr-only">Nội dung câu hỏi</span><RichTextEditor id="question-content" ariaLabel="Nội dung câu hỏi" placeholder="Nhập nội dung câu hỏi..." value={form.content} onChange={value => setField('content', value)} disabled={readonly} /></label>
          <ImagePicker label="Thêm ảnh cho câu hỏi" value={form.imageUrl} onUpload={(file, reason) => uploadImage(file, 'question', null, reason)} onRemove={() => onChange({ ...form, imageUrl: '', storageKey: '' })} disabled={readonly || Boolean(uploadingTarget)} uploading={uploadingTarget === 'question'} />
        </section>

        <section className="editor-section classification-section" aria-labelledby="classification-heading">
          <div className="workspace-section-heading"><div><span>02</span><h2 id="classification-heading">Phân loại</h2></div>{editing && <p className="form-note">Môn học, chương và chủ đề được giữ nguyên sau khi tạo.</p>}</div>
          <div className="editor-grid classification-grid">
            <Select label="Môn học" disabled={editing} required value={form.subjectId} options={[{ value: '', label: 'Tìm theo mã hoặc tên môn học...' }, ...catalogs.subjects]} onChange={event => onChange({ ...form, subjectId: event.target.value, chapterId: '', topicId: '' })} />
            <Select label="Chương" required disabled={editing || !form.subjectId} value={form.chapterId || ''} options={[{ value: '', label: 'Chọn chương...' }, ...catalogs.chapters]} onChange={event => onChange({ ...form, chapterId: event.target.value, topicId: '' })} />
            <Select label="Chủ đề" disabled={editing || !form.chapterId} value={form.topicId || ''} options={[{ value: '', label: 'Chọn chủ đề...' }, ...catalogs.topics]} onChange={event => setField('topicId', event.target.value)} />
            <Select label="Độ khó" value={form.difficulty} options={difficulties.map(value => ({ value, label: difficultyLabels[value] || value }))} onChange={event => setField('difficulty', event.target.value)} />
            <Input label="Khoa" value={form.facultyId || ''} readOnly required />
            <Select label="Loại câu hỏi" value={form.type} options={questionTypes.map(value => ({ value, label: questionTypeLabels[value] || value }))} onChange={event => changeType(event.target.value)} />
          </div>
        </section>

        <section className="editor-section answer-section" aria-labelledby="answers-heading">
          <div className="workspace-section-heading answer-section-heading"><div><span>03</span><div><h2 id="answers-heading">Phương án trả lời</h2><p>Chọn nút tròn bên trái để đánh dấu đáp án đúng.</p></div></div>{form.type !== 'TRUE_FALSE' && <Button type="button" variant="secondary" onClick={() => { const label = String.fromCharCode(65 + form.options.length); onChange({ ...form, options: [...form.options, { label, content: '', imageUrl: '', storageKey: '' }] }); }}><Icon name="plus" />Thêm phương án</Button>}</div>
          <div className="option-editor compact-option-list">{form.options.map((option, index) => <article className="option-row" key={option.id || option.label}>
            <label className="correct-control" title={`Đánh dấu phương án ${option.label} là đáp án đúng`}><input aria-label={`Phương án ${option.label} là đáp án đúng`} type={form.type === 'MULTIPLE_CHOICE' ? 'checkbox' : 'radio'} name="correctAnswer" checked={Array.isArray(form.correctAnswer) ? form.correctAnswer.includes(option.label) : form.correctAnswer === option.label} onChange={event => choose(option.label, event.target.checked)} /><strong>{option.label}</strong></label>
            <label className="field option-content-field"><span className="sr-only">Nội dung phương án {option.label}</span><input required aria-label={`Nội dung phương án ${option.label}`} value={option.content} onChange={event => setOption(index, 'content', event.target.value)} placeholder={`Nhập nội dung phương án ${option.label}...`} /></label>
            <ImagePicker compact label={`Thêm ảnh cho phương án ${option.label}`} value={option.imageUrl} onUpload={(file, reason) => uploadImage(file, 'option', index, reason)} onRemove={() => setOptionImage(index, '', '')} disabled={readonly || Boolean(uploadingTarget)} uploading={uploadingTarget === `option-${index}`} />
            {form.type !== 'TRUE_FALSE' && form.options.length > 2 && <button type="button" className="option-delete-button" aria-label={`Xóa phương án ${option.label}`} title="Xóa phương án" onClick={() => setDeleteIndex(index)}><Icon name="trash" /></button>}
          </article>)}</div>
        </section>
      </fieldset>
      {error && <p className="editor-error" role="alert">{error.message}</p>}
      <footer className="editor-actions workspace-actions"><span className="workspace-save-state">Trạng thái: <strong>{editing ? 'Có thay đổi chưa lưu' : 'Chưa lưu'}</strong></span><div><Button type="button" variant="ghost" onClick={onCancel}>Hủy</Button><Button type="button" variant="secondary" onClick={() => setPreview(true)}>Xem trước</Button>{!readonly && <Button type="submit" loading={saving} disabled={Boolean(uploadingTarget)}>{editing ? 'Lưu thay đổi' : 'Tạo câu hỏi'}</Button>}{!readonly && form.status === 'NEED_REVISION' && <Button type="submit" name="action" value="resubmit" loading={saving} disabled={Boolean(uploadingTarget)}>Lưu và gửi lại</Button>}</div></footer>
    </form>
    <ConfirmDialog open={deleteIndex !== null} title="Xóa phương án?" description={deleteIndex !== null ? `Phương án ${form.options[deleteIndex]?.label} sẽ bị xóa khỏi câu hỏi.` : ''} confirmLabel="Xóa phương án" danger onClose={() => setDeleteIndex(null)} onConfirm={() => removeOption(deleteIndex)} />
    <Drawer open={preview} title="Xem trước câu hỏi" onClose={() => setPreview(false)}><QuestionPreview question={{ ...form, options: form.options.map(option => ({ ...option, correct: Array.isArray(form.correctAnswer) ? form.correctAnswer.includes(option.label) : form.correctAnswer === option.label })) }} /></Drawer>
  </>;
}
