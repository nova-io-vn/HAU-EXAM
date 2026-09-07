import { routes } from "./routes";
import { roles } from "./roles";

const item = (label, to, icon, options = {}) => ({
  label,
  to,
  icon,
  ...options,
});

export const navigationByRole = {
  [roles.SYSTEM_ADMIN]: [
    {
      section: "TỔNG QUAN",
      items: [item("Tổng quan", routes.dashboard, "dashboard")],
    },
    {
      section: "QUẢN TRỊ",
      items: [
        item("Quản lý Khoa", routes.faculties, "building", {
          tour: "faculty-menu",
        }),
        item("Quản lý giảng viên", routes.users, "users", {
          tour: "lecturers-menu",
        }),
        item("Tài khoản chờ duyệt", routes.registrations, "check", {
          tour: "registrations-menu",
        }),
      ],
    },
    {
      section: "HỖ TRỢ",
      items: [
        item("Yêu cầu liên hệ", routes.contactAdmin, "support", {
          tour: "contact-menu",
        }),
      ],
    },
    {
      section: "HỆ THỐNG",
      items: [
        item("Thông báo", routes.notifications, "bell", {
          tour: "notifications-menu",
        }),
        item("Cài đặt hệ thống", routes.settings, "settings", {
          tour: "email-settings",
        }),
      ],
    },
  ],
  [roles.SUBJECT_ADMIN]: [
    {
      section: "TỔNG QUAN",
      items: [item("Tổng quan", routes.dashboard, "dashboard")],
    },
    {
      section: "NỘI DUNG HỌC THUẬT",
      items: [
        item("Môn học", routes.subjects, "book", { tour: "subjects-menu" }),
        item("Cấu trúc kiến thức", routes.knowledge, "network", {
          tour: "knowledge-menu",
        }),
        item("Ngân hàng câu hỏi", routes.questions, "database"),
        item("Câu hỏi chờ duyệt", routes.review, "check", {
          tour: "review-menu",
          badge: "pending",
        }),
      ],
    },
    {
      section: "PHÂN TÍCH",
      items: [
        item("Độ bao phủ kiến thức", routes.coverage, "chart", {
          tour: "coverage-menu",
        }),
        item("Ma trận đề", routes.matrices, "grid", { tour: "matrices-menu" }),
      ],
    },
    {
      section: "AI TRỢ LÝ",
      items: [
        item("Tạo câu hỏi bằng AI", routes.generate, "sparkles", {
          tour: "ai-menu",
        }),
      ],
    },
    {
      section: "HỆ THỐNG",
      items: [
        item("Thông báo", routes.notifications, "bell", {
          tour: "notifications-menu",
        }),
      ],
    },
  ],
  [roles.USER]: [
    {
      section: "TỔNG QUAN",
      items: [item("Tổng quan", routes.dashboard, "dashboard")],
    },
    {
      section: "CÂU HỎI",
      items: [
        item("Câu hỏi của tôi", routes.myQuestions, "file", {
          end: true,
          tour: "my-questions-menu",
        }),
        item("Tạo câu hỏi", routes.newQuestion, "plus", {
          tour: "new-question-menu",
        }),
        item("Ngân hàng câu hỏi", routes.questions, "database", {
          tour: "questions-menu",
        }),
      ],
    },
    {
      section: "TRỢ LÝ AI",
      items: [
        item("Tạo câu hỏi bằng AI", routes.generate, "sparkles", {
          tour: "ai-menu",
        }),
        item("Tài liệu của tôi", routes.documents, "folder"),
      ],
    },
    {
      section: "HỆ THỐNG",
      items: [
        item("Thông báo", routes.notifications, "bell", {
          tour: "notifications-menu",
        }),
      ],
    },
  ],
};

export const bottomNavigation = [
  item("Trung tâm trợ giúp", routes.help, "help", { tour: "help-menu" }),
];
