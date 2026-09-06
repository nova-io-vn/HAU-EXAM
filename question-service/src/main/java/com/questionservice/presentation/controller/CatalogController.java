package com.questionservice.presentation.controller;

import com.questionservice.application.service.CatalogService;
import com.questionservice.domain.model.*;
import com.questionservice.presentation.request.CatalogRequests.*;
import com.questionservice.presentation.response.*;
import com.questionservice.presentation.response.CatalogResponse.*;
import com.questionservice.presentation.support.ActorResolver;
import jakarta.validation.Valid;

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

    public CatalogController(CatalogService s, ActorResolver a) {
        service = s;
        actors = a;
    }

    @GetMapping("/subjects")
    public ApiResponse<List<SubjectView>> subjects(@AuthenticationPrincipal Jwt j, @RequestParam(required = false) String facultyId) {
        return ApiResponse.ok(service.subjects(actors.from(j), facultyId).stream().map(SubjectView::from).toList());
    }

    @PostMapping("/subjects")
    @PreAuthorize("hasRole('SUBJECT_ADMIN')")
    public ApiResponse<SubjectView> createSubject(@AuthenticationPrincipal Jwt j, @Valid @RequestBody SubjectRequest r) {
        return ApiResponse.ok(SubjectView.from(service.saveSubject(null, r.facultyId(), r.code(), r.name(), actors.from(j))));
    }

    @PutMapping("/subjects/{id}")
    @PreAuthorize("hasRole('SUBJECT_ADMIN')")
    public ApiResponse<SubjectView> updateSubject(@PathVariable UUID id, @AuthenticationPrincipal Jwt j, @Valid @RequestBody SubjectRequest r) {
        return ApiResponse.ok(SubjectView.from(service.saveSubject(id, r.facultyId(), r.code(), r.name(), actors.from(j))));
    }

    @DeleteMapping("/subjects/{id}")
    @PreAuthorize("hasRole('SUBJECT_ADMIN')")
    public ApiResponse<Void> deleteSubject(@PathVariable UUID id, @AuthenticationPrincipal Jwt j) {
        service.deleteSubject(id, actors.from(j));
        return ApiResponse.ok(null);
    }

    @GetMapping("/chapters")
    public ApiResponse<List<ChapterView>> chapters(@RequestParam UUID subjectId) {
        return ApiResponse.ok(service.chapters(subjectId).stream().map(ChapterView::from).toList());
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
    public ApiResponse<List<TopicView>> topics(@RequestParam UUID chapterId) {
        return ApiResponse.ok(service.topics(chapterId).stream().map(TopicView::from).toList());
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
}
