package com.questionservice.application.service;

import com.questionservice.application.port.out.CatalogRepository;
import com.questionservice.application.port.out.QuestionRepository;
import com.questionservice.domain.exception.ForbiddenException;
import com.questionservice.domain.exception.NotFoundException;
import com.questionservice.domain.model.Actor;
import com.questionservice.domain.model.Difficulty;
import com.questionservice.domain.model.KnowledgeItem;
import com.questionservice.domain.model.Role;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CoverageService {
    private static final String FORMULA =
            "Knowledge coverage = Knowledge Items with at least one APPROVED question / total Knowledge Items. "
                    + "Difficulty targets are reported separately and do not change knowledge coverage.";

    private final CatalogRepository catalog;
    private final QuestionRepository questions;

    public CoverageService(CatalogRepository catalog, QuestionRepository questions) {
        this.catalog = catalog;
        this.questions = questions;
    }

    public SubjectCoverage calculate(UUID subjectId, Actor actor) {
        requireSubjectAdmin(actor);
        var subject = catalog.findSubject(subjectId)
                .orElseThrow(() -> new NotFoundException("SUBJECT_NOT_FOUND", "Subject not found"));
        if (!subject.isAvailableTo(actor.facultyId())) {
            throw new ForbiddenException("COVERAGE_ACCESS_DENIED", "Subject is outside faculty scope");
        }

        Map<UUID, EnumMap<Difficulty, Long>> approvedByItem = new HashMap<>();
        questions.approvedCoverage(subjectId).forEach(count -> approvedByItem
                .computeIfAbsent(count.knowledgeItemId(), ignored -> new EnumMap<>(Difficulty.class))
                .put(count.difficulty(), count.count()));

        List<ChapterCoverage> chapterRows = new ArrayList<>();
        List<ItemCoverage> allItems = new ArrayList<>();
        List<MissingKnowledge> missingKnowledge = new ArrayList<>();
        for (var chapter : catalog.findChapters(subjectId)) {
            List<TopicCoverage> topicRows = new ArrayList<>();
            List<ItemCoverage> chapterItems = new ArrayList<>();
            for (var topic : catalog.findTopics(chapter.id())) {
                List<ItemCoverage> itemRows = catalog.findKnowledgeItems(topic.id()).stream()
                        .map(item -> itemCoverage(item, approvedByItem.get(item.id())))
                        .toList();
                itemRows.stream()
                        .filter(item -> item.coverageStatus() == KnowledgeCoverageStatus.MISSING)
                        .map(item -> new MissingKnowledge(item.knowledgeItemId(), item.knowledgeItemName(),
                                chapter.id(), chapter.name(), topic.id(), topic.name(),
                                item.approvedQuestionCount(), item.missingDifficultyTargets()))
                        .forEach(missingKnowledge::add);
                chapterItems.addAll(itemRows);
                allItems.addAll(itemRows);
                topicRows.add(new TopicCoverage(topic.id(), topic.code(), topic.name(), summary(itemRows), itemRows));
            }
            chapterRows.add(new ChapterCoverage(chapter.id(), chapter.code(), chapter.name(),
                    summary(chapterItems), topicRows));
        }

        return new SubjectCoverage(subject.id(), subject.code(), subject.name(), summary(allItems), chapterRows,
                missingKnowledge, questions.countWithoutKnowledgeItem(subjectId), FORMULA);
    }

    private static void requireSubjectAdmin(Actor actor) {
        if (actor == null || actor.role() != Role.SUBJECT_ADMIN
                || actor.facultyId() == null || actor.facultyId().isBlank()) {
            throw new ForbiddenException("COVERAGE_ACCESS_DENIED", "SUBJECT_ADMIN faculty scope is required");
        }
    }

    private ItemCoverage itemCoverage(KnowledgeItem item, EnumMap<Difficulty, Long> values) {
        long easy = count(values, Difficulty.EASY);
        long medium = count(values, Difficulty.MEDIUM);
        long hard = count(values, Difficulty.HARD);
        long approved = easy + medium + hard;
        int targetTotal = item.targetEasy() + item.targetMedium() + item.targetHard();
        long targetSatisfied = Math.min(easy, item.targetEasy())
                + Math.min(medium, item.targetMedium())
                + Math.min(hard, item.targetHard());
        Double targetPercentage = targetTotal == 0 ? null : percentage(targetSatisfied, targetTotal);
        List<String> missingTargets = new ArrayList<>();
        addMissing(missingTargets, Difficulty.EASY, easy, item.targetEasy());
        addMissing(missingTargets, Difficulty.MEDIUM, medium, item.targetMedium());
        addMissing(missingTargets, Difficulty.HARD, hard, item.targetHard());
        DifficultyTargetStatus targetStatus = targetTotal == 0
                ? DifficultyTargetStatus.NOT_CONFIGURED
                : missingTargets.isEmpty() ? DifficultyTargetStatus.SATISFIED : DifficultyTargetStatus.MISSING;

        return new ItemCoverage(item.id(), item.code(), item.name(), approved,
                approved > 0 ? KnowledgeCoverageStatus.COVERED : KnowledgeCoverageStatus.MISSING,
                easy, medium, hard, item.targetEasy(), item.targetMedium(), item.targetHard(),
                targetPercentage, targetStatus, missingTargets);
    }

    private Summary summary(List<ItemCoverage> items) {
        long totalItems = items.size();
        long coveredItems = items.stream()
                .filter(item -> item.coverageStatus() == KnowledgeCoverageStatus.COVERED)
                .count();
        long approved = items.stream().mapToLong(ItemCoverage::approvedQuestionCount).sum();
        long targetQuestions = items.stream().mapToLong(ItemCoverage::targetTotal).sum();
        long targetSatisfiedQuestions = items.stream().mapToLong(ItemCoverage::satisfiedTargetQuestions).sum();
        long configuredItems = items.stream().filter(item -> item.targetTotal() > 0).count();
        long satisfiedItems = items.stream()
                .filter(item -> item.difficultyTargetStatus() == DifficultyTargetStatus.SATISFIED)
                .count();
        DifficultyTargetSummary difficultyTarget = new DifficultyTargetSummary(
                targetQuestions == 0 ? null : percentage(targetSatisfiedQuestions, targetQuestions),
                approved, targetQuestions, configuredItems, satisfiedItems);
        return new Summary(percentage(coveredItems, totalItems), approved, coveredItems, totalItems, difficultyTarget);
    }

    private static void addMissing(List<String> result, Difficulty difficulty, long actual, int target) {
        if (actual < target) result.add(difficulty.name() + " thiếu " + (target - actual));
    }

    private static long count(EnumMap<Difficulty, Long> values, Difficulty difficulty) {
        return values == null ? 0 : values.getOrDefault(difficulty, 0L);
    }

    private static double percentage(long covered, long total) {
        return total == 0 ? 0 : Math.round(covered * 1000d / total) / 10d;
    }

    public enum KnowledgeCoverageStatus { COVERED, MISSING }
    public enum DifficultyTargetStatus { SATISFIED, MISSING, NOT_CONFIGURED }

    public record SubjectCoverage(UUID subjectId, String subjectCode, String subjectName, Summary overall,
                                  List<ChapterCoverage> chapters, List<MissingKnowledge> missingKnowledge,
                                  long legacyQuestionsWithoutKnowledgeItem, String formula) { }

    public record ChapterCoverage(UUID chapterId, String chapterCode, String chapterName, Summary coverage,
                                  List<TopicCoverage> topics) { }

    public record TopicCoverage(UUID topicId, String topicCode, String topicName, Summary coverage,
                                List<ItemCoverage> knowledgeItems) { }

    public record ItemCoverage(UUID knowledgeItemId, String knowledgeItemCode, String knowledgeItemName,
                               long approvedQuestionCount, KnowledgeCoverageStatus coverageStatus,
                               long easyCount, long mediumCount, long hardCount,
                               int targetEasy, int targetMedium, int targetHard,
                               Double difficultyTargetPercentage, DifficultyTargetStatus difficultyTargetStatus,
                               List<String> missingDifficultyTargets) {
        int targetTotal() {
            return targetEasy + targetMedium + targetHard;
        }

        long satisfiedTargetQuestions() {
            return Math.min(easyCount, targetEasy)
                    + Math.min(mediumCount, targetMedium)
                    + Math.min(hardCount, targetHard);
        }
    }

    public record Summary(double coveragePercentage, long approvedQuestionCount,
                          long coveredKnowledgeItems, long totalKnowledgeItems,
                          DifficultyTargetSummary difficultyTarget) { }

    public record DifficultyTargetSummary(Double percentage, long approvedQuestionCount,
                                          long targetQuestionCount, long configuredKnowledgeItems,
                                          long satisfiedKnowledgeItems) { }

    public record MissingKnowledge(UUID knowledgeItemId, String knowledgeItemName,
                                   UUID chapterId, String chapterName, UUID topicId, String topicName,
                                   long approvedQuestionCount, List<String> missingDifficultyTargets) { }
}
