import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { PageHeader } from "../../../components/shared/PageHeader";
import { questionsApi } from "../api/questionsApi";
import { QuestionEditor } from "../components/QuestionEditor";
import {
  editorPayload,
  emptyQuestion,
  validateQuestion,
} from "../model/questionModel";

import { useAuth } from "../../auth/hooks/useAuth";
import { toast } from "../../notifications/store/notificationStore";

export function CreateQuestionPage() {
  const auth = useAuth();
  const [form, setForm] = useState(() => ({
    ...emptyQuestion(),
    facultyId: auth.facultyId || "",
  }));
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  async function submit(event) {
    event.preventDefault();
    if (saving) return;
    const message = validateQuestion(form);
    if (message) {
      setError(new Error(message));
      return;
    }
    setSaving(true);
    setError(null);
    try {
      const created = await questionsApi.create(editorPayload(form));
      toast.success("Đã tạo câu hỏi thành công.");
      navigate(`/questions/${created.id}`);
    } catch (reason) {
      setError(reason);
      toast.error(reason.message || "Vui lòng kiểm tra dữ liệu và thử lại.", { title: "Không thể tạo câu hỏi" });
    } finally {
      setSaving(false);
    }
  }
  return (
    <section>
      <PageHeader
        description="Tạo và hoàn thiện nội dung câu hỏi trắc nghiệm trước khi gửi duyệt."
      />
      <QuestionEditor
        form={form}
        onChange={setForm}
        onSubmit={submit}
        onCancel={() => navigate('/questions/mine')}
        saving={saving}
        error={error}
      />
    </section>
  );
}
