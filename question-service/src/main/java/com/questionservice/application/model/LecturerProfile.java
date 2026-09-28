package com.questionservice.application.model;

import java.util.UUID;

public record LecturerProfile(UUID userId, String lecturerCode, String fullName, String facultyId, String role,
                              String status, String academicRank, String academicDegree, String avatarUrl) { }
