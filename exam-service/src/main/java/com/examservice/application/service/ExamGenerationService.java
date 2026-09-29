package com.examservice.application.service;

import com.examservice.application.port.out.*;
import com.examservice.domain.exception.*;
import com.examservice.domain.model.*;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExamGenerationService {
    private final ExamRepository exams;
    private final ExamMatrixRepository matrices;
    private final ExamTemplateRepository templates;
    private final QuestionCatalogPort questions;
    private final ExamEventPublisher events;
    private final Clock clock;
    private final int maxVersionCount;

    @Autowired
    public ExamGenerationService(ExamRepository exams, ExamMatrixRepository matrices,
                                 ExamTemplateRepository templates, QuestionCatalogPort questions,
                                 ExamEventPublisher events, Clock clock,
                                 @Value("${exam.generation.max-version-count:20}") int maxVersionCount) {
        this.exams = exams; this.matrices = matrices; this.templates = templates; this.questions = questions;
        this.events = events; this.clock = clock; this.maxVersionCount = maxVersionCount;
    }

    public ExamGenerationService(ExamRepository exams, ExamMatrixRepository matrices,
                                 ExamTemplateRepository templates, QuestionCatalogPort questions, Clock clock) {
        this(exams, matrices, templates, questions, (exam, user) -> { }, clock, 20);
    }

    @Transactional
    public Exam generate(String name, String examCode, int durationMinutes, UUID matrixId,
                         UUID templateId, Actor actor) {
        return generate(new GenerateCommand(name, examCode, durationMinutes, matrixId, templateId,
                1, 1, false, false, false), actor);
    }

    @Transactional
    public Exam generate(GenerateCommand command, Actor actor) {
        int versionCount = validateRange(command.startCode(), command.endCode());
        ExamMatrix matrix = matrix(command.matrixId(), actor);
        validateTemplate(command.templateId(), matrix.id());
        List<LoadedBucket> buckets = loadBuckets(matrix, actor.bearerToken());
        CapacityAnalysis capacity = analyze(matrix, command.startCode(), command.endCode(), buckets);
        requireSufficient(capacity);

        UUID examId = UUID.randomUUID();
        Instant now = Instant.now(clock);
        Map<UUID, Integer> usage = new HashMap<>();
        Map<UUID, List<UUID>> fixedSelection = new HashMap<>();
        Map<UUID, QuestionCatalogPort.QuestionDetails> details = new HashMap<>();
        List<ExamVersion> versions = new ArrayList<>(versionCount);
        for (long value = command.startCode(); value <= command.endCode(); value++) {
            int code = (int) value;
            versions.add(buildVersion(examId, code, matrix, buckets, command, actor, usage,
                    fixedSelection, details, now));
        }
        versions.forEach(version -> validateVersion(matrix, version));
        boolean reuseWarning = command.allowQuestionReplacement() && capacity.estimatedReuseCount() > 0;
        Exam result = exams.save(new Exam(examId, command.name(), normalizeCode(command.examCode()),
                command.durationMinutes(), ExamStatus.ACTIVE, matrix.facultyId(), matrix.subjectId(), matrix.id(),
                command.templateId(), actor.userId(), command.startCode(), command.endCode(),
                command.shuffleQuestions(), command.shuffleAnswers(), command.allowQuestionReplacement(),
                reuseWarning, versions, now, now));
        events.generated(result, actor.userId());
        return result;
    }

    @Transactional(readOnly = true)
    public CapacityAnalysis capacity(UUID matrixId, int startCode, int endCode, Actor actor) {
        validateRange(startCode, endCode);
        ExamMatrix matrix = matrix(matrixId, actor);
        return analyze(matrix, startCode, endCode, loadBuckets(matrix, actor.bearerToken()));
    }

    @Transactional
    public Exam regenerate(UUID examId, Actor actor) {
        Exam exam = get(examId, actor);
        ExamMatrix matrix = matrices.findById(exam.matrixId())
                .orElseThrow(() -> new NotFoundException("Exam matrix not found"));
        int currentCode = exam.versions().stream().mapToInt(ExamVersion::versionCode).max().orElse(0);
        if (currentCode == Integer.MAX_VALUE) throw new IllegalArgumentException("No next version code is available");
        int nextCode = currentCode + 1;
        List<LoadedBucket> buckets = loadBuckets(matrix, actor.bearerToken());
        requireSufficient(analyze(matrix, nextCode, nextCode, buckets));
        Map<UUID, Integer> usage = existingUsage(exam.versions());
        Map<UUID, List<UUID>> fixed = fixedSelection(exam.versions());
        Map<UUID, QuestionCatalogPort.QuestionDetails> details = new HashMap<>();
        GenerateCommand command = new GenerateCommand(exam.name(), exam.examCode(), exam.durationMinutes(),
                exam.matrixId(), exam.templateId(), nextCode, nextCode, exam.shuffleQuestions(),
                exam.shuffleAnswers(), exam.allowQuestionReplacement());
        Instant now = Instant.now(clock);
        ExamVersion version = buildVersion(exam.id(), nextCode, matrix, buckets, command, actor, usage, fixed, details, now);
        validateVersion(matrix, version);
        List<ExamVersion> versions = new ArrayList<>(exam.versions());
        versions.add(version);
        Exam updated = new Exam(exam.id(), exam.name(), exam.examCode(), exam.durationMinutes(), exam.status(),
                exam.facultyId(), exam.subjectId(), exam.matrixId(), exam.templateId(), exam.createdBy(),
                exam.versionStartCode(), nextCode, exam.shuffleQuestions(), exam.shuffleAnswers(),
                exam.allowQuestionReplacement(), exam.reuseWarning()
                        || (exam.allowQuestionReplacement() && reused(versions)), versions,
                exam.createdAt(), now);
        return exams.save(updated);
    }

    @Transactional(readOnly = true)
    public List<Exam> list(Actor actor) {
        MatrixService.role(actor);
        return exams.findByFaculty(actor.facultyId());
    }

    @Transactional(readOnly = true)
    public Exam get(UUID id, Actor actor) {
        Exam exam = exams.findById(id).orElseThrow(() -> new NotFoundException("Exam not found"));
        MatrixService.scope(actor, exam.facultyId());
        return exam;
    }

    private ExamVersion buildVersion(UUID examId, int versionCode, ExamMatrix matrix,
                                     List<LoadedBucket> buckets, GenerateCommand command, Actor actor,
                                     Map<UUID, Integer> usage, Map<UUID, List<UUID>> fixedSelection,
                                     Map<UUID, QuestionCatalogPort.QuestionDetails> details, Instant now) {
        long seed = generationSeed(examId, versionCode);
        Set<UUID> usedInVersion = new HashSet<>();
        List<SelectedQuestion> selected = new ArrayList<>();
        for (LoadedBucket bucket : buckets) {
            List<QuestionCatalogPort.QuestionCandidate> values = selectCandidates(bucket, command, seed,
                    usage, usedInVersion, fixedSelection);
            for (QuestionCatalogPort.QuestionCandidate candidate : values) {
                usedInVersion.add(candidate.id());
                usage.merge(candidate.id(), 1, Integer::sum);
                QuestionCatalogPort.QuestionDetails question = details.computeIfAbsent(candidate.id(),
                        id -> questions.question(id, actor.bearerToken()));
                selected.add(new SelectedQuestion(candidate.id(), bucket.rule().id(), optionOrder(question,
                        command.shuffleAnswers(), seed)));
            }
        }
        if (command.shuffleQuestions()) {
            selected.sort(Comparator.comparingLong(value -> stableRank(seed, value.questionId(), value.ruleId())));
        }
        List<ExamQuestionReference> references = new ArrayList<>();
        for (int index = 0; index < selected.size(); index++) {
            SelectedQuestion value = selected.get(index);
            references.add(new ExamQuestionReference(UUID.randomUUID(), value.questionId(), index + 1,
                    value.ruleId(), value.options()));
        }
        return new ExamVersion(UUID.randomUUID(), versionCode, seed, actor.userId(), references, now);
    }

    private List<QuestionCatalogPort.QuestionCandidate> selectCandidates(LoadedBucket bucket,
            GenerateCommand command, long seed, Map<UUID, Integer> usage, Set<UUID> usedInVersion,
            Map<UUID, List<UUID>> fixedSelection) {
        int required = bucket.rule().questionCount();
        Map<UUID, QuestionCatalogPort.QuestionCandidate> byId = bucket.candidates().stream()
                .collect(Collectors.toMap(QuestionCatalogPort.QuestionCandidate::id, Function.identity()));
        List<QuestionCatalogPort.QuestionCandidate> available = bucket.candidates().stream()
                .filter(candidate -> !usedInVersion.contains(candidate.id())).toList();
        List<QuestionCatalogPort.QuestionCandidate> selected;
        if (!command.allowQuestionReplacement() && fixedSelection.containsKey(bucket.rule().id())) {
            selected = fixedSelection.get(bucket.rule().id()).stream().map(byId::get).filter(Objects::nonNull)
                    .filter(candidate -> !usedInVersion.contains(candidate.id())).toList();
        } else {
            selected = available.stream().sorted(Comparator
                    .comparingInt((QuestionCatalogPort.QuestionCandidate candidate) ->
                            command.allowQuestionReplacement() ? usage.getOrDefault(candidate.id(), 0) : 0)
                    .thenComparingLong(candidate -> stableRank(seed, bucket.rule().id(), candidate.id()))
                    .thenComparing(candidate -> candidate.id().toString())).limit(required).toList();
            if (!command.allowQuestionReplacement())
                fixedSelection.put(bucket.rule().id(), selected.stream().map(QuestionCatalogPort.QuestionCandidate::id).toList());
        }
        if (selected.size() < required) {
            throw shortage(bucket.rule(), required, selected.size());
        }
        return selected;
    }

    private List<ExamOptionReference> optionOrder(QuestionCatalogPort.QuestionDetails question,
                                                   boolean shuffleAnswers, long seed) {
        List<QuestionCatalogPort.Option> options = new ArrayList<>(question.options());
        options.sort(Comparator.comparingInt(QuestionCatalogPort.Option::sortOrder)
                .thenComparing(option -> option.id().toString()));
        if (shuffleAnswers && question.answerShuffleAllowed()) {
            options.sort(Comparator.comparingLong(option -> stableRank(seed ^ question.id().getMostSignificantBits(),
                    question.id(), option.id())));
        }
        List<ExamOptionReference> result = new ArrayList<>();
        for (int index = 0; index < options.size(); index++)
            result.add(new ExamOptionReference(UUID.randomUUID(), options.get(index).id(), index + 1));
        return result;
    }

    private List<LoadedBucket> loadBuckets(ExamMatrix matrix, String token) {
        List<LoadedBucket> buckets = new ArrayList<>();
        for (ExamMatrixRule rule : matrix.rules()) {
            List<QuestionCatalogPort.QuestionCandidate> candidates = questions.approvedQuestions(matrix.facultyId(),
                    matrix.subjectId(), rule.chapterId(), rule.topicId(), rule.knowledgeItemId(),
                    rule.difficulty(), token).stream().filter(candidate -> matches(matrix, rule, candidate))
                    .collect(Collectors.toMap(QuestionCatalogPort.QuestionCandidate::id, Function.identity(),
                            (left, right) -> left, LinkedHashMap::new)).values().stream().toList();
            buckets.add(new LoadedBucket(rule, candidates));
        }
        return buckets;
    }

    private boolean matches(ExamMatrix matrix, ExamMatrixRule rule, QuestionCatalogPort.QuestionCandidate question) {
        return "APPROVED".equals(question.status()) && matrix.facultyId().equals(question.facultyId())
                && matrix.subjectId().equals(question.subjectId()) && rule.chapterId().equals(question.chapterId())
                && Objects.equals(rule.topicId(), question.topicId())
                && (rule.knowledgeItemId() == null || Objects.equals(rule.knowledgeItemId(), question.knowledgeItemId()))
                && rule.difficulty() == question.difficulty();
    }

    private CapacityAnalysis analyze(ExamMatrix matrix, int startCode, int endCode, List<LoadedBucket> buckets) {
        int versions = endCode - startCode + 1;
        Set<UUID> allCandidates = new HashSet<>();
        List<BucketCapacity> capacities = new ArrayList<>();
        List<BucketShortage> shortages = new ArrayList<>();
        long totalSelections = 0;
        long estimatedUnique = 0;
        for (LoadedBucket bucket : buckets) {
            int required = bucket.rule().questionCount();
            int available = bucket.candidates().size();
            int missing = Math.max(0, required - available);
            BucketCapacity value = new BucketCapacity(bucket.rule().id(), bucket.rule().chapterId(),
                    bucket.rule().topicId(), bucket.rule().knowledgeItemId(), bucket.rule().difficulty(),
                    required, available, missing);
            capacities.add(value);
            if (missing > 0) shortages.add(new BucketShortage(value.ruleId(), value.chapterId(), value.topicId(),
                    value.knowledgeItemId(), value.difficulty(), required, available, missing));
            bucket.candidates().forEach(candidate -> allCandidates.add(candidate.id()));
            long selections = (long) required * versions;
            totalSelections += selections;
            estimatedUnique += Math.min(selections, available);
        }
        long estimatedReuse = Math.max(0, totalSelections - estimatedUnique);
        double reuseRate = totalSelections == 0 ? 0 : Math.round(estimatedReuse * 1000d / totalSelections) / 10d;
        String warning = estimatedReuse == 0 ? null
                : "Ngân hàng hiện chưa đủ câu hỏi để tạo các mã đề hoàn toàn khác nhau. Hệ thống sẽ tối thiểu hóa số câu hỏi trùng lặp.";
        return new CapacityAnalysis(matrix.id(), startCode, endCode, versions, matrix.totalQuestions(),
                maxVersionCount, allCandidates.size(), shortages.isEmpty(), estimatedReuse == 0,
                estimatedUnique, totalSelections, estimatedReuse, reuseRate, warning, capacities, shortages);
    }

    private void validateVersion(ExamMatrix matrix, ExamVersion version) {
        if (version.questions().size() != matrix.totalQuestions())
            throw new InvalidMatrixException("Generated version count is inconsistent with matrix");
        Map<UUID, Long> counts = version.questions().stream()
                .collect(Collectors.groupingBy(ExamQuestionReference::matrixRuleId, Collectors.counting()));
        for (ExamMatrixRule rule : matrix.rules()) {
            if (counts.getOrDefault(rule.id(), 0L) != rule.questionCount())
                throw new InvalidMatrixException("Generated version does not satisfy matrix rule " + rule.id());
        }
    }

    private ExamMatrix matrix(UUID matrixId, Actor actor) {
        ExamMatrix matrix = matrices.findById(matrixId)
                .orElseThrow(() -> new NotFoundException("Exam matrix not found"));
        MatrixService.scope(actor, matrix.facultyId());
        return matrix;
    }

    private void validateTemplate(UUID templateId, UUID matrixId) {
        if (templateId == null) return;
        var template = templates.findById(templateId)
                .orElseThrow(() -> new NotFoundException("Exam template not found"));
        if (!template.matrixId().equals(matrixId)) throw new InvalidMatrixException("Template belongs to another matrix");
    }

    private int validateRange(int startCode, int endCode) {
        long count = (long) endCode - startCode + 1;
        if (startCode < 1 || endCode < startCode) throw new IllegalArgumentException("startCode must be <= endCode");
        if (count > maxVersionCount)
            throw new IllegalArgumentException("Version count exceeds configured maximum of " + maxVersionCount);
        return (int) count;
    }

    private void requireSufficient(CapacityAnalysis capacity) {
        if (!capacity.canGenerate()) {
            BucketShortage shortage = capacity.shortages().getFirst();
            throw shortage(new ExamMatrixRule(shortage.ruleId(), shortage.chapterId(), shortage.topicId(),
                    shortage.knowledgeItemId(), shortage.difficulty(), shortage.required()),
                    shortage.required(), shortage.available());
        }
    }

    private InsufficientQuestionsException shortage(ExamMatrixRule rule, int required, int available) {
        return new InsufficientQuestionsException("Insufficient APPROVED questions for chapter=" + rule.chapterId()
                + ", topic=" + rule.topicId() + ", knowledgeItem=" + rule.knowledgeItemId()
                + ", difficulty=" + rule.difficulty() + ": required=" + required + ", available=" + available
                + ", missing=" + Math.max(0, required - available));
    }

    private static String normalizeCode(String code) {
        if (code == null || code.isBlank()) throw new IllegalArgumentException("Exam code is required");
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private static long generationSeed(UUID examId, int versionCode) {
        return mix(examId.getMostSignificantBits() ^ examId.getLeastSignificantBits() ^ ((long) versionCode << 32));
    }

    private static long stableRank(long seed, UUID first, UUID second) {
        return mix(seed ^ first.getMostSignificantBits() ^ first.getLeastSignificantBits()
                ^ second.getMostSignificantBits() ^ second.getLeastSignificantBits());
    }

    private static long mix(long value) {
        value ^= value >>> 33; value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33; value *= 0xc4ceb9fe1a85ec53L;
        return value ^ value >>> 33;
    }

    private static Map<UUID, Integer> existingUsage(List<ExamVersion> versions) {
        Map<UUID, Integer> usage = new HashMap<>();
        versions.forEach(version -> version.questions().forEach(question -> usage.merge(question.questionId(), 1, Integer::sum)));
        return usage;
    }

    private static Map<UUID, List<UUID>> fixedSelection(List<ExamVersion> versions) {
        if (versions.isEmpty()) return new HashMap<>();
        return versions.getFirst().questions().stream().collect(Collectors.groupingBy(ExamQuestionReference::matrixRuleId,
                Collectors.mapping(ExamQuestionReference::questionId, Collectors.toList())));
    }

    private static boolean reused(List<ExamVersion> versions) {
        Set<UUID> unique = new HashSet<>();
        long total = 0;
        for (ExamVersion version : versions) for (ExamQuestionReference question : version.questions()) {
            total++; unique.add(question.questionId());
        }
        return unique.size() < total;
    }

    private record LoadedBucket(ExamMatrixRule rule, List<QuestionCatalogPort.QuestionCandidate> candidates) { }
    private record SelectedQuestion(UUID questionId, UUID ruleId, List<ExamOptionReference> options) { }

    public record GenerateCommand(String name, String examCode, int durationMinutes, UUID matrixId, UUID templateId,
                                  int startCode, int endCode, boolean shuffleQuestions, boolean shuffleAnswers,
                                  boolean allowQuestionReplacement) { }
    public record BucketCapacity(UUID ruleId, UUID chapterId, UUID topicId, UUID knowledgeItemId,
                                 Difficulty difficulty, int required, int available, int missing) { }
    public record BucketShortage(UUID ruleId, UUID chapterId, UUID topicId, UUID knowledgeItemId,
                                 Difficulty difficulty, int required, int available, int missing) { }
    public record CapacityAnalysis(UUID matrixId, int startCode, int endCode, int numberOfVersions,
                                   int questionsPerVersion, int maxVersionCount, int approvedCandidateCount,
                                   boolean canGenerate, boolean completeUniquenessPossible,
                                   long estimatedUniqueQuestionCount, long totalQuestionSelections,
                                   long estimatedReuseCount, double estimatedReuseRate, String warning,
                                   List<BucketCapacity> buckets, List<BucketShortage> shortages) { }
}
