package com.userservice.application.service;

import com.userservice.application.dto.ActorContext;
import com.userservice.application.port.out.LecturerWorkbookReader;
import com.userservice.application.port.out.UserEventPublisher;
import com.userservice.domain.model.*;
import com.userservice.domain.repository.FacultyRepository;
import com.userservice.domain.repository.UserProfileRepository;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BulkLecturerImportService {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final LecturerWorkbookReader workbook;
    private final UserProfileRepository users;
    private final FacultyRepository faculties;
    private final UserEventPublisher events;
    private final Clock clock;

    public BulkLecturerImportService(LecturerWorkbookReader workbook, UserProfileRepository users, FacultyRepository faculties, UserEventPublisher events, Clock clock) {
        this.workbook = workbook; this.users = users; this.faculties = faculties; this.events = events; this.clock = clock;
    }

    public Preview preview(ActorContext actor, String name, String contentType, long size, InputStream input) {
        requireAdmin(actor); return validate(workbook.read(name, contentType, size, input));
    }

    @Transactional
    public ImportResult confirm(ActorContext actor, String name, String contentType, long size, InputStream input, UUID correlationId) {
        requireAdmin(actor);
        Preview preview = validate(workbook.read(name, contentType, size, input));
        if (preview.errorCount() > 0 || preview.duplicateCount() > 0) throw new IllegalArgumentException("Workbook contains invalid or duplicate rows; import was not started");
        Instant now = Instant.now(clock); var created = new ArrayList<UserProfile>();
        for (RowResult row : preview.rows()) {
            UUID id = UUID.randomUUID();
            UserProfile user = UserProfile.imported(id, row.lecturerCode(), row.fullName(), blank(row.phone()), row.email(),
                    rank(row.academicRank()), degree(row.academicDegree()), row.facultyCode(), now);
            created.add(users.save(user));
        }
        created.forEach(user -> events.accountImportRequested(user, correlationId));
        return new ImportResult(created.size(), "PENDING_APPROVAL", "Imported lecturers use the existing forgot-password flow after approval; no plaintext password is created or transmitted.");
    }

    public byte[] template(ActorContext actor) { requireAdmin(actor); return workbook.template(); }

    private Preview validate(List<LecturerWorkbookReader.Row> source) {
        Set<String> codes = new HashSet<>(), emails = new HashSet<>(); var rows = new ArrayList<RowResult>();
        int valid = 0, errors = 0, duplicates = 0;
        for (var row : source) {
            String code = row.lecturerCode() == null ? "" : row.lecturerCode().trim().toUpperCase(Locale.ROOT);
            String email = row.email() == null ? "" : row.email().trim().toLowerCase(Locale.ROOT);
            var messages = new ArrayList<String>(); boolean duplicate = false;
            if (code.isBlank()) messages.add("lecturerCode is required");
            if (row.fullName() == null || row.fullName().isBlank()) messages.add("fullName is required");
            if (!EMAIL.matcher(email).matches()) messages.add("email is invalid");
            if (row.facultyCode() == null || faculties.findByCode(row.facultyCode().trim().toUpperCase(Locale.ROOT)).filter(Faculty::active).isEmpty()) messages.add("faculty does not exist or is inactive");
            try { rank(row.academicRank()); } catch (Exception e) { messages.add("academicRank is invalid"); }
            try { degree(row.academicDegree()); } catch (Exception e) { messages.add("academicDegree is invalid"); }
            if (!code.isBlank() && (!codes.add(code) || users.existsByLecturerCode(code))) { messages.add("lecturerCode is duplicated"); duplicate = true; }
            if (!email.isBlank() && (!emails.add(email) || users.existsByEmail(email))) { messages.add("email is duplicated"); duplicate = true; }
            String status = duplicate ? "DUPLICATE" : messages.isEmpty() ? "VALID" : "ERROR";
            if ("VALID".equals(status)) valid++; else if (duplicate) duplicates++; else errors++;
            rows.add(new RowResult(row.rowNumber(), code, trim(row.fullName()), blank(row.phone()), upper(row.academicRank(), "NONE"), upper(row.academicDegree(), "NONE"), email, upper(row.facultyCode(), ""), status, List.copyOf(messages)));
        }
        return new Preview(rows.size(), valid, errors, duplicates, List.copyOf(rows));
    }

    private AcademicRank rank(String value) { return AcademicRank.valueOf(upper(value, "NONE")); }
    private AcademicDegree degree(String value) { return AcademicDegree.valueOf(upper(value, "NONE")); }
    private String upper(String value, String fallback) { return value == null || value.isBlank() ? fallback : value.trim().toUpperCase(Locale.ROOT); }
    private String trim(String value) { return value == null ? "" : value.trim(); }
    private String blank(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private void requireAdmin(ActorContext actor) { if (actor == null || actor.role() != Role.SYSTEM_ADMIN) throw new com.userservice.domain.exception.ForbiddenOperationException("SYSTEM_ADMIN role is required"); }

    public record RowResult(int rowNumber, String lecturerCode, String fullName, String phone, String academicRank, String academicDegree, String email, String facultyCode, String status, List<String> errors) { }
    public record Preview(int total, int validCount, int errorCount, int duplicateCount, List<RowResult> rows) { }
    public record ImportResult(int importedCount, String status, String activationInstruction) { }
}
