import { useEffect } from 'react';
import { EditorContent, useEditor } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import { Icon } from './Icon';

function ToolButton({ label, active = false, children, ...props }) {
  return <button type="button" className={active ? 'is-active' : ''} aria-label={label} title={label} {...props}>{children}</button>;
}

export function RichTextEditor({ value = '', onChange, disabled = false, id = 'rich-text-editor', ariaLabel, placeholder = '' }) {
  const editor = useEditor({
    extensions: [StarterKit],
    content: value,
    editable: !disabled,
    editorProps: { attributes: { ...(ariaLabel ? { 'aria-label': ariaLabel } : {}), role: 'textbox', 'data-placeholder': placeholder } },
    onUpdate: ({ editor: current }) => onChange?.(current.getHTML()),
  });
  useEffect(() => { if (editor && value !== editor.getHTML() && !editor.isFocused) editor.commands.setContent(value || ''); }, [editor, value]);
  useEffect(() => { editor?.setEditable(!disabled); }, [editor, disabled]);
  if (!editor) return <div className="rich-editor rich-editor-loading" aria-busy="true" />;
  return <div className="rich-editor" id={id}>
    <div className="rich-toolbar" role="toolbar" aria-label="Định dạng nội dung">
      <ToolButton label="In đậm" active={editor.isActive('bold')} disabled={disabled} onClick={() => editor.chain().focus().toggleBold().run()}><strong>B</strong></ToolButton>
      <ToolButton label="In nghiêng" active={editor.isActive('italic')} disabled={disabled} onClick={() => editor.chain().focus().toggleItalic().run()}><em>I</em></ToolButton>
      <ToolButton label="Gạch chân" active={editor.isActive('underline')} disabled={disabled} onClick={() => editor.chain().focus().toggleUnderline().run()}><u>U</u></ToolButton>
      <span className="rich-toolbar-separator" />
      <select title="Kiểu đoạn" aria-label="Kiểu đoạn" value={editor.isActive('heading', { level: 3 }) ? 'heading' : 'paragraph'} onChange={event => event.target.value === 'heading' ? editor.chain().focus().setHeading({ level: 3 }).run() : editor.chain().focus().setParagraph().run()} disabled={disabled}>
        <option value="paragraph">Đoạn văn</option><option value="heading">Tiêu đề</option>
      </select>
      <span className="rich-toolbar-separator" />
      <ToolButton label="Danh sách" active={editor.isActive('bulletList')} disabled={disabled} onClick={() => editor.chain().focus().toggleBulletList().run()}><Icon name="list" /></ToolButton>
      <ToolButton label="Danh sách đánh số" active={editor.isActive('orderedList')} disabled={disabled} onClick={() => editor.chain().focus().toggleOrderedList().run()}><Icon name="orderedList" /></ToolButton>
      <span className="rich-toolbar-separator" />
      <ToolButton label="Hoàn tác" disabled={disabled || !editor.can().undo()} onClick={() => editor.chain().focus().undo().run()}><Icon name="undo" /></ToolButton>
      <ToolButton label="Làm lại" disabled={disabled || !editor.can().redo()} onClick={() => editor.chain().focus().redo().run()}><Icon name="redo" /></ToolButton>
    </div>
    <EditorContent editor={editor} className="rich-content" />
  </div>;
}
