package com.examservice.presentation.response;

import com.examservice.domain.model.*;
import java.time.Instant;
import java.util.*;

public final class ExamViews {
    private ExamViews() { }

    public record MatrixView(UUID id, String name, String facultyId, UUID subjectId, int totalQuestions,
                             List<ExamMatrixRule> rules, Instant createdAt, Instant updatedAt) {
        public static MatrixView from(ExamMatrix matrix) {
            return new MatrixView(matrix.id(), matrix.name(), matrix.facultyId(), matrix.subjectId(),
                    matrix.totalQuestions(), matrix.rules(), matrix.createdAt(), matrix.updatedAt());
        }
    }

    public record TemplateView(UUID id, String name, String facultyId, UUID matrixId, String header,
                               String instructions, Instant createdAt) {
        public static TemplateView from(ExamTemplate template) {
            return new TemplateView(template.id(), template.name(), template.facultyId(), template.matrixId(),
                    template.header(), template.instructions(), template.createdAt());
        }
    }

    public record VersionView(UUID id, int version, int versionCode, long generationSeed, UUID createdBy,
                              int questionCount, boolean matrixValid, double maxOverlapRate,
                              List<ExamQuestionReference> questions, Instant createdAt) { }
    public record VersionOverlap(int firstVersionCode, int secondVersionCode, int sharedQuestions,
                                 double overlapRate) { }
    public record ReuseStatistics(long uniqueQuestionCount, long totalQuestionSelections, long reuseCount,
                                  double reuseRate, boolean warning, String warningMessage) { }

    public record ExamView(UUID id, String name, String examCode, int durationMinutes, ExamStatus status,
                           String facultyId, UUID subjectId, UUID matrixId, UUID templateId, UUID createdBy,
                           int versionStartCode, int versionEndCode, boolean shuffleQuestions,
                           boolean shuffleAnswers, boolean allowQuestionReplacement, List<VersionView> versions,
                           List<VersionOverlap> overlaps, ReuseStatistics reuse, Instant createdAt,
                           Instant updatedAt) {
        public static ExamView from(Exam exam) {
            List<VersionOverlap> overlaps = overlaps(exam.versions());
            Map<Integer, Double> maximum = new HashMap<>();
            overlaps.forEach(value -> {
                maximum.merge(value.firstVersionCode(), value.overlapRate(), Math::max);
                maximum.merge(value.secondVersionCode(), value.overlapRate(), Math::max);
            });
            List<VersionView> versions = exam.versions().stream().map(version -> new VersionView(version.id(),
                    version.versionCode(), version.versionCode(), version.generationSeed(), version.createdBy(),
                    version.questionCount(), true, maximum.getOrDefault(version.versionCode(), 0d),
                    version.questions(), version.createdAt())).toList();
            String warning = exam.reuseWarning()
                    ? "Ngân hàng hiện chưa đủ câu hỏi để tạo các mã đề hoàn toàn khác nhau. Hệ thống đã tối thiểu hóa số câu hỏi trùng lặp."
                    : null;
            return new ExamView(exam.id(), exam.name(), exam.examCode(), exam.durationMinutes(), exam.status(),
                    exam.facultyId(), exam.subjectId(), exam.matrixId(), exam.templateId(), exam.createdBy(),
                    exam.versionStartCode(), exam.versionEndCode(), exam.shuffleQuestions(), exam.shuffleAnswers(),
                    exam.allowQuestionReplacement(), versions, overlaps,
                    new ReuseStatistics(exam.uniqueQuestionCount(), exam.totalQuestionSelections(),
                            exam.reuseCount(), exam.reuseRate(), exam.reuseWarning(), warning),
                    exam.createdAt(), exam.updatedAt());
        }

        private static List<VersionOverlap> overlaps(List<ExamVersion> versions) {
            List<VersionOverlap> result = new ArrayList<>();
            for (int first = 0; first < versions.size(); first++) {
                Set<UUID> firstIds = versions.get(first).questions().stream()
                        .map(ExamQuestionReference::questionId).collect(java.util.stream.Collectors.toSet());
                for (int second = first + 1; second < versions.size(); second++) {
                    Set<UUID> secondIds = versions.get(second).questions().stream()
                            .map(ExamQuestionReference::questionId).collect(java.util.stream.Collectors.toSet());
                    int shared = (int) firstIds.stream().filter(secondIds::contains).count();
                    int denominator = Math.max(firstIds.size(), secondIds.size());
                    double rate = denominator == 0 ? 0 : Math.round(shared * 1000d / denominator) / 10d;
                    result.add(new VersionOverlap(versions.get(first).versionCode(),
                            versions.get(second).versionCode(), shared, rate));
                }
            }
            return result;
        }
    }
}
