package com.examservice.application.service;

import com.examservice.application.port.out.ExportMetadataPort;
import com.examservice.application.port.out.QuestionCatalogPort;
import com.examservice.domain.exception.DomainException;
import com.examservice.domain.model.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

@Service
public class ExamPdfService {
    private static final float MARGIN = 52;
    private static final float LINE = 17;
    private final QuestionCatalogPort questions;
    private final ExportMetadataPort metadata;

    public ExamPdfService(QuestionCatalogPort questions, ExportMetadataPort metadata) {
        this.questions = questions; this.metadata = metadata;
    }

    public ExportedPdf export(Exam exam, int versionCode, String token) {
        return export(exam, versionCode, ExportType.STUDENT_EXAM, token);
    }

    public ExportedPdf export(Exam exam, int versionCode, ExportType type, String token) {
        ExamVersion version = exam.versions().stream().filter(value -> value.versionCode() == versionCode)
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Exam version not found"));
        var exporter = metadata.exporter(token);
        var subject = metadata.subject(exam.subjectId(), token);
        String faculty = metadata.facultyName(exam.facultyId());
        Map<UUID, QuestionCatalogPort.QuestionDetails> details = new HashMap<>();
        List<Line> lines = header(exam, versionCode, type, faculty, subject.name());
        if (type == ExportType.STUDENT_EXAM) {
            appendStudentExam(lines, version, token, details);
        } else {
            appendAnswerKey(lines, version, token, details);
        }
        lines.add(Line.blank());
        lines.add(new Line("Người xuất đề: " + exporter.fullName(), true, 11, Alignment.LEFT));
        lines.add(new Line("Mã đề: " + versionCode, false, 11, Alignment.LEFT));
        lines.add(Line.blank());
        lines.add(new Line("____________________", false, 11, Alignment.LEFT));
        lines.add(new Line("Chữ ký", false, 10, Alignment.LEFT));
        byte[] body = render(lines, exam.name(), versionCode);
        String suffix = type == ExportType.ANSWER_KEY ? "_Answer_Key" : "";
        String filename = safe(subject.code()) + "_" + safe(exam.name()) + "_" + versionCode + suffix + ".pdf";
        return new ExportedPdf(body, filename);
    }

