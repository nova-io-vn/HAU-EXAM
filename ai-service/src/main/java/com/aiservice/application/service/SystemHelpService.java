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
        return ask(role, message, null);
    }

    public Result ask(String role, String message, String previousQuestion) {
        if (message == null || message.isBlank()) throw new IllegalArgumentException("Message is required");
        if (isSensitive(message)) return new Result("Tôi không thể cung cấp bí mật hệ thống, hỗ trợ vượt quyền, truy cập dữ liệu trái phép hoặc thực thi câu lệnh nguy hiểm. Tôi có thể hướng dẫn cách sử dụng HAU QM an toàn.", List.of(), List.of());
        List<Article> articles = KNOWLEDGE.get(role);
        if (articles == null) throw new IllegalArgumentException("Unsupported role");
        Result simple = simpleAnswer(message);
        if (simple != null) return simple;
        Result policy = policyAnswer(role, message, previousQuestion);
        if (policy != null) return policy;
        String intent = message.toLowerCase(Locale.ROOT);
        if ("USER".equals(role) && (intent.contains("phê duyệt") || intent.contains("phe duyet") || intent.contains("duyệt câu hỏi") || intent.contains("duyet cau hoi")))
            return new Result("Giảng viên không trực tiếp phê duyệt câu hỏi. Bạn hãy mở Câu hỏi của tôi, kiểm tra nội dung rồi chọn Gửi duyệt để Quản trị viên chuyên môn của Khoa xem xét.", List.of(new Action("NAVIGATE", "Mở câu hỏi của tôi", "MY_QUESTIONS")), List.of());
        try {
            String contextQuestion = previousQuestion == null ? "" : previousQuestion.trim();
            var sources = knowledge == null ? List.<AiKnowledgeService.Source>of() : knowledge.retrieve(message, 4);
            String source = mapper.writeValueAsString(Map.of("role", role, "articles", articles, "policySources", sources,
                    "responsePolicy", "SYSTEM_ADMIN manages accounts and faculties but does not review subject questions. Only SUBJECT_ADMIN of the question's faculty reviews it. Newly registered users wait for SYSTEM_ADMIN approval before login. Answer safe everyday conversation and general knowledge normally. Use supplied sources for HAU-specific workflows. If no relevant source exists, say that there is not enough evidence. Never reveal secrets or grant access."));
            String request = mapper.writeValueAsString(Map.of("message", message, "previousUserQuestion", contextQuestion));
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
            return plain("Mình chưa có đủ thông tin đã kiểm chứng để trả lời câu hỏi này. Bạn có thể hỏi về một thao tác cụ thể trong HAU QM.");
        }
        return new Result(match.title() + ":\n" + match.workflow(), List.of(new Action("NAVIGATE", "Mở " + match.title(), match.routeKey())), List.of());
    }

    private int score(String message, Article article) {
        return java.util.stream.Stream.concat(java.util.stream.Stream.of(article.title()), java.util.Arrays.stream(article.title().split("\\s+")))
                .map(value -> value.toLowerCase(java.util.Locale.ROOT))
                .mapToInt(value -> value.length() > 2 && message.contains(value) ? 1 : 0)
                .sum();
    }

    private Result policyAnswer(String role, String message, String previousQuestion) {
        String text = message.toLowerCase(Locale.ROOT);
        String previous = previousQuestion == null ? "" : previousQuestion.toLowerCase(Locale.ROOT);
        boolean aboutReview = (text.contains("duyệt") || text.contains("phê duyệt") || text.contains("phe duyet"))
                && (text.contains("câu hỏi") || text.contains("cau hoi")
                    || (text.contains("đó") && previous.contains("câu hỏi")));
        if (aboutReview) {
            if ("SYSTEM_ADMIN".equals(role))
                return plain("SYSTEM_ADMIN quản lý tài khoản và khoa, không duyệt câu hỏi chuyên môn. SUBJECT_ADMIN chỉ được duyệt câu hỏi thuộc khoa mình được phân công; Question Service phải kiểm tra facultyId của người duyệt và câu hỏi.");
            if ("SUBJECT_ADMIN".equals(role))
                return new Result("Bạn chỉ được duyệt câu hỏi thuộc khoa mình quản lý. Mở Duyệt câu hỏi, kiểm tra nội dung và chọn phê duyệt, từ chối hoặc yêu cầu chỉnh sửa.",
                        List.of(new Action("NAVIGATE", "Mở Duyệt câu hỏi", "QUESTION_REVIEW")), List.of());
            return new Result("Giảng viên tạo câu hỏi và gửi duyệt; SUBJECT_ADMIN của cùng khoa mới có quyền xét duyệt chuyên môn.",
                    List.of(new Action("NAVIGATE", "Mở Câu hỏi của tôi", "MY_QUESTIONS")), List.of());
        }
        if ((text.contains("đăng ký") || text.contains("dang ky"))
                && (text.contains("đăng nhập") || text.contains("dang nhap") || text.contains("duyệt") || text.contains("duyet")))
            return plain("Tài khoản mới đăng ký ở trạng thái PENDING_APPROVAL, chưa được đăng nhập ngay. SYSTEM_ADMIN cần phê duyệt để tài khoản chuyển sang ACTIVE.");
        if (text.contains("tạo câu hỏi") || text.contains("tao cau hoi")) {
            if ("SYSTEM_ADMIN".equals(role))
                return plain("SYSTEM_ADMIN quản lý tài khoản và khoa, không trực tiếp tạo câu hỏi chuyên môn. Giảng viên mở Tạo câu hỏi, chọn môn và chương được phân công, nhập nội dung và đáp án, lưu bản nháp rồi gửi duyệt.");
            return new Result("Mở Tạo câu hỏi; chọn môn và chương được phân công; nhập nội dung và các phương án, đánh dấu đáp án đúng; lưu bản nháp rồi gửi duyệt khi hoàn tất.",
                    List.of(new Action("NAVIGATE", "Mở Tạo câu hỏi", "QUESTION_CREATE")), List.of());
        }
        return null;
    }

    public record Article(String routeKey, String title, String workflow) {}
    public record Action(String type, String label, String routeKey) {}
    private boolean isSensitive(String message) { return BLOCKED.stream().anyMatch(pattern -> pattern.matcher(message).find()); }

    private Result simpleAnswer(String message) {
        String normalized = message.trim().toLowerCase(Locale.ROOT).replaceAll("[!?.]+$", "");
        if (Set.of("xin chào", "chào", "hello", "hi").contains(normalized))
            return plain("Xin chào, mình là Kute ✨ Mình là trợ lý AI của HAU QM, có thể hỗ trợ bạn sử dụng hệ thống, học liệu và các câu hỏi kiến thức phổ thông an toàn.");
        if (normalized.contains("bạn là ai") || normalized.contains("ban la ai"))
            return plain("Mình là Kute, trợ lý AI của HAU QM. Mình không phải người thật, nhưng mình luôn sẵn sàng hỗ trợ bạn một cách rõ ràng và thân thiện.");
        if (normalized.contains("người thật") || normalized.contains("nguoi that"))
            return plain("Không nhé, mình là Kute — một trợ lý AI của HAU QM, không phải người thật. Mình không có cơ thể hay đời sống cá nhân, nhưng có thể hỗ trợ bạn về hệ thống và kiến thức an toàn.");
        if (normalized.contains("hôm nay mình nên bắt đầu từ đâu") || normalized.contains("hom nay minh nen bat dau tu dau"))
            return plain("Mình gợi ý bắt đầu bằng một mục tiêu nhỏ: kiểm tra thông báo, chọn việc quan trọng nhất cần hoàn thành, rồi dành 25 phút tập trung cho việc đó. Nếu bạn nói vai trò và mục tiêu hôm nay, mình có thể đề xuất cụ thể hơn.");
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
