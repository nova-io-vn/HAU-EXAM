package com.questionservice.presentation.controller;

import com.questionservice.application.service.CatalogService;
import com.questionservice.domain.model.*;
import com.questionservice.presentation.request.CatalogRequests.*;
import com.questionservice.presentation.response.*;
import com.questionservice.presentation.response.CatalogResponse.*;
import com.questionservice.presentation.support.ActorResolver;
import com.questionservice.infrastructure.security.InternalServiceTokenVerifier;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.*;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class CatalogController {
    private final CatalogService service;
    private final ActorResolver actors;
    private final InternalServiceTokenVerifier internalTokens;

    public CatalogController(CatalogService s, ActorResolver a, InternalServiceTokenVerifier internalTokens) {
        service = s; actors = a; this.internalTokens = internalTokens;
    }

    @GetMapping("/subjects")
    public ApiResponse<List<SubjectView>> subjects(@AuthenticationPrincipal Jwt j, @RequestParam(required = false) String facultyId) {
        return ApiResponse.ok(service.subjects(actors.from(j), facultyId).stream().map(SubjectView::from).toList());
    }

    @PostMapping("/subjects/{id}/lecturers")
    @PreAuthorize("hasRole('SUBJECT_ADMIN')")
    public ApiResponse<Void> assignLecturer(@PathVariable UUID id, @AuthenticationPrincipal Jwt j, @Valid @RequestBody LecturerAssignmentRequest r) {
        service.assignLecturer(id, r.userId(), actors.from(j));
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/subjects/{id}/lecturers/{userId}")
    @PreAuthorize("hasRole('SUBJECT_ADMIN')")
    public ApiResponse<Void> removeLecturer(@PathVariable UUID id, @PathVariable UUID userId, @AuthenticationPrincipal Jwt j) {
        service.removeLecturer(id, userId, actors.from(j));
        return ApiResponse.ok(null);
    }

    @GetMapping("/subjects/{id}/lecturers")
    public ApiResponse<List<LecturerView>> lecturers(@PathVariable UUID id, @AuthenticationPrincipal Jwt j) {
        return ApiResponse.ok(service.lecturers(id, actors.from(j)).stream().map(LecturerView::from).toList());
    }

    @GetMapping("/subjects/{id}/eligible-lecturers")
    @PreAuthorize("hasRole('SUBJECT_ADMIN')")
    public ApiResponse<List<LecturerView>> eligibleLecturers(@PathVariable UUID id,
                                                              @RequestParam(required = false) String keyword,
                                                              @AuthenticationPrincipal Jwt j) {
        return ApiResponse.ok(service.eligibleLecturers(id, keyword, actors.from(j)).stream().map(LecturerView::from).toList());
    }

    @GetMapping("/internal/subjects/{subjectId}/assignments/{userId}")
    public Boolean assigned(@PathVariable UUID subjectId, @PathVariable UUID userId, @RequestHeader(value = "X-Internal-Service-Token", required = false) String token) {
        if (!internalTokens.matches(token)) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid service token");
        return service.isAssigned(subjectId, userId);
    }

    @PostMapping("/internal/catalog-contexts")
    public List<CatalogService.CatalogContext> catalogContexts(
            @RequestHeader(value = "X-Internal-Service-Token", required = false) String token,
            @RequestBody List<CatalogContextLookup> lookups) {
        if (!internalTokens.matches(token)) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid service token");
        if (lookups == null || lookups.size() > 100) throw new IllegalArgumentException("Invalid catalog context batch");
        return lookups.stream().map(value -> service.context(value.subjectId(), value.chapterId(), value.topicId())).toList();
    }

    public record CatalogContextLookup(UUID subjectId, UUID chapterId, UUID topicId) { }

    @PostMapping("/subjects")
    @PreAuthorize("hasRole('SUBJECT_ADMIN')")
    public ApiResponse<SubjectView> createSubject(@AuthenticationPrincipal Jwt j, @Valid @RequestBody SubjectRequest r) {
        return ApiResponse.ok(SubjectView.from(service.saveSubject(null, r.code(), r.name(), r.managingFacultyId(), r.participatingFacultyIds(), actors.from(j))));
    }

    @PutMapping("/subjects/{id}")
    @PreAuthorize("hasRole('SUBJECT_ADMIN')")
    public ApiResponse<SubjectView> updateSubject(@PathVariable UUID id, @AuthenticationPrincipal Jwt j, @Valid @RequestBody SubjectRequest r) {
        return ApiResponse.ok(SubjectView.from(service.saveSubject(id, r.code(), r.name(), r.managingFacultyId(), r.participatingFacultyIds(), actors.from(j))));
    }

    @DeleteMapping("/subjects/{id}")
    @PreAuthorize("hasRole('SUBJECT_ADMIN')")
    public ApiResponse<Void> deleteSubject(@PathVariable UUID id, @AuthenticationPrincipal Jwt j) {
        service.deleteSubject(id, actors.from(j));
        return ApiResponse.ok(null);
    }

    @GetMapping("/chapters")
    public ApiResponse<List<ChapterView>> chapters(@AuthenticationPrincipal Jwt j, @RequestParam UUID subjectId) {
        return ApiResponse.ok(service.chapters(subjectId, actors.from(j)).stream().map(ChapterView::from).toList());
    }

    @PostMapping("/chapters")
    @PreAuthorize("hasRole('SUBJECT_ADMIN')")
    public ApiResponse<ChapterView> createChapter(@AuthenticationPrincipal Jwt j, @Valid @RequestBody ChapterRequest r) {
        return ApiResponse.ok(ChapterView.from(service.saveChapter(null, r.subjectId(), r.code(), r.name(), r.ordinal(), actors.from(j))));
    }

    @PutMapping("/chapters/{id}")
    @PreAuthorize("hasRole('SUBJECT_ADMIN')")
    public ApiResponse<ChapterView> updateChapter(@PathVariable UUID id, @AuthenticationPrincipal Jwt j, @Valid @RequestBody ChapterRequest r) {
        return ApiResponse.ok(ChapterView.from(service.saveChapter(id, r.subjectId(), r.code(), r.name(), r.ordinal(), actors.from(j))));
    }

    @DeleteMapping("/chapters/{id}")
    @PreAuthorize("hasRole('SUBJECT_ADMIN')")
    public ApiResponse<Void> deleteChapter(@PathVariable UUID id, @AuthenticationPrincipal Jwt j) {
        service.deleteChapter(id, actors.from(j));
        return ApiResponse.ok(null);
    }

    @GetMapping("/topics")
    public ApiResponse<List<TopicView>> topics(@AuthenticationPrincipal Jwt j, @RequestParam UUID chapterId) {
        return ApiResponse.ok(service.topics(chapterId, actors.from(j)).stream().map(TopicView::from).toList());
    }

    @PostMapping("/topics")
    @PreAuthorize("hasRole('SUBJECT_ADMIN')")
    public ApiResponse<TopicView> createTopic(@AuthenticationPrincipal Jwt j, @Valid @RequestBody TopicRequest r) {
        return ApiResponse.ok(TopicView.from(service.saveTopic(null, r.chapterId(), r.code(), r.name(), actors.from(j))));
    }

    @PutMapping("/topics/{id}")
    @PreAuthorize("hasRole('SUBJECT_ADMIN')")
    public ApiResponse<TopicView> updateTopic(@PathVariable UUID id, @AuthenticationPrincipal Jwt j, @Valid @RequestBody TopicRequest r) {
        return ApiResponse.ok(TopicView.from(service.saveTopic(id, r.chapterId(), r.code(), r.name(), actors.from(j))));
    }

    @DeleteMapping("/topics/{id}")
    @PreAuthorize("hasRole('SUBJECT_ADMIN')")
    public ApiResponse<Void> deleteTopic(@PathVariable UUID id, @AuthenticationPrincipal Jwt j) {
        service.deleteTopic(id, actors.from(j));
        return ApiResponse.ok(null);
    }

    @GetMapping("/knowledge-items")
    public ApiResponse<List<KnowledgeItemView>> knowledgeItems(@AuthenticationPrincipal Jwt j, @RequestParam UUID topicId) {
        return ApiResponse.ok(service.knowledgeItems(topicId, actors.from(j)).stream().map(KnowledgeItemView::from).toList());
    }

    @PostMapping("/knowledge-items")
    @PreAuthorize("hasRole('SUBJECT_ADMIN')")
    public ApiResponse<KnowledgeItemView> createKnowledgeItem(@AuthenticationPrincipal Jwt j, @Valid @RequestBody KnowledgeItemRequest r) {
        return ApiResponse.ok(KnowledgeItemView.from(service.saveKnowledgeItem(null, r.topicId(), r.code(), r.name(), r.ordinal(), r.targetEasy(), r.targetMedium(), r.targetHard(), actors.from(j))));
    }

    @PutMapping("/knowledge-items/{id}")
    @PreAuthorize("hasRole('SUBJECT_ADMIN')")
    public ApiResponse<KnowledgeItemView> updateKnowledgeItem(@PathVariable UUID id, @AuthenticationPrincipal Jwt j, @Valid @RequestBody KnowledgeItemRequest r) {
        return ApiResponse.ok(KnowledgeItemView.from(service.saveKnowledgeItem(id, r.topicId(), r.code(), r.name(), r.ordinal(), r.targetEasy(), r.targetMedium(), r.targetHard(), actors.from(j))));
    }

    @DeleteMapping("/knowledge-items/{id}")
    @PreAuthorize("hasRole('SUBJECT_ADMIN')")
    public ApiResponse<Void> deleteKnowledgeItem(@PathVariable UUID id, @AuthenticationPrincipal Jwt j) {
        service.deleteKnowledgeItem(id, actors.from(j));
        return ApiResponse.ok(null);
    }

    @PostMapping("/subjects/{id}/structure/import")
    @PreAuthorize("hasRole('SUBJECT_ADMIN')")
    public ApiResponse<Void> importStructure(@PathVariable UUID id,@AuthenticationPrincipal Jwt jwt,
                                             @Valid @RequestBody StructureImportRequest request) {
        service.importStructure(id, request.chapters().stream().map(chapter -> new CatalogService.StructureChapter(
                chapter.code(), chapter.name(), chapter.topics().stream().map(topic -> new CatalogService.StructureTopic(
                topic.code(), topic.name(), topic.items().stream().map(item -> new CatalogService.StructureItem(
                item.code(), item.name(), item.targetEasy(), item.targetMedium(), item.targetHard())).toList())).toList())).toList(), actors.from(jwt));
        return ApiResponse.ok(null);
    }

    public record StructureImportRequest(@NotEmpty List<@Valid StructureChapterRequest> chapters){}
    public record StructureChapterRequest(@NotBlank String code,@NotBlank String name,@NotEmpty List<@Valid StructureTopicRequest> topics){}
    public record StructureTopicRequest(@NotBlank String code,@NotBlank String name,@NotEmpty List<@Valid StructureItemRequest> items){}
    public record StructureItemRequest(@NotBlank String code,@NotBlank String name,@Min(0) int targetEasy,@Min(0) int targetMedium,@Min(0) int targetHard){}
}
