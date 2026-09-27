package com.questionservice.application.port.out;
import java.util.UUID;
public interface UserDirectoryPort { boolean isLecturerInFaculty(UUID userId, String facultyId); }
