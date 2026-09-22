import { useEffect } from 'react';
import { EditorContent, useEditor } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Underline from '@tiptap/extension-underline';
import { TextStyle } from '@tiptap/extension-text-style';

export function RichTextEditor({ value = '', onChange, disabled = false, id = 'rich-text-editor', ariaLabel }) {
  const editor = useEditor({ extensions: [StarterKit, Underline, TextStyle], content: value, editable: !disabled, editorProps: ariaLabel ? { attributes: { 'aria-label': ariaLabel, role: 'textbox' } } : undefined, onUpdate: ({ editor: current }) => onChange?.(current.getHTML()) });
  useEffect(() => { if (editor && value !== editor.getHTML() && !editor.isFocused) editor.commands.setContent(value || ''); }, [editor, value]);
  useEffect(() => { editor?.setEditable(!disabled); }, [editor, disabled]);
  if (!editor) return <div className="rich-editor rich-editor-loading" aria-busy="true" />;
  return <div className="rich-editor" id={id}>
    <div className="rich-toolbar" role="toolbar" aria-label="Định dạng nội dung">
      <button type="button" onClick={() => editor.chain().focus().toggleBold().run()} className={editor.isActive('bold') ? 'is-active' : ''} disabled={disabled} aria-label="In đậm"><strong>B</strong></button>
      <button type="button" onClick={() => editor.chain().focus().toggleItalic().run()} className={editor.isActive('italic') ? 'is-active' : ''} disabled={disabled} aria-label="In nghiêng"><em>I</em></button>
      <button type="button" onClick={() => editor.chain().focus().toggleUnderline().run()} className={editor.isActive('underline') ? 'is-active' : ''} disabled={disabled} aria-label="Gạch chân"><u>U</u></button>
      <span className="rich-toolbar-separator" />
      <select aria-label="Kiểu đoạn" value={editor.isActive('heading', { level: 3 }) ? 'heading' : 'paragraph'} onChange={event => event.target.value === 'heading' ? editor.chain().focus().toggleHeading({ level: 3 }).run() : editor.chain().focus().setParagraph().run()} disabled={disabled}><option value="paragraph">Đoạn văn</option><option value="heading">Tiêu đề</option></select>
      <select aria-label="Cỡ chữ" defaultValue="medium" onChange={event => editor.chain().focus().setMark('textStyle', { fontSize: { small: '0.9em', medium: '1em', large: '1.2em' }[event.target.value] }).run()} disabled={disabled}><option value="small">Nhỏ</option><option value="medium">Vừa</option><option value="large">Lớn</option></select>
      <span className="rich-toolbar-separator" />
      <button type="button" onClick={() => editor.chain().focus().toggleBulletList().run()} className={editor.isActive('bulletList') ? 'is-active' : ''} disabled={disabled}>• Danh sách</button>
      <button type="button" onClick={() => editor.chain().focus().toggleOrderedList().run()} className={editor.isActive('orderedList') ? 'is-active' : ''} disabled={disabled}>1. Danh sách</button>
      <button type="button" onClick={() => editor.chain().focus().undo().run()} disabled={disabled || !editor.can().undo()} aria-label="Hoàn tác">↶</button>
      <button type="button" onClick={() => editor.chain().focus().redo().run()} disabled={disabled || !editor.can().redo()} aria-label="Làm lại">↷</button>
    </div>
    <EditorContent editor={editor} className="rich-content" />
  </div>;
}
