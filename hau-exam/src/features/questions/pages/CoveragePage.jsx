import { useEffect, useState } from "react";
import { PageHeader } from "../../../components/shared/PageHeader";
import { Icon, Loading, Select } from "../../../components/ui";
import { api } from "../../../services/api/client";
import { catalogApi } from "../api/catalogApi";
import "./coverage.css";

function percent(value) {
  return `${Number(value || 0).toLocaleString("vi-VN", { maximumFractionDigits: 1 })}%`;
}

function isApprovalEvent(event) {
  const type = event.detail?.type || event.detail?.eventType || "";
  return type.includes("QUESTION") && type.includes("APPROVED");
}

function DifficultyTarget({ item }) {
  const targetTotal = item.targetEasy + item.targetMedium + item.targetHard;
  return <div className="coverage-difficulty">
    <div className="difficulty-grid">
      <span>Dễ <b>{item.easyCount}{targetTotal > 0 ? ` / ${item.targetEasy}` : ""}</b></span>
      <span>Trung bình <b>{item.mediumCount}{targetTotal > 0 ? ` / ${item.targetMedium}` : ""}</b></span>
      <span>Khó <b>{item.hardCount}{targetTotal > 0 ? ` / ${item.targetHard}` : ""}</b></span>
    </div>
    {item.difficultyTargetStatus === "MISSING" && <p className="coverage-target-missing">
      Mục tiêu độ khó còn thiếu: {item.missingDifficultyTargets.join(" · ")}
    </p>}
    {item.difficultyTargetStatus === "SATISFIED" && <p className="coverage-target-complete">
      Đã đạt mục tiêu phân bố độ khó ({percent(item.difficultyTargetPercentage)}).
    </p>}
    {item.difficultyTargetStatus === "NOT_CONFIGURED" && <p className="coverage-target-unconfigured">
      Chưa cấu hình mục tiêu độ khó; số liệu trên chỉ là phân bố thực tế.
    </p>}
  </div>;
}

function Summary({ summary }) {
  return <span className="coverage-summary">
    <b>{percent(summary.coveragePercentage)}</b>
    <small>{summary.coveredKnowledgeItems} / {summary.totalKnowledgeItems} đơn vị kiến thức</small>
  </span>;
}

