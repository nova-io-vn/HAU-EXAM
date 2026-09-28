package com.aiservice.application.port.out;

import java.util.*;

public interface CatalogDirectoryPort {
    List<CatalogContext> resolve(List<Lookup> lookups);
    record Lookup(UUID subjectId, UUID chapterId, UUID topicId) { }
    record CatalogContext(UUID subjectId, String subjectCode, String subjectName, String managingFacultyId,
                          UUID chapterId, String chapterCode, String chapterName,
                          UUID topicId, String topicCode, String topicName) { }
}
