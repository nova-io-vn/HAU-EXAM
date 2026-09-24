import { useCallback, useEffect, useMemo, useState } from "react";
import { Button, DataTable, Dialog, Input, Select, StatusBadge } from "../../../components/ui";
import { PageHeader } from "../../../components/shared/PageHeader";
import { contactStatusLabels } from "../../../utils/enumLabels";
import { adminSupportApi } from "../api/adminSupportApi";

const statuses = Object.keys(contactStatusLabels);

export function ContactAdminPage() {
  const [data, setData] = useState(null);
  const [listError, setListError] = useState("");
  const [actionError, setActionError] = useState("");
  const [selected, setSelected] = useState(null);
  const [reply, setReply] = useState("");
  const [busy, setBusy] = useState(false);
  const [filters, setFilters] = useState({ keyword: "", status: "" });

  const load = useCallback(async () => {
    try {
      const result = await adminSupportApi.list({
        status: filters.status,
        page: 0,
        size: 20,
      });
      setData(result);
      setListError("");
    } catch (reason) {
      setListError(reason.message || "Không thể tải yêu cầu liên hệ.");
    }
  }, [filters.status]);

  useEffect(() => {
    const timer = setTimeout(() => void load(), 150);
    return () => clearTimeout(timer);
  }, [load]);

  const rows = useMemo(() => {
    const items = data?.content || data?.data?.content || data?.data || [];
    const keyword = filters.keyword.trim().toLocaleLowerCase("vi-VN");
    if (!keyword) return items;
    return items.filter((item) =>
      [item.name, item.email, item.subject]
        .filter(Boolean)
        .some((value) => value.toLocaleLowerCase("vi-VN").includes(keyword)),
    );
  }, [data, filters.keyword]);

  async function sendReply() {
    if (!selected || !reply.trim()) return;
    setBusy(true);
    setActionError("");
    try {
      await adminSupportApi.reply(selected.id, reply.trim());
      setSelected(null);
      setReply("");
      await load();
    } catch (reason) {
      setActionError(reason.message || "Không thể gửi phản hồi.");
    } finally {
      setBusy(false);
    }
  }

  async function setStatus(value) {
    if (!selected) return;
    setBusy(true);
    setActionError("");
    try {
      const updated = await adminSupportApi.status(selected.id, value);
      setSelected(updated || ((current) => ({ ...current, status: value })));
      setData((current) => {
        if (!current?.content) return current;
        return {
          ...current,
          content: current.content.map((item) =>
            item.id === selected.id ? { ...item, ...(updated || { status: value }) } : item,
          ),
        };
      });
    } catch (reason) {
      setActionError(reason.message || "Không thể cập nhật trạng thái.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <section>
      <PageHeader title="Yêu cầu liên hệ" description="Tiếp nhận và phản hồi yêu cầu hỗ trợ từ người dùng." />
      <div className="contact-toolbar">
        <Input label="Tìm kiếm" placeholder="Tên, email hoặc chủ đề" value={filters.keyword} onChange={(event) => setFilters((current) => ({ ...current, keyword: event.target.value }))} />
        <Select label="Trạng thái" value={filters.status} options={[{ value: "", label: "Tất cả trạng thái" }, ...statuses.map((value) => ({ value, label: contactStatusLabels[value] }))]} onChange={(event) => setFilters((current) => ({ ...current, status: event.target.value }))} />
      </div>
      <div className="surface admin-panel">
        {listError && <p className="editor-error" role="alert">{listError}</p>}
        {!data ? (
          <p>Đang tải dữ liệu…</p>
        ) : (
          <DataTable
            rows={rows}
            emptyTitle="Chưa có yêu cầu liên hệ."
            columns={[
              { key: "name", header: "Người gửi" },
              { key: "email", header: "Email" },
              { key: "subject", header: "Chủ đề" },
              { key: "status", header: "Trạng thái", render: (item) => <StatusBadge status={item.status} /> },
              { key: "createdAt", header: "Ngày gửi" },
              {
                key: "id",
                header: "Hành động",
                render: (item) => (
                  <Button
                    variant="secondary"
                    onClick={() => {
                      setSelected(item);
                      setReply("");
                      setActionError("");
                    }}
                  >
                    Xem chi tiết
                  </Button>
                ),
              },
            ]}
          />
        )}
      </div>
      <Dialog
        open={Boolean(selected)}
        title={selected?.subject || "Chi tiết yêu cầu"}
        subtitle={selected ? `${selected.name} · ${selected.email}` : ""}
        size="medium"
        onClose={() => {
          if (!busy) {
            setSelected(null);
            setActionError("");
          }
        }}
        footer={
          <>
            <Button variant="secondary" onClick={() => setSelected(null)} disabled={busy}>Đóng</Button>
            <Button loading={busy} disabled={!reply.trim() || busy} onClick={() => void sendReply()}>Gửi phản hồi</Button>
          </>
        }
      >
        {selected && (
          <div className="contact-detail">
            <section><span className="eyebrow">NỘI DUNG YÊU CẦU</span><p>{selected.message}</p></section>
            <Select label="Trạng thái" value={selected.status} options={statuses.map((value) => ({ value, label: contactStatusLabels[value] }))} onChange={(event) => void setStatus(event.target.value)} />
            <label className="field"><span>Phản hồi</span><textarea rows="6" placeholder="Nhập nội dung phản hồi…" value={reply} onChange={(event) => setReply(event.target.value)} /></label>
            {actionError && <p className="field-error" role="alert">{actionError}</p>}
          </div>
        )}
      </Dialog>
    </section>
  );
}
