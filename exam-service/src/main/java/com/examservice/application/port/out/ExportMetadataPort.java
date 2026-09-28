package com.examservice.application.port.out;

import java.util.UUID;

public interface ExportMetadataPort {
    Exporter exporter(String bearerToken);
    String facultyName(String facultyCode);
    Subject subject(UUID subjectId, String bearerToken);
    record Exporter(UUID id,String fullName,String lecturerCode){}
    record Subject(String code,String name){}
}
