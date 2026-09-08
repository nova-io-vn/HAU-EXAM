package com.userservice.presentation.request;

import jakarta.validation.constraints.Min;

public record CompleteOnboardingRequest(@Min(1) int version) { }
