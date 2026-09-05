package com.hau.question.presentation.request;
import jakarta.validation.constraints.*; import java.util.UUID;
public final class CatalogRequests {private CatalogRequests(){} public record SubjectRequest(@NotBlank String facultyId,@NotBlank String code,@NotBlank String name){} public record ChapterRequest(@NotNull UUID subjectId,@NotBlank String code,@NotBlank String name,@Min(0)int ordinal){} public record TopicRequest(@NotNull UUID chapterId,@NotBlank String code,@NotBlank String name){}}
