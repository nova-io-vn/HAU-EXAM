package com.questionservice.application.port.out;
import com.questionservice.domain.model.*; import java.util.*;
public interface CatalogRepository {
 Subject saveSubject(Subject value); Optional<Subject> findSubject(UUID id); List<Subject> findSubjects(String facultyId); void deleteSubject(UUID id);
 Chapter saveChapter(Chapter value); Optional<Chapter> findChapter(UUID id); List<Chapter> findChapters(UUID subjectId); void deleteChapter(UUID id);
 Topic saveTopic(Topic value); Optional<Topic> findTopic(UUID id); List<Topic> findTopics(UUID chapterId); void deleteTopic(UUID id);
}
