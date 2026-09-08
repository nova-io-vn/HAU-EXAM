import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import {
  Button,
  DataTable,
  Input,
  Loading,
  Select,
} from "../../../components/ui";
import { PageHeader } from "../../../components/shared/PageHeader";
import { questionsApi } from "../api/questionsApi";
import { QuestionPagination } from "../components/QuestionPagination";
import { ReviewError } from "../components/ReviewError";
import { formatDateTime, normalizePage } from "../model/questionModel";

export function ReviewQueuePage() {
  const [params, setParams] = useSearchParams();
  return (
    <ReviewQueue
      key={params.toString()}
      params={params}
      setParams={setParams}
    />
  );
}

function ReviewQueue({ params, setParams }) {
  const keyword = params.get("keyword") || "";
  const source = ["AI", "MANUAL"].includes(params.get("source"))
    ? params.get("source")
    : "";
  const difficulty = ["EASY", "MEDIUM", "HARD"].includes(
    params.get("difficulty"),
  )
    ? params.get("difficulty")
    : "";
  const page = Math.max(0, Number.parseInt(params.get("page"), 10) || 0);
  const [draft, setDraft] = useState(keyword);
  const [state, setState] = useState({ loading: true });
  const [refresh, setRefresh] = useState(0);
  useEffect(() => {
    let active = true;
    questionsApi
      .list({
        status: "PENDING_REVIEW",
        keyword,
        source,
        difficulty,
        page,
        size: 10,
        sort: "createdAt,asc",
      })
      .then((data) => {
        if (active) setState({ data: normalizePage(data) });
      })
      .catch((error) => {
        if (active) setState({ error });
      });
    return () => {
      active = false;
    };
  }, [keyword, source, difficulty, page, refresh]);
  function query(values) {
    setState({ loading: true });
    setRefresh((value) => value + 1);
    setParams({ ...Object.fromEntries(params), ...values });
  }
  const queryString = params.toString();
  return (
    <section className="review-queue-page">
      <PageHeader
        title="Câu hỏi chờ duyệt"
        description="Xem xét câu hỏi thuộc phạm vi Khoa được phân công trước khi đưa vào ngân hàng."
        actions={
          <Button
            variant="secondary"
            onClick={() => {
              setState({ loading: true });
              setRefresh((value) => value + 1);
            }}
          >
            Làm mới
          </Button>
        }
      />
      <form
        className="review-filters"
        onSubmit={(event) => {
          event.preventDefault();
          query({ keyword: draft.trim(), page: "0" });
        }}
      >
        <Input
          label="Tìm kiếm"
          placeholder="Mã hoặc nội dung câu hỏi"
          value={draft}
          onChange={(event) => setDraft(event.target.value)}
        />
        <Select
          label="Nguồn"
          value={source}
          options={[
            { value: "", label: "Tất cả nguồn" },
            { value: "MANUAL", label: "Thủ công" },
            { value: "AI", label: "AI" },
          ]}
          onChange={(event) => query({ source: event.target.value, page: "0" })}
        />
        <Select
          label="Độ khó"
          value={difficulty}
          options={[
            { value: "", label: "Tất cả độ khó" },
            { value: "EASY", label: "Dễ" },
            { value: "MEDIUM", label: "Trung bình" },
            { value: "HARD", label: "Khó" },
          ]}
          onChange={(event) =>
            query({ difficulty: event.target.value, page: "0" })
          }
        />
        <Button type="submit">Áp dụng</Button>
      </form>
      <div className="surface question-table-surface">
        {state.loading ? (
          <div className="question-state">
            <Loading label="Đang tải hàng đợi xét duyệt" />
          </div>
        ) : state.error ? (
          <ReviewError
            error={state.error}
            onRetry={() => {
              setState({ loading: true });
              setRefresh((value) => value + 1);
            }}
          />
        ) : (
          <>
            <DataTable
              rows={state.data.items}
              emptyTitle="Không có câu hỏi đang chờ phê duyệt."
              columns={[
                {
                  key: "id",
                  header: "Mã câu hỏi",
                  render: (q) => (
                    <Link
                      className="mono"
                      to={
                        "/review/" +
                        q.id +
                        (queryString ? "?" + queryString : "")
                      }
                    >
                      {String(q.id).slice(0, 8)}
                    </Link>
                  ),
                },
                {
                  key: "content",
                  header: "Nội dung",
                  render: (q) => (
                    <Link
                      className="question-cell"
                      to={
                        "/review/" +
                        q.id +
                        (queryString ? "?" + queryString : "")
                      }
                    >
                      {q.content}
                    </Link>
                  ),
                },
                {
                  key: "taxonomy",
                  header: "Môn / Chương / Chủ đề",
                  render: (q) => (
                    <span>
                      {q.subjectName || q.subjectId || "—"}
                      <small>
                        {q.chapterName || q.chapterId || "—"} ·{" "}
                        {q.topicName || q.topicId || "—"}
                      </small>
                    </span>
                  ),
                },
                {
                  key: "createdBy",
                  header: "Tác giả",
                  render: (q) => (
                    <span className="mono">{q.createdBy || "—"}</span>
                  ),
                },
                {
                  key: "difficulty",
                  header: "Độ khó",
                  render: (q) => q.difficulty || "—",
                },
                {
                  key: "source",
                  header: "Nguồn",
                  render: (q) => (q.source === "AI" ? "AI" : "Thủ công"),
                },
                {
                  key: "createdAt",
                  header: "Gửi lúc",
                  render: (q) => formatDateTime(q.createdAt),
                },
                {
                  key: "action",
                  header: "Thao tác",
                  render: (q) => (
                    <Link
                      to={
                        "/review/" +
                        q.id +
                        (queryString ? "?" + queryString : "")
                      }
                    >
                      Xem xét
                    </Link>
                  ),
                },
              ]}
            />
            <QuestionPagination
              {...state.data}
              onChange={(next) => query({ page: String(next) })}
            />
            {page > 0 && state.data.items.length === 0 && (
              <div className="review-empty-actions">
                <p>Trang này không còn câu hỏi phù hợp.</p>
                <Button
                  variant="secondary"
                  onClick={() => query({ page: "0" })}
                >
                  Về trang đầu
                </Button>
              </div>
            )}
          </>
        )}
      </div>
    </section>
  );
}
