package com.notificationservice.presentation.controller;

import com.notificationservice.domain.model.ContactStatus;
import com.notificationservice.infrastructure.mail.SmtpEmailSender;
import com.notificationservice.infrastructure.persistence.entity.ContactRequestEntity;
import com.notificationservice.infrastructure.persistence.repository.JpaContactRequestRepository;
import com.notificationservice.presentation.request.ContactRequests.Create;
import com.notificationservice.presentation.request.ContactRequests.Reply;
import com.notificationservice.presentation.response.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;import java.util.UUID;

@RestController
public class ContactController {
    private final JpaContactRequestRepository repository; private final SmtpEmailSender email;
    public ContactController(JpaContactRequestRepository repository,SmtpEmailSender email){this.repository=repository;this.email=email;}
    @PostMapping("/api/v1/public/contact") public ApiResponse<ContactResponse> create(@Valid @RequestBody Create request){var now=Instant.now();var entity=new ContactRequestEntity();entity.setId(UUID.randomUUID());entity.setName(request.name().trim());entity.setEmail(request.email().trim());entity.setSubject(request.subject().trim());entity.setMessage(request.message().trim());entity.setFacultyId(request.facultyId());entity.setStatus(ContactStatus.NEW);entity.setCreatedAt(now);entity.setUpdatedAt(now);return ApiResponse.success(ContactResponse.from(repository.save(entity)));}
    @PreAuthorize("hasRole('SYSTEM_ADMIN')") @GetMapping("/api/v1/admin/contact") public ApiResponse<?> list(@RequestParam(required=false) ContactStatus status,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){var result=status==null?repository.findAll(PageRequest.of(page,size)):repository.findByStatus(status,PageRequest.of(page,size));return ApiResponse.success(result.map(ContactResponse::from));}
    @PreAuthorize("hasRole('SYSTEM_ADMIN')") @GetMapping("/api/v1/admin/contact/{id}") public ApiResponse<ContactResponse> get(@PathVariable UUID id){return ApiResponse.success(ContactResponse.from(repository.findById(id).orElseThrow()));}
    @PreAuthorize("hasRole('SYSTEM_ADMIN')") @PatchMapping("/api/v1/admin/contact/{id}/status") public ApiResponse<ContactResponse> status(@PathVariable UUID id,@RequestParam ContactStatus value){var c=repository.findById(id).orElseThrow();c.setStatus(value);c.setUpdatedAt(Instant.now());return ApiResponse.success(ContactResponse.from(repository.save(c)));}
    @PreAuthorize("hasRole('SYSTEM_ADMIN')") @PostMapping("/api/v1/admin/contact/{id}/reply") public ApiResponse<ContactResponse> reply(@PathVariable UUID id,@Valid @RequestBody Reply request){var c=repository.findById(id).orElseThrow();email.send(c.getEmail(),"[HAU QM] Phản hồi yêu cầu hỗ trợ",request.replyMessage());var now=Instant.now();c.setReplyMessage(request.replyMessage());c.setRepliedAt(now);c.setUpdatedAt(now);c.setStatus(ContactStatus.REPLIED);return ApiResponse.success(ContactResponse.from(repository.save(c)));}
}
