package com.questionservice.presentation.request; import jakarta.validation.constraints.NotBlank; public record ReviewRequest(@NotBlank String reason){}
