package com.userservice.application.dto;

import java.time.LocalDate;
import com.userservice.domain.model.AcademicDegree;
import com.userservice.domain.model.AcademicRank;
public record UpdateProfileCommand(String fullName, LocalDate dateOfBirth, String phone, String email, String address, String avatar, AcademicRank academicRank, AcademicDegree academicDegree) { }
