package com.aiservice.application.service;

import com.aiservice.application.port.out.AiProvider;
import com.aiservice.domain.exception.InvalidAiOutputException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SystemHelpService {
    private static final Logger log = LoggerFactory.getLogger(SystemHelpService.class);
    private static final Map<String, List<Article>> KNOWLEDGE = Map.of(
            "USER", List.of(
                    new Article("QUESTION_CREATE", "Tạo câu hỏi thủ công", "Mở Câu hỏi của tôi; chọn Tạo câu hỏi; chọn môn/chương/chủ đề; nhập nội dung, đáp án; lưu bản nháp; gửi duyệt khi hoàn tất."),
                    new Article("MY_QUESTIONS", "Gửi câu hỏi để duyệt", "Mở Câu hỏi của tôi; chọn câu hỏi bản nháp; kiểm tra nội dung; chọn Gửi duyệt; theo dõi trạng thái PENDING_REVIEW."),
                    new Article("AI_GENERATE", "Tạo câu hỏi bằng AI", "Mở Tạo câu hỏi bằng AI; chọn hoặc tải tài liệu; cấu hình yêu cầu; tạo tác vụ; theo dõi tiến độ và kiểm tra kết quả trước khi gửi duyệt."),
                    new Article("AI_DOCUMENTS", "Tài liệu AI", "Mở Tài liệu AI để tải và quản lý học liệu của chính bạn."),
                    new Article("NOTIFICATIONS", "Thông báo", "Mở Thông báo để xem, đánh dấu đã đọc và theo dõi kết quả duyệt."),
                    new Article("PROFILE", "Hồ sơ và giao diện", "Mở Hồ sơ cá nhân để cập nhật hồ sơ và chọn Giao diện Sáng, Tối hoặc Theo hệ thống.")),
            "SUBJECT_ADMIN", List.of(
                    new Article("QUESTION_REVIEW", "Duyệt câu hỏi", "Mở Duyệt câu hỏi; chọn câu hỏi chờ duyệt thuộc khoa; kiểm tra nội dung; phê duyệt, từ chối hoặc yêu cầu chỉnh sửa."),
                    new Article("SUBJECTS", "Quản lý chuyên môn", "Mở Môn học để quản lý nội dung trong phạm vi khoa được phân công."),
                    new Article("EXAM_MATRICES", "Ma trận đề", "Mở Ma trận đề; tạo hoặc chọn ma trận; cấu hình phân bố chương/chủ đề/độ khó; lưu và kiểm tra độ phủ."),
                    new Article("AI_GENERATE", "AI hỗ trợ", "Mở Tạo câu hỏi bằng AI để tạo nội dung hỗ trợ; kết quả vẫn phải được kiểm tra theo workflow duyệt."),
                    new Article("NOTIFICATIONS", "Thông báo", "Mở Thông báo để theo dõi sự kiện chuyên môn trong phạm vi tài khoản."),
                    new Article("PROFILE", "Hồ sơ và giao diện", "Mở Hồ sơ cá nhân để cập nhật hồ sơ và chọn Giao diện.")),
            "SYSTEM_ADMIN", List.of(
                    new Article("USERS", "Quản lý người dùng", "Mở Quản lý người dùng để tìm tài khoản, xem chi tiết, gán vai trò, gán khoa hoặc khóa/mở khóa."),
                    new Article("FACULTIES", "Quản lý khoa", "Mở Quản lý khoa để tìm, tạo và cập nhật khoa."),
                    new Article("SYSTEM_SETTINGS", "Cấu hình Email", "Mở Cài đặt hệ thống; xem cấu hình SMTP đã bootstrap; chỉnh sửa và dùng kiểm tra gửi email khi cần."),
                    new Article("CONTACT_REQUESTS", "Yêu cầu liên hệ", "Mở Yêu cầu liên hệ để xem và xử lý yêu cầu hỗ trợ."),
                    new Article("NOTIFICATIONS", "Thông báo", "Mở Thông báo để xem thông báo hệ thống."),
                    new Article("PROFILE", "Hồ sơ và giao diện", "Mở Hồ sơ cá nhân để cập nhật hồ sơ và chọn Giao diện.")));

    private final AiProvider provider;
    private final ObjectMapper mapper;
    private final AiKnowledgeService knowledge;

    private static final List<Pattern> BLOCKED = List.of(
            Pattern.compile("(?i)(api|cloudinary|jwt|smtp|database|db|service)[\\s_-]*(key|secret|password|token|credential)"),
            Pattern.compile("(?i)(system prompt|developer message|internal prompt|tiết lộ.*prompt|hiển thị.*prompt)"),
            Pattern.compile("(?i)(bypass|vượt qua|bỏ qua|ignore).*(rbac|phân quyền|security|bảo mật|previous instructions|chỉ dẫn trước)"),
            Pattern.compile("(?i)(truy cập|đọc|lấy|xem).*(dữ liệu|tài khoản|user).*(người khác|không.*quyền|trái phép)"),
            Pattern.compile("(?is)\\b(select|insert|update|delete|drop|alter|truncate)\\b.+\\b(from|into|table|where|set)\\b")
    );

    public SystemHelpService(AiProvider provider, ObjectMapper mapper, AiKnowledgeService knowledge) { this.provider = provider; this.mapper = mapper; this.knowledge = knowledge; }

    public Result ask(String role, String message) {
        if (message == null || message.isBlank()) throw new IllegalArgumentException("Message is required");
        if (isSensitive(message)) return new Result("Tôi không thể cung cấp bí mật hệ thống, hỗ trợ vượt quyền, truy cập dữ liệu trái phép hoặc thực thi câu lệnh nguy hiểm. Tôi có thể hướng dẫn cách sử dụng HAU QM an toàn.", List.of(), List.of());
        Result simple = simpleAnswer(message);
        if (simple != null) return simple;
        String intent = message.toLowerCase(Locale.ROOT);
        if ("USER".equals(role) && (intent.contains("phê duyệt") || intent.contains("phe duyet") || intent.contains("duyệt câu hỏi") || intent.contains("duyet cau hoi")))
            return new Result("Giảng viên không trực tiếp phê duyệt câu hỏi. Bạn hãy mở Câu hỏi của tôi, kiểm tra nội dung rồi chọn Gửi duyệt để Quản trị viên chuyên môn của Khoa xem xét.", List.of(new Action("NAVIGATE", "Mở câu hỏi của tôi", "MY_QUESTIONS")), List.of());
        List<Article> articles = KNOWLEDGE.get(role);
        if (articles == null) throw new IllegalArgumentException("Unsupported role");
        try {
            var sources = knowledge == null ? List.<AiKnowledgeService.Source>of() : knowledge.retrieve(message, 4);
            String source = mapper.writeValueAsString(Map.of("role", role, "articles", articles, "policySources", sources,
                    "responsePolicy", "Answer safe everyday conversation and general knowledge normally. Use supplied sources for HAU-specific workflows. Never reveal secrets or grant access."));
            String request = mapper.writeValueAsString(Map.of("message", message));
            var root = mapper.readTree(provider.systemHelp(source, request));
            String answer = root.path("answer").asText("").trim();
            if (answer.isBlank()) throw new InvalidAiOutputException("System help answer is empty");
            Set<String> allowed = articles.stream().map(Article::routeKey).collect(java.util.stream.Collectors.toUnmodifiableSet());
            var actions = new java.util.ArrayList<Action>();
            root.path("actions").forEach(node -> {
                String routeKey = node.path("routeKey").asText("");
                if (allowed.contains(routeKey) && "NAVIGATE".equals(node.path("type").asText("NAVIGATE")))
                    actions.add(new Action("NAVIGATE", node.path("label").asText("Đi tới chức năng"), routeKey));
            });
            log.info("System help answer completed; role={} actionCount={} messageLength={}", role, actions.size(), message.length());
            return new Result(answer, List.copyOf(actions), sources);
        } catch (com.aiservice.domain.exception.ProviderException | InvalidAiOutputException exception) {
            log.warn("System help provider unavailable; using local role guide; role={} errorType={}", role, exception.getClass().getSimpleName());
            return localAnswer(message, articles);
        } catch (Exception exception) {
            log.warn("System help response malformed; using local role guide; role={} errorType={}", role, exception.getClass().getSimpleName());
            return localAnswer(message, articles);
        }
    }

    private Result localAnswer(String message, List<Article> articles) {
        String normalized = message.toLowerCase(java.util.Locale.ROOT);
        Article match = articles.stream().max(java.util.Comparator.comparingInt(article -> score(normalized, article))).orElse(articles.getFirst());
        if (score(normalized, match) == 0) {
            return new Result("HAU QM có các nhóm chức năng chính: hồ sơ và tài khoản, ngân hàng câu hỏi, học liệu và AI, phê duyệt chuyên môn, ma trận và đề thi, thông báo, tin nhắn và hỗ trợ. Bạn có thể nói rõ mục tiêu, ví dụ ‘làm sao tạo câu hỏi?’ để mình hướng dẫn từng bước.", List.of(), List.of());
        }
        return new Result(match.title() + ":\n" + match.workflow(), List.of(new Action("NAVIGATE", "Mở " + match.title(), match.routeKey())), List.of());
    }

    private int score(String message, Article article) {
        return java.util.stream.Stream.concat(java.util.stream.Stream.of(article.title()), java.util.Arrays.stream(article.title().split("\\s+")))
                .map(value -> value.toLowerCase(java.util.Locale.ROOT))
                .mapToInt(value -> value.length() > 2 && message.contains(value) ? 1 : 0)
                .sum();
    }

    public record Article(String routeKey, String title, String workflow) {}
    public record Action(String type, String label, String routeKey) {}
    private boolean isSensitive(String message) { return BLOCKED.stream().anyMatch(pattern -> pattern.matcher(message).find()); }

    private Result simpleAnswer(String message) {
        String normalized = message.trim().toLowerCase(Locale.ROOT).replaceAll("[!?.]+$", "");
        if (Set.of("xin chào", "chào", "hello", "hi").contains(normalized))
            return plain("Xin chào! Tôi là Trợ lý HAU QM. Tôi có thể hỗ trợ bạn về hệ thống, học liệu hoặc các câu hỏi kiến thức phổ thông an toàn.");
        if (normalized.contains("bạn là ai") || normalized.contains("ban la ai"))
            return plain("Tôi là HAU QM Assistant, trợ lý hỗ trợ sử dụng hệ thống và giải đáp ngắn gọn các câu hỏi kiến thức phổ thông an toàn.");
        if (normalized.equals("cảm ơn") || normalized.equals("cam on") || normalized.equals("thank you") || normalized.equals("thanks"))
            return plain("Rất vui được hỗ trợ bạn. Nếu cần, bạn cứ hỏi tiếp nhé!");
        if (normalized.matches("1\\s*\\+\\s*1(\\s*bằng\\s*mấy|\\s*=\\s*\\?|\\s*la\\s*may)?"))
            return plain("1 + 1 = 2.");
        if (normalized.contains("rest api") && (normalized.contains("là gì") || normalized.contains("la gi") || normalized.startsWith("giải thích") || normalized.startsWith("giai thich")))
            return plain("REST API là cách các hệ thống trao đổi dữ liệu qua HTTP bằng những tài nguyên và phương thức quen thuộc như GET, POST, PUT, PATCH và DELETE. Dữ liệu thường được gửi dưới dạng JSON.");
        if ((normalized.equals("java là gì") || normalized.equals("java la gi")))
            return plain("Java là ngôn ngữ lập trình hướng đối tượng, đa nền tảng, thường dùng để xây dựng backend, ứng dụng doanh nghiệp, Android và nhiều hệ thống phân tán.");
        return null;
    }

    private Result plain(String answer) { return new Result(answer, List.of(), List.of()); }
    public record Result(String answer, List<Action> actions, List<AiKnowledgeService.Source> sources) {}
}
