package com.questionservice.presentation.response;

import com.questionservice.domain.model.*;

import java.time.Instant;
import java.util.UUID;
import java.util.Set;
import com.questionservice.application.model.LecturerProfile;

public final class CatalogResponse {
    private CatalogResponse() {
    }

    public record SubjectView(UUID id, String facultyId, String managingFacultyId, Set<String> participatingFacultyIds, String code, String name, Instant createdAt,
                              Instant updatedAt) {
        public static SubjectView from(Subject s) {
            return new SubjectView(s.id(), s.managingFacultyId(), s.managingFacultyId(), s.participatingFacultyIds(), s.code(), s.name(), s.createdAt(), s.updatedAt());
        }
    }

    public record LecturerView(UUID userId, String lecturerCode, String fullName, String facultyId,
                               String academicRank, String academicDegree, String avatarUrl) {
        public static LecturerView from(LecturerProfile profile) {
            return new LecturerView(profile.userId(), profile.lecturerCode(), profile.fullName(), profile.facultyId(),
                    profile.academicRank(), profile.academicDegree(), profile.avatarUrl());
        }
    }

    public record ChapterView(UUID id, UUID subjectId, String code, String name, int ordinal, Instant createdAt,
                              Instant updatedAt) {
        public static ChapterView from(Chapter c) {
            return new ChapterView(c.id(), c.subjectId(), c.code(), c.name(), c.ordinal(), c.createdAt(), c.updatedAt());
        }
    }

    public record TopicView(UUID id, UUID chapterId, String code, String name, Instant createdAt, Instant updatedAt) {
        public static TopicView from(Topic t) {
            return new TopicView(t.id(), t.chapterId(), t.code(), t.name(), t.createdAt(), t.updatedAt());
        }
    }
}
