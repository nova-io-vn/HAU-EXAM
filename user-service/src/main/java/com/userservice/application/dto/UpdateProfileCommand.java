package com.userservice.application.dto;

import java.time.LocalDate;
public record UpdateProfileCommand(String fullName, LocalDate dateOfBirth, String phone, String email, String address, String avatar) { }