    public ExportedArchive exportAll(Exam exam, ExportType type, String token) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(output)) {
            for (ExamVersion version : exam.versions().stream()
                    .sorted(Comparator.comparingInt(ExamVersion::versionCode)).toList()) {
                ExportedPdf pdf = export(exam, version.versionCode(), type, token);
                zip.putNextEntry(new ZipEntry(pdf.filename()));
                zip.write(pdf.bytes());
                zip.closeEntry();
            }
            zip.finish();
            return new ExportedArchive(output.toByteArray(), safe(exam.name()) + "_All_Versions.zip");
        } catch (IOException exception) {
            throw new DomainException("Unable to generate exam archive");
        }
    }

    private List<Line> header(Exam exam, int versionCode, ExportType type, String faculty, String subject) {
        List<Line> lines = new ArrayList<>();
        lines.add(new Line("TRƯỜNG ĐẠI HỌC KIẾN TRÚC HÀ NỘI", true, 12, Alignment.CENTER));
        lines.add(new Line("KHOA " + faculty.toUpperCase(Locale.ROOT), true, 11, Alignment.CENTER));
        lines.add(Line.blank());
        lines.add(new Line(type == ExportType.ANSWER_KEY ? "ĐÁP ÁN" : "ĐỀ THI " + exam.name().toUpperCase(Locale.ROOT),
                true, 15, Alignment.CENTER));
        lines.add(new Line("Môn: " + subject, true, 11, Alignment.CENTER));
        lines.add(new Line("Thời gian: " + exam.durationMinutes() + " phút", false, 11, Alignment.CENTER));
        lines.add(new Line("MÃ ĐỀ: " + versionCode, true, 13, Alignment.CENTER));
        lines.add(Line.blank());
        return lines;
    }

    private void appendStudentExam(List<Line> lines, ExamVersion version, String token,
                                   Map<UUID, QuestionCatalogPort.QuestionDetails> details) {
        for (ExamQuestionReference reference : version.questions().stream()
                .sorted(Comparator.comparingInt(ExamQuestionReference::position)).toList()) {
            QuestionCatalogPort.QuestionDetails question = details.computeIfAbsent(reference.questionId(),
                    id -> questions.question(id, token));
            lines.add(new Line("Câu " + reference.position() + ": " + plain(question.content()), true, 11, Alignment.LEFT));
            List<QuestionCatalogPort.Option> ordered = orderedOptions(reference, question);
            for (int index = 0; index < ordered.size(); index++)
                lines.add(new Line("    " + displayLabel(index + 1) + ". " + plain(ordered.get(index).content()),
                        false, 11, Alignment.LEFT));
            lines.add(Line.blank());
        }
    }

    private void appendAnswerKey(List<Line> lines, ExamVersion version, String token,
                                 Map<UUID, QuestionCatalogPort.QuestionDetails> details) {
        for (ExamQuestionReference reference : version.questions().stream()
                .sorted(Comparator.comparingInt(ExamQuestionReference::position)).toList()) {
            QuestionCatalogPort.QuestionDetails question = details.computeIfAbsent(reference.questionId(),
                    id -> questions.question(id, token));
            List<QuestionCatalogPort.Option> ordered = orderedOptions(reference, question);
            List<String> correct = new ArrayList<>();
            for (int index = 0; index < ordered.size(); index++)
                if (ordered.get(index).correct()) correct.add(displayLabel(index + 1));
            if (correct.isEmpty()) throw new DomainException("Question has no correct answer: " + reference.questionId());
            lines.add(new Line("Câu " + reference.position() + ": " + String.join(", ", correct),
                    true, 11, Alignment.LEFT));
        }
    }

    private List<QuestionCatalogPort.Option> orderedOptions(ExamQuestionReference reference,
                                                             QuestionCatalogPort.QuestionDetails question) {
        Map<UUID, QuestionCatalogPort.Option> byId = question.options().stream()
                .collect(java.util.stream.Collectors.toMap(QuestionCatalogPort.Option::id, option -> option));
        if (reference.options().isEmpty()) {
            return question.options().stream().sorted(Comparator.comparingInt(QuestionCatalogPort.Option::sortOrder)
                    .thenComparing(option -> option.id().toString())).toList();
        }
        List<QuestionCatalogPort.Option> result = new ArrayList<>();
        for (ExamOptionReference mapping : reference.options().stream()
                .sorted(Comparator.comparingInt(ExamOptionReference::displayOrder)).toList()) {
            QuestionCatalogPort.Option option = byId.get(mapping.optionId());
            if (option == null) throw new DomainException("Persisted option is no longer available: " + mapping.optionId());
            result.add(option);
        }
        return result;
    }

    private byte[] render(List<Line> source, String examName, int versionCode) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Font font = font(document);
            PDPageContentStream stream = null;
            float y = 0;
            int pageNumber = 0;
            for (Line sourceLine : source) {
                for (String value : wrap(font.safe(sourceLine.text()), sourceLine.size() >= 14 ? 75 : 95)) {
                    if (stream == null || y < MARGIN + 35) {
                        if (stream != null) { footer(stream, font, pageNumber, versionCode); stream.close(); }
                        PDPage page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        stream = new PDPageContentStream(document, page);
                        y = PDRectangle.A4.getHeight() - MARGIN;
                        pageNumber++;
                    }
                    PDFont selectedFont = sourceLine.bold() ? font.bold() : font.regular();
                    float x = MARGIN;
                    if (sourceLine.alignment() == Alignment.CENTER) {
                        float width = selectedFont.getStringWidth(value) / 1000 * sourceLine.size();
                        x = Math.max(MARGIN, (PDRectangle.A4.getWidth() - width) / 2);
                    }
                    stream.beginText(); stream.setFont(selectedFont, sourceLine.size());
                    stream.newLineAtOffset(x, y); stream.showText(value); stream.endText();
                    y -= sourceLine.text().isBlank() ? LINE / 2 : LINE;
                }
            }
            if (stream != null) { footer(stream, font, pageNumber, versionCode); stream.close(); }
            document.getDocumentInformation().setTitle(examName + " - Mã đề " + versionCode);
            document.getDocumentInformation().setProducer("HAU Exam");
            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new DomainException("Unable to generate exam PDF");
        }
    }

    private void footer(PDPageContentStream stream, Font font, int page, int versionCode) throws IOException {
        stream.beginText(); stream.setFont(font.regular(), 9); stream.newLineAtOffset(MARGIN, 24);
        stream.showText("Mã đề " + versionCode); stream.endText();
        stream.beginText(); stream.setFont(font.regular(), 9); stream.newLineAtOffset(PDRectangle.A4.getWidth() - 100, 24);
        stream.showText("Trang " + page); stream.endText();
    }

    private Font font(PDDocument document) throws IOException {
        List<Path> paths = new ArrayList<>();
        String windows = System.getenv("WINDIR");
        if (windows != null) { paths.add(Path.of(windows, "Fonts", "arial.ttf")); paths.add(Path.of(windows, "Fonts", "arialbd.ttf")); }
        paths.add(Path.of("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"));
        Path regular = paths.stream().filter(Files::isRegularFile).findFirst().orElse(null);
        if (regular != null) {
            Path bold = regular.getFileName().toString().equalsIgnoreCase("arial.ttf")
                    ? regular.resolveSibling("arialbd.ttf") : regular.resolveSibling("DejaVuSans-Bold.ttf");
            PDType0Font normalFont = PDType0Font.load(document, Files.newInputStream(regular));
            PDType0Font boldFont = Files.isRegularFile(bold) ? PDType0Font.load(document, Files.newInputStream(bold)) : normalFont;
            return new Font(normalFont, boldFont, true);
        }
        return new Font(new PDType1Font(Standard14Fonts.FontName.HELVETICA),
                new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), false);
    }

    private static String plain(String value) {
        return (value == null ? "" : value).replaceAll("<[^>]+>", " ").replace("&nbsp;", " ")
                .replace("&amp;", "&").replaceAll("\\s+", " ").trim();
    }

    private static String safe(String value) {
        String result = Normalizer.normalize(value == null ? "exam" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replaceAll("[^A-Za-z0-9_-]+", "-")
                .replaceAll("^-+|-+$", "");
        return result.isBlank() ? "exam" : result;
    }

    private static String displayLabel(int order) {
        if (order < 1 || order > 26) throw new DomainException("Unsupported option display order: " + order);
        return String.valueOf((char) ('A' + order - 1));
    }

    private static List<String> wrap(String value, int max) {
        if (value.isBlank()) return List.of("");
        List<String> output = new ArrayList<>();
        String remaining = value;
        while (remaining.length() > max) {
            int index = remaining.lastIndexOf(' ', max);
            if (index < 1) index = max;
            output.add(remaining.substring(0, index));
            remaining = remaining.substring(index).trim();
        }
        output.add(remaining);
        return output;
    }

    public enum ExportType { STUDENT_EXAM, ANSWER_KEY }
    private enum Alignment { LEFT, CENTER }
    private record Line(String text, boolean bold, float size, Alignment alignment) {
        static Line blank() { return new Line("", false, 11, Alignment.LEFT); }
    }
    private record Font(PDFont regular, PDFont bold, boolean unicode) {
        String safe(String value) {
            return unicode ? value : Normalizer.normalize(value, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "").replaceAll("[^\\x20-\\x7E]", "?");
        }
    }
    public record ExportedPdf(byte[] bytes, String filename) { }
    public record ExportedArchive(byte[] bytes, String filename) { }
}
