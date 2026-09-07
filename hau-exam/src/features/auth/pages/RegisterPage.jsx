import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Button, Input, Select } from "../../../components/ui";
import { routes } from "../../../constants/routes";
import { authApi } from "../api/authApi";
import { AuthAlert } from "../components/AuthAlert";
import { AuthLayout } from "../components/AuthLayout";
import { PasswordInput } from "../components/PasswordInput";
import { getAuthErrorMessage } from "../model/authErrors";
import { facultiesApi } from "../../users/api/facultiesApi";

const initialForm = {
  lecturerCode: "",
  password: "",
  confirmPassword: "",
  fullName: "",
  email: "",
  facultyId: "",
  consent: false,
};
export function RegisterPage() {
  const [form, setForm] = useState(initialForm);
  const [faculties, setFaculties] = useState([]);
  const [facultyState, setFacultyState] = useState({
    loading: true,
    error: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  useEffect(() => {
    let active = true;
    facultiesApi
      .publicList()
      .then((result) => {
        if (!active) return;
        const items =
          result?.content || result?.data?.content || result?.data || [];
        setFaculties(items);
        setFacultyState({ loading: false, error: "" });
      })
      .catch((reason) => {
        if (active)
          setFacultyState({
            loading: false,
            error: reason.message || "Không thể tải danh sách Khoa.",
          });
      });
    return () => {
      active = false;
    };
  }, []);
  function change(field) {
    return (event) =>
      setForm((current) => ({
        ...current,
        [field]:
          event.target.type === "checkbox"
            ? event.target.checked
            : event.target.value,
      }));
  }
  const mismatch = form.password !== form.confirmPassword;
  const invalid =
    !form.lecturerCode.trim() ||
    !form.fullName.trim() ||
    !form.email.trim() ||
    !form.facultyId ||
    form.password.length < 8 ||
    mismatch ||
    !form.consent ||
    facultyState.loading;
  async function submit(event) {
    event.preventDefault();
    if (invalid) return;
    setError("");
    setLoading(true);
    try {
      await authApi.register({
        lecturerCode: form.lecturerCode.trim(),
        password: form.password,
        fullName: form.fullName.trim(),
        email: form.email.trim(),
        facultyId: form.facultyId,
      });
      navigate(routes.registrationPending, { replace: true });
    } catch (reason) {
      setError(getAuthErrorMessage(reason, "Không thể đăng ký tài khoản."));
    } finally {
      setLoading(false);
    }
  }
  return (
    <AuthLayout
      activeTab="register"
      title="Đăng ký tài khoản"
      description="Tài khoản mới cần được quản trị viên hệ thống phê duyệt trước khi đăng nhập."
      footer={
        <span>
          Đã có tài khoản? <Link to={routes.login}>Đăng nhập</Link>
        </span>
      }
    >
      <AuthAlert>{error}</AuthAlert>
      <form className="auth-form" onSubmit={submit}>
        <Input
          label="Họ và tên"
          name="fullName"
          autoComplete="name"
          required
          value={form.fullName}
          onChange={change("fullName")}
        />
        <Input
          label="Mã giảng viên"
          name="lecturerCode"
          autoComplete="username"
          required
          value={form.lecturerCode}
          onChange={change("lecturerCode")}
        />
        <Input
          label="Email"
          name="email"
          type="email"
          autoComplete="email"
          required
          value={form.email}
          onChange={change("email")}
        />
        <Select
          label="Khoa đăng ký"
          name="facultyId"
          required
          value={form.facultyId}
          disabled={facultyState.loading || Boolean(facultyState.error)}
          options={[
            {
              value: "",
              label: facultyState.loading
                ? "Đang tải danh sách Khoa..."
                : "Chọn Khoa",
            },
            ...faculties
              .filter((item) => item.active !== false)
              .map((item) => ({
                value: item.code || item.id,
                label: item.name,
              })),
          ]}
          onChange={change("facultyId")}
        />
        {facultyState.error && (
          <p className="field-error" role="alert">
            {facultyState.error}
          </p>
        )}
        <PasswordInput
          label="Mật khẩu"
          name="password"
          autoComplete="new-password"
          required
          value={form.password}
          onChange={change("password")}
          helper="Mật khẩu tối thiểu 8 ký tự."
        />
        <PasswordInput
          label="Xác nhận mật khẩu"
          name="confirmPassword"
          autoComplete="new-password"
          required
          value={form.confirmPassword}
          onChange={change("confirmPassword")}
          error={mismatch ? "Mật khẩu xác nhận không khớp." : undefined}
        />
        <label className="policy-consent">
          <input
            type="checkbox"
            checked={form.consent}
            onChange={change("consent")}
          />
          <span>
            Tôi đồng ý với <Link to={routes.terms}>Điều khoản sử dụng</Link> và{" "}
            <Link to={routes.privacy}>Chính sách bảo mật</Link>.
          </span>
        </label>
        <Button type="submit" loading={loading} disabled={invalid || loading}>
          Đăng ký tài khoản
        </Button>
      </form>
    </AuthLayout>
  );
}
