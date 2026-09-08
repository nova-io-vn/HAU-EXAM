import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  Button,
  DataTable,
  DashboardMetricSkeleton,
  TableSkeleton,
  StatusBadge,
} from "../../../components/ui";
import { PageHeader } from "../../../components/shared/PageHeader";
import { routes } from "../../../constants/routes";
import { useAuth } from "../../auth/hooks/useAuth";
import { facultiesApi } from "../api/facultiesApi";
import { usersApi } from "../api/usersApi";
import { questionsApi } from "../../questions/api/questionsApi";
import { normalizePage, formatDateTime } from "../model/userModel";
export function AdminDashboardPage() {
  const { role } = useAuth();
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);
  const load = useCallback(async () => {
    try {
      const [faculties, users, pending, questions] = await Promise.all([
        facultiesApi.list({ page: 0, size: 1 }),
        usersApi.list({ page: 0, size: 1 }),
        usersApi.list({ status: "PENDING_APPROVAL", page: 0, size: 5 }),
        questionsApi.list({ page: 0, size: 1 }),
      ]);
      setData({
        faculties: normalizePage(faculties),
        users: normalizePage(users),
        pending: normalizePage(pending),
        questions: normalizePage(questions),
      });
    } catch (e) {
      setError(e);
    }
  }, []);
  useEffect(() => {
    if (role !== "SYSTEM_ADMIN") return;
    const timer = setTimeout(load, 0);
    return () => clearTimeout(timer);
  }, [load, role]);
  if (role !== "SYSTEM_ADMIN")
    return (
      <section>
        <PageHeader
          title="Tổng quan"
          description="Không gian làm việc theo vai trò của bạn."
        />
        <div className="surface admin-unavailable">
          <strong>Dashboard chuyên môn</strong>
          <p>
            Hãy sử dụng các chức năng trong menu theo phạm vi được phân công.
          </p>
        </div>
      </section>
    );
  if (error)
    return (
      <section>
        <PageHeader
          title="Tổng quan hệ thống"
          description="Theo dõi người dùng, Khoa và hoạt động trên toàn hệ thống khảo thí HAU."
        />
        <div className="surface admin-error">
          <h2>Không thể tải số liệu hệ thống</h2>
          <p>{error.message}</p>
          <Button onClick={load}>Thử lại</Button>
        </div>
      </section>
    );
  if (!data) return <section className="admin-dashboard"><PageHeader title="Tổng quan hệ thống" description="Theo dõi người dùng, Khoa và hoạt động trên toàn hệ thống khảo thí HAU."/><DashboardMetricSkeleton count={4}/><div className="admin-dashboard-grid"><section className="surface admin-panel"><TableSkeleton rows={5} columns={4}/></section><section className="surface admin-panel"><div className="chart-empty">Đang tải dữ liệu quản trị...</div></section></div><section className="surface admin-panel"><TableSkeleton rows={6} columns={7}/></section></section>;
  const stats = [
    ["Tổng giảng viên", data.users.totalElements, "users"],
    ["Tổng số Khoa", data.faculties.totalElements, "faculties"],
    ["Chờ phê duyệt", data.pending.totalElements, "registrations"],
    ["Tổng số câu hỏi", data.questions.totalElements, "questions"],
  ];
  return (
    <section className="admin-dashboard">
      <PageHeader
        title="Tổng quan hệ thống"
        description="Theo dõi người dùng, Khoa và hoạt động trên toàn hệ thống khảo thí HAU."
        actions={
          <>
            <Button variant="secondary" onClick={() => window.print()}>
              Xuất báo cáo hệ thống
            </Button>
            <Link className="button button-primary" to={routes.users}>
              Quản lý người dùng
            </Link>
          </>
        }
      />
      <div className="admin-kpi-grid">
        {stats.map(([label, value, to], index) => (
          <Link className="admin-kpi" to={routes[to]} key={label}>
            <span className={"kpi-accent kpi-" + index} />
            <small>{label}</small>
            <strong>{value.toLocaleString("vi-VN")}</strong>
            <span>Xem chi tiết →</span>
          </Link>
        ))}
      </div>
      <div className="admin-dashboard-grid">
        <section className="surface admin-panel">
          <header>
            <div>
              <span className="eyebrow">PHẠM VI HỆ THỐNG</span>
              <h2>Phân bố giảng viên theo Khoa</h2>
            </div>
            <Link to={routes.faculties}>Quản lý Khoa →</Link>
          </header>
          <DataTable rows={[]} emptyTitle="Chưa có dữ liệu phân bố Khoa." />
        </section>
        <section className="surface admin-panel">
          <header>
            <div>
              <span className="eyebrow">ACTIVITY</span>
              <h2>Hoạt động gần đây</h2>
            </div>
          </header>
          <div className="admin-unavailable">
            <strong>Chưa có Activity API</strong>
            <p>
              Hoạt động sẽ hiển thị khi backend cung cấp audit/activity feed.
            </p>
          </div>
        </section>
      </div>
      <section className="surface admin-panel">
        <header>
          <div>
            <span className="eyebrow">CẦN XỬ LÝ</span>
            <h2>Tài khoản chờ phê duyệt</h2>
          </div>
          <Link to={routes.registrations}>Xem tất cả →</Link>
        </header>
        <DataTable
          rows={data.pending.items}
          emptyTitle="Không có tài khoản đang chờ phê duyệt."
          columns={[
            {
              key: "lecturerCode",
              header: "Mã GV",
              render: (u) => <span className="mono">{u.lecturerCode}</span>,
            },
            { key: "fullName", header: "Giảng viên" },
            { key: "email", header: "Email" },
            {
              key: "facultyId",
              header: "Khoa",
              render: (u) => u.facultyId || "Chưa phân công",
            },
            {
              key: "createdAt",
              header: "Ngày đăng ký",
              render: (u) => formatDateTime(u.createdAt),
            },
            {
              key: "status",
              header: "Trạng thái",
              render: (u) => <StatusBadge status={u.status} />,
            },
            {
              key: "action",
              header: "",
              render: (u) => (
                <Link to={routes.userDetail.replace(":id", u.id)}>
                  Xem & duyệt
                </Link>
              ),
            },
          ]}
        />
      </section>
    </section>
  );
}
