import { ErrorPage } from "./ErrorPage";
export function NotFoundPage() {
  return (
    <ErrorPage
      code="404"
      title="Không tìm thấy trang"
      description="Trang bạn đang tìm kiếm không tồn tại hoặc đã được di chuyển."
    />
  );
}