export function CoveragePage() {
  const [subjects, setSubjects] = useState([]);
  const [selected, setSelected] = useState("");
  const [data, setData] = useState(null);
  const [subjectsLoading, setSubjectsLoading] = useState(true);
  const [coverageLoading, setCoverageLoading] = useState(false);
  const [error, setError] = useState("");
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    let active = true;
    catalogApi.subjects()
      .then(values => {
        if (!active) return;
        const available = values || [];
        setSubjects(available);
        if (available.length) setCoverageLoading(true);
        setSelected(available[0]?.id || "");
      })
      .catch(requestError => { if (active) setError(requestError.message); })
      .finally(() => { if (active) setSubjectsLoading(false); });
    return () => { active = false; };
  }, []);

  useEffect(() => {
    if (!selected) return undefined;
    const controller = new AbortController();
    let active = true;
    const timer = window.setTimeout(() => {
      api.get(`/api/v1/coverage/subjects/${selected}`, { signal: controller.signal })
        .then(value => { if (active) setData(value); })
        .catch(requestError => { if (active) setError(requestError.message); })
        .finally(() => { if (active) setCoverageLoading(false); });
    }, 0);
    return () => {
      active = false;
      window.clearTimeout(timer);
      controller.abort();
    };
  }, [selected, refreshKey]);

  useEffect(() => {
    const refreshOnApproval = event => {
      if (selected && isApprovalEvent(event)) {
        setCoverageLoading(true);
        setRefreshKey(value => value + 1);
      }
    };
    window.addEventListener("hau:realtime", refreshOnApproval);
    return () => window.removeEventListener("hau:realtime", refreshOnApproval);
  }, [selected]);

  const selectSubject = event => {
    setData(null);
    setError("");
    setCoverageLoading(Boolean(event.target.value));
    setSelected(event.target.value);
  };

  const refresh = () => {
    setCoverageLoading(true);
    setRefreshKey(value => value + 1);
  };

  const noStructure = data?.overall.totalKnowledgeItems === 0;

  return <section className="coverage-page">
    <PageHeader title="Độ bao phủ kiến thức" description="Mức độ ngân hàng câu hỏi APPROVED bao phủ cấu trúc kiến thức của môn học." />
    <div className="surface coverage-controls">
      <Select label="Môn học" value={selected} onChange={selectSubject}
        options={subjects.map(subject => ({ value: subject.id, label: `${subject.code} - ${subject.name}` }))} />
      <button className="coverage-refresh" type="button" disabled={!selected || coverageLoading}
        onClick={refresh}>
        <Icon name="refresh" variant="primary" /> Làm mới
      </button>
      {data && <article className="coverage-overall">
        <span>Độ bao phủ kiến thức</span>
        <strong>{percent(data.overall.coveragePercentage)}</strong>
        <small>{data.overall.coveredKnowledgeItems} / {data.overall.totalKnowledgeItems} đơn vị kiến thức đã có câu hỏi được duyệt</small>
      </article>}
    </div>
    {error && <p className="request-error" role="alert">{error}</p>}
    {(subjectsLoading || coverageLoading) && <Loading label="Đang tính độ bao phủ" />}
    {!subjectsLoading && !selected && <div className="surface coverage-empty">Chưa có môn học trong phạm vi quản lý.</div>}
    {data && noStructure && <div className="surface coverage-empty">
      <Icon name="info" variant="primary" size={22} />
      <strong>Chưa có cấu trúc kiến thức</strong>
      <span>Hãy bổ sung Knowledge Item cho môn học trước khi đánh giá độ bao phủ.</span>
    </div>}
    {data && !noStructure && <>
      <section className="surface coverage-missing-panel" aria-labelledby="missing-knowledge-title">
        <header>
          <Icon name="info" variant={data.missingKnowledge.length ? "warning" : "success"} size={21} />
          <div><h2 id="missing-knowledge-title">Phần kiến thức còn thiếu câu hỏi</h2><p>{data.missingKnowledge.length
            ? `${data.missingKnowledge.length} đơn vị kiến thức chưa có câu hỏi APPROVED.`
            : "Tất cả đơn vị kiến thức đã có câu hỏi APPROVED."}</p></div>
        </header>
        {data.missingKnowledge.length > 0 && <ul>{data.missingKnowledge.map(item => <li key={item.knowledgeItemId}>
          <Icon name="info" variant="warning" />
          <span><strong>{item.knowledgeItemName}</strong><small>{item.chapterName} · {item.topicName}</small></span>
          <b>{item.approvedQuestionCount} câu</b>
        </li>)}</ul>}
      </section>
      <div className="coverage-tree">{data.chapters.map(chapter => <details className="surface coverage-node" key={chapter.chapterId} open>
        <summary><span><strong>{chapter.chapterCode} · {chapter.chapterName}</strong><small>{chapter.coverage.approvedQuestionCount} câu APPROVED</small></span><Summary summary={chapter.coverage} /></summary>
        <div>{chapter.topics.map(topic => <details className="coverage-topic" key={topic.topicId} open>
          <summary><span>{topic.topicCode} · {topic.topicName}</span><Summary summary={topic.coverage} /></summary>
          <div className="coverage-items">{topic.knowledgeItems.map(item => <article className={`coverage-item is-${item.coverageStatus.toLowerCase()}`} key={item.knowledgeItemId}>
            <header>
              <Icon name={item.coverageStatus === "COVERED" ? "check" : "info"} variant={item.coverageStatus === "COVERED" ? "success" : "warning"} size={20} />
              <span><strong>{item.knowledgeItemName}</strong><small>{item.knowledgeItemCode}</small></span>
              <b>{item.approvedQuestionCount} câu</b>
            </header>
            <DifficultyTarget item={item} />
          </article>)}{!topic.knowledgeItems.length && <p>Chưa có đơn vị kiến thức trong chủ đề này.</p>}</div>
        </details>)}</div>
      </details>)}</div>
    </>}
    {data?.legacyQuestionsWithoutKnowledgeItem > 0 && <p className="coverage-legacy" role="status">
      <Icon name="info" variant="warning" /> {data.legacyQuestionsWithoutKnowledgeItem} câu hỏi chưa được gắn đơn vị kiến thức và không được dùng để đánh dấu bao phủ.
    </p>}
    {data && <p className="coverage-formula">{data.formula}</p>}
  </section>;
}
