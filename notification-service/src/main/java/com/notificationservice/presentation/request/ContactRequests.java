package com.notificationservice.presentation.request;
import jakarta.validation.constraints.*;
public final class ContactRequests {
    private ContactRequests(){}
    public record Create(@NotBlank @Size(max=160) String name,@NotBlank @Email @Size(max=254) String email,@NotBlank @Size(max=200) String subject,@NotBlank @Size(min=10,max=5000) String message,@Size(max=80) String facultyId){}
    public record Reply(@NotBlank @Size(min=1,max=5000) String replyMessage){}
}
