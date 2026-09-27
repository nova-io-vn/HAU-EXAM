import { Link } from "react-router-dom";
import { Button, DataTable, StatusBadge } from "../../../components/ui";
import { formatDateTime, canEdit, canArchive } from "../model/questionModel";
import { sourceLabels } from "../../../utils/enumLabels";

const plain = (value) => String(value || "").replace(/<[^>]*>/g, " ").replace(/\s+/g, " ").trim();

export function QuestionTable({ questions, auth, onPreview, onAction, page = 0, pageSize = 10, selectedIds = new Set(), onToggle, creatorMap = {} }) {
  const columns = [
    { key: "select", header: "", render: (item) => <input type="checkbox" checked={selectedIds.has(item.id)} onChange={() => onToggle?.(item.id)} aria-label={`Chọn câu hỏi ${item.id}`} /> },
    { key: "stt", header: "STT", render: (_, index) => <span>{page * pageSize + index + 1}</span> },
    { key: "content", header: "Câu hỏi", render: (item) => <span className="question-cell">{plain(item.content)}</span> },
    { key: "subject", header: "Môn học", render: (item) => item.subjectName || item.subject?.name || "—" },
    { key: "chapter", header: "Chương / Chủ đề", render: (item) => <span>{item.chapterName || item.chapter?.name || "—"}<small>{item.topicName || ""}</small></span> },
    { key: "difficulty", header: "Độ khó", render: (item) => <StatusBadge status={item.difficulty} /> },
    { key: "status", header: "Trạng thái", render: (item) => <StatusBadge status={item.status} /> },
    { key: "source", header: "Nguồn", render: (item) => <span>{sourceLabels[item.source] || sourceLabels.MANUAL}</span> },
    { key: "creator", header: "Người tạo", render: (item) => { const person = creatorMap[item.createdBy]; return <span>{item.createdByName || person?.fullName || person?.displayName || person?.lecturerCode || "Không xác định"}{(person?.lecturerCode || item.lecturerCode) && <small>{person?.lecturerCode || item.lecturerCode}</small>}</span>; } },
    { key: "updatedAt", header: "Cập nhật", render: (item) => formatDateTime(item.updatedAt) },
    { key: "actions", header: "Thao tác", render: (item) => <div className="row-actions"><Button variant="ghost" onClick={() => onPreview(item)}>Xem trước</Button><Link to={`/questions/${item.id}`}>Chi tiết</Link>{canEdit(item, auth) && <><Link to={`/questions/${item.id}/edit`}>Sửa</Link><Button onClick={() => onAction("submit", item)}>Gửi duyệt</Button></>}{canArchive(item, auth) && <Button variant="ghost" onClick={() => onAction("archive", item)}>Lưu trữ</Button>}</div> },
  ];
  return <DataTable columns={columns} rows={questions} emptyTitle="Không tìm thấy câu hỏi" />;
}
