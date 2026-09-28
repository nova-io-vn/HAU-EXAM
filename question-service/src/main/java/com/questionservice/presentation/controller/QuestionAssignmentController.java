package com.questionservice.presentation.controller;

import com.questionservice.application.model.*;
import com.questionservice.application.service.QuestionAssignmentService;
import com.questionservice.presentation.response.ApiResponse;
import com.questionservice.presentation.support.ActorResolver;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/question-assignments")
public class QuestionAssignmentController {
    private final QuestionAssignmentService service; private final ActorResolver actors;
    public QuestionAssignmentController(QuestionAssignmentService service,ActorResolver actors){this.service=service;this.actors=actors;}

    @GetMapping @PreAuthorize("hasAnyRole('SUBJECT_ADMIN','USER')")
    public ApiResponse<List<View>> list(@AuthenticationPrincipal Jwt jwt){return ApiResponse.ok(service.list(actors.from(jwt)).stream().map(View::from).toList());}
    @GetMapping("/{id}") @PreAuthorize("hasAnyRole('SUBJECT_ADMIN','USER')")
    public ApiResponse<View> get(@PathVariable UUID id,@AuthenticationPrincipal Jwt jwt){return ApiResponse.ok(View.from(service.get(id,actors.from(jwt))));}
    @PostMapping @PreAuthorize("hasRole('SUBJECT_ADMIN')")
    public ApiResponse<View> create(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody Request r){return ApiResponse.ok(View.from(service.create(actors.from(jwt),r.command())));}
    @PutMapping("/{id}") @PreAuthorize("hasRole('SUBJECT_ADMIN')")
    public ApiResponse<View> update(@PathVariable UUID id,@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody Request r){return ApiResponse.ok(View.from(service.update(id,actors.from(jwt),r.command())));}

    public record Request(@NotNull UUID subjectId,UUID chapterId,UUID topicId,UUID knowledgeItemId,@NotNull UUID lecturerId,
                          @Min(1) int requiredQuestionCount,@Min(0) int requiredEasy,@Min(0) int requiredMedium,@Min(0) int requiredHard,
                          @NotNull LocalDate deadline,@Size(max=1000) String note){
        QuestionAssignmentService.Command command(){return new QuestionAssignmentService.Command(subjectId,chapterId,topicId,knowledgeItemId,lecturerId,requiredQuestionCount,requiredEasy,requiredMedium,requiredHard,deadline,note);}
    }
    public record View(UUID id,String facultyId,UUID subjectId,UUID chapterId,UUID topicId,UUID knowledgeItemId,
                       UUID lecturerId,UUID assignedBy,int requiredQuestionCount,int requiredEasy,int requiredMedium,int requiredHard,
                       LocalDate deadline,String note,String status,AssignmentProgress progress,Instant createdAt,Instant updatedAt){
        static View from(QuestionAssignmentView value){var a=value.assignment();return new View(a.id(),a.facultyId(),a.subjectId(),a.chapterId(),a.topicId(),a.knowledgeItemId(),a.lecturerId(),a.assignedBy(),a.requiredQuestionCount(),a.requiredEasy(),a.requiredMedium(),a.requiredHard(),a.deadline(),a.note(),value.status().name(),value.progress(),a.createdAt(),a.updatedAt());}
    }
}
