package com.examservice.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.examservice.application.port.out.ExportMetadataPort;
import com.examservice.application.port.out.QuestionCatalogPort;
import com.examservice.application.service.ExamPdfService;
import com.examservice.domain.model.*;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.*;
import java.util.zip.ZipInputStream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

class ExamPdfServiceTest {
    @Test
    void studentPdfHidesAnswersAndAnswerKeyUsesPersistedDisplayOrder() throws Exception {
        UUID questionId = UUID.randomUUID(), wrongA = UUID.randomUUID(), correct = UUID.randomUUID(), wrongC = UUID.randomUUID();
        QuestionCatalogPort questions = mock(QuestionCatalogPort.class);
        when(questions.question(eq(questionId), anyString())).thenReturn(new QuestionCatalogPort.QuestionDetails(
                questionId, "SINGLE_CHOICE", "Thủ đô Việt Nam?", List.of(
                new QuestionCatalogPort.Option(wrongA, "A", "Huế", false, 1),
                new QuestionCatalogPort.Option(correct, "B", "Hà Nội", true, 2),
                new QuestionCatalogPort.Option(wrongC, "C", "Đà Nẵng", false, 3))));
        ExportMetadataPort metadata = mock(ExportMetadataPort.class);
        when(metadata.exporter("token")).thenReturn(new ExportMetadataPort.Exporter(UUID.randomUUID(), "Nguyễn Văn A", "GV01"));
        when(metadata.facultyName("CNTT")).thenReturn("Công nghệ thông tin");
        when(metadata.subject(any(), eq("token"))).thenReturn(new ExportMetadataPort.Subject("AI", "Trí tuệ nhân tạo"));
        ExamPdfService service = new ExamPdfService(questions, metadata);
        Exam exam = exam(questionId, List.of(
                new ExamOptionReference(UUID.randomUUID(), wrongC, 1),
                new ExamOptionReference(UUID.randomUUID(), wrongA, 2),
                new ExamOptionReference(UUID.randomUUID(), correct, 3)));

        String student = text(service.export(exam, 101, ExamPdfService.ExportType.STUDENT_EXAM, "token").bytes());
        String answer = text(service.export(exam, 101, ExamPdfService.ExportType.ANSWER_KEY, "token").bytes());

        assertTrue(student.contains("MÃ ĐỀ: 101") || student.contains("MA DE: 101"));
        assertTrue(student.contains("Người xuất đề") || student.contains("Nguoi xuat de"));
        assertFalse(student.contains("Câu 1: C") || student.contains("Cau 1: C"));
        assertTrue(answer.contains("Câu 1: C") || answer.contains("Cau 1: C"));
    }

    @Test
    void exportAllCreatesOneDeterministicallyNamedPdfPerVersion() throws Exception {
        UUID questionId = UUID.randomUUID(), optionId = UUID.randomUUID();
        QuestionCatalogPort questions = mock(QuestionCatalogPort.class);
        when(questions.question(any(), anyString())).thenReturn(new QuestionCatalogPort.QuestionDetails(questionId,
                "TRUE_FALSE", "Đúng?", List.of(new QuestionCatalogPort.Option(optionId, "A", "Đúng", true, 1))));
        ExportMetadataPort metadata = mock(ExportMetadataPort.class);
        when(metadata.exporter(anyString())).thenReturn(new ExportMetadataPort.Exporter(UUID.randomUUID(), "A", "GV"));
        when(metadata.facultyName(anyString())).thenReturn("CNTT");
        when(metadata.subject(any(), anyString())).thenReturn(new ExportMetadataPort.Subject("AI", "AI"));
        Exam base = exam(questionId, List.of(new ExamOptionReference(UUID.randomUUID(), optionId, 1)));
        ExamVersion second = new ExamVersion(UUID.randomUUID(), 102, 2L, UUID.randomUUID(),
                base.versions().getFirst().questions(), Instant.now());
        Exam exam = new Exam(base.id(), base.name(), base.examCode(), base.durationMinutes(), base.status(),
                base.facultyId(), base.subjectId(), base.matrixId(), null, base.createdBy(), 101, 102,
                false, false, true, false, List.of(base.versions().getFirst(), second), Instant.now(), Instant.now());

        byte[] zip = new ExamPdfService(questions, metadata)
                .exportAll(exam, ExamPdfService.ExportType.STUDENT_EXAM, "token").bytes();
        List<String> entries = new ArrayList<>();
        try (ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(zip))) {
            for (var entry = input.getNextEntry(); entry != null; entry = input.getNextEntry()) entries.add(entry.getName());
        }
        assertEquals(2, entries.size());
        assertTrue(entries.stream().anyMatch(name -> name.endsWith("_101.pdf")));
        assertTrue(entries.stream().anyMatch(name -> name.endsWith("_102.pdf")));
    }

    private static Exam exam(UUID questionId, List<ExamOptionReference> mappings) {
        UUID creator = UUID.randomUUID();
        ExamQuestionReference reference = new ExamQuestionReference(UUID.randomUUID(), questionId, 1,
                UUID.randomUUID(), mappings);
        ExamVersion version = new ExamVersion(UUID.randomUUID(), 101, 1L, creator, List.of(reference), Instant.now());
        return new Exam(UUID.randomUUID(), "Thi cuối kỳ", "AI-FINAL", 60, ExamStatus.ACTIVE, "CNTT",
                UUID.randomUUID(), UUID.randomUUID(), null, creator, 101, 101, false, true, true,
                false, List.of(version), Instant.now(), Instant.now());
    }

    private static String text(byte[] pdf) throws Exception {
        try (var document = Loader.loadPDF(pdf)) { return new PDFTextStripper().getText(document); }
    }
}
