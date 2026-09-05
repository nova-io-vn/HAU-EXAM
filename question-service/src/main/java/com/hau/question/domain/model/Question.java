package com.hau.question.domain.model;

import com.hau.question.domain.exception.InvalidTransitionException;
import java.time.Instant;
import java.util.*;

public final class Question {
    private final UUID id;
    private final String facultyId;
    private final UUID subjectId;
    private final UUID chapterId;
    private final UUID topicId;
    private String content;
    private String imageUrl;
    private String storageKey;
    private QuestionType type;
    private Difficulty difficulty;
    private QuestionStatus status;
    private final QuestionSource source;
    private final String aiSourceId;
    private final UUID createdBy;
    private final Instant createdAt;
    private Instant updatedAt;
    private List<QuestionOption> options;
    private final List<QuestionReviewHistory> reviewHistory;

    public Question(UUID id, String facultyId, UUID subjectId, UUID chapterId, UUID topicId, String content,
                    String imageUrl, String storageKey, QuestionType type, Difficulty difficulty,
                    QuestionStatus status, QuestionSource source, String aiSourceId, UUID createdBy,
                    Instant createdAt, Instant updatedAt, List<QuestionOption> options,
                    List<QuestionReviewHistory> reviewHistory) {
        this.id = Objects.requireNonNull(id); this.facultyId = required(facultyId, "facultyId");
        this.subjectId = Objects.requireNonNull(subjectId); this.chapterId = Objects.requireNonNull(chapterId);
        this.topicId = topicId; this.content = required(content, "content"); this.imageUrl = imageUrl;
        this.storageKey = storageKey; this.type = Objects.requireNonNull(type);
        this.difficulty = Objects.requireNonNull(difficulty); this.status = Objects.requireNonNull(status);
        this.source = Objects.requireNonNull(source); this.aiSourceId = aiSourceId;
        this.createdBy = Objects.requireNonNull(createdBy); this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt); this.options = List.copyOf(options);
        this.reviewHistory = new ArrayList<>(reviewHistory); validateOptions(type, this.options);
        if (source == QuestionSource.AI && status == QuestionStatus.APPROVED)
            throw new IllegalArgumentException("AI question cannot be imported as approved");
    }

    public static Question create(UUID id, String facultyId, UUID subjectId, UUID chapterId, UUID topicId,
                                  String content, String imageUrl, String storageKey, QuestionType type,
                                  Difficulty difficulty, QuestionSource source, String aiSourceId,
                                  UUID createdBy, List<QuestionOption> options, Instant now) {
        return new Question(id, facultyId, subjectId, chapterId, topicId, content, imageUrl, storageKey,
            type, difficulty, QuestionStatus.DRAFT, source, aiSourceId, createdBy, now, now, options, List.of());
    }

    public void edit(String content, String imageUrl, String storageKey, QuestionType type,
                     Difficulty difficulty, List<QuestionOption> options, Instant now) {
        if (status != QuestionStatus.DRAFT && status != QuestionStatus.NEED_REVISION)
            throw new InvalidTransitionException("Question cannot be edited in status " + status);
        validateOptions(type, options); this.content = required(content, "content"); this.imageUrl = imageUrl;
        this.storageKey = storageKey; this.type = type; this.difficulty = difficulty; this.options = List.copyOf(options);
        this.status = QuestionStatus.DRAFT; this.updatedAt = now;
    }
    public void submit(Instant now) { requireStatus(QuestionStatus.DRAFT); status = QuestionStatus.PENDING_REVIEW; updatedAt = now; }
    public void approve(UUID reviewerId, String comment, Instant now) { review(QuestionStatus.APPROVED, ReviewAction.APPROVED, reviewerId, comment, now); }
    public void reject(UUID reviewerId, String reason, Instant now) { required(reason, "reason"); review(QuestionStatus.REJECTED, ReviewAction.REJECTED, reviewerId, reason, now); }
    public void requestRevision(UUID reviewerId, String reason, Instant now) { required(reason, "reason"); review(QuestionStatus.NEED_REVISION, ReviewAction.REVISION_REQUESTED, reviewerId, reason, now); }
    public void archive(Instant now) { if (status == QuestionStatus.PENDING_REVIEW) throw new InvalidTransitionException("Pending question cannot be archived"); status = QuestionStatus.ARCHIVED; updatedAt = now; }
    private void review(QuestionStatus target, ReviewAction action, UUID reviewer, String comment, Instant now) {
        requireStatus(QuestionStatus.PENDING_REVIEW); status = target; updatedAt = now;
        reviewHistory.add(new QuestionReviewHistory(UUID.randomUUID(), reviewer, action, comment, now));
    }
    private void requireStatus(QuestionStatus expected) { if (status != expected) throw new InvalidTransitionException("Expected " + expected + " but was " + status); }
    private static String required(String value, String field) { if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required"); return value.trim(); }
    private static void validateOptions(QuestionType type, List<QuestionOption> options) {
        if (options == null || options.size() < 2) throw new IllegalArgumentException("At least two options are required");
        long labels = options.stream().map(QuestionOption::label).map(String::toUpperCase).distinct().count();
        if (labels != options.size()) throw new IllegalArgumentException("Option labels must be unique");
        long correct = options.stream().filter(QuestionOption::correct).count();
        if (correct == 0 || (type != QuestionType.MULTIPLE_CHOICE && correct != 1)) throw new IllegalArgumentException("Invalid correct option count");
        if (type == QuestionType.TRUE_FALSE && options.size() != 2) throw new IllegalArgumentException("TRUE_FALSE requires two options");
    }
    public UUID id(){return id;} public String facultyId(){return facultyId;} public UUID subjectId(){return subjectId;}
    public UUID chapterId(){return chapterId;} public UUID topicId(){return topicId;} public String content(){return content;}
    public String imageUrl(){return imageUrl;} public String storageKey(){return storageKey;} public QuestionType type(){return type;}
    public Difficulty difficulty(){return difficulty;} public QuestionStatus status(){return status;} public QuestionSource source(){return source;}
    public String aiSourceId(){return aiSourceId;} public UUID createdBy(){return createdBy;} public Instant createdAt(){return createdAt;}
    public Instant updatedAt(){return updatedAt;} public List<QuestionOption> options(){return options;} public List<QuestionReviewHistory> reviewHistory(){return List.copyOf(reviewHistory);}
}
