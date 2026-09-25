package com.userservice.presentation.controller;

import com.userservice.application.dto.UpdateProfileCommand;
import com.userservice.application.port.in.UserProfileUseCase;
import com.userservice.presentation.mapper.UserProfileResponseMapper;
import com.userservice.presentation.request.UpdateProfileRequest;
import com.userservice.presentation.response.*;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import com.userservice.application.model.ImageUploadCommand;
import com.userservice.application.service.ImageStorageService;

import java.util.UUID;
import java.util.List;
import com.userservice.application.service.UserContactQueryService;

@RestController
@RequestMapping("/api/v1/users/me")
public class UserProfileController {
    private final UserProfileUseCase useCase;
    private final UserProfileResponseMapper mapper;
    private final ImageStorageService imageStorage;
    private final UserContactQueryService contacts;

    public UserProfileController(UserProfileUseCase useCase, UserProfileResponseMapper mapper, ImageStorageService imageStorage, UserContactQueryService contacts) {
        this.useCase = useCase; this.mapper = mapper; this.imageStorage = imageStorage; this.contacts = contacts;
    }

    @PutMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UserProfileResponse> avatar(@AuthenticationPrincipal Jwt jwt, @RequestPart("file") MultipartFile file) throws java.io.IOException {
        if (file == null || file.isEmpty()) throw new com.userservice.application.exception.ImageUploadException("Image file is empty");
        var command = new ImageUploadCommand(file.getBytes(), file.getOriginalFilename(), file.getContentType(), file.getSize());
        var image = imageStorage.upload(command, "hau-exam/avatars/" + userId(jwt));
        return ApiResponse.success(mapper.toResponse(useCase.updateOwnAvatar(userId(jwt), image)));
    }

    @GetMapping
    public ApiResponse<UserProfileResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(mapper.toResponse(useCase.getOwnProfile(userId(jwt))));
    }

    @GetMapping("/chat-contacts")
    public ApiResponse<List<UserContactQueryService.ChatContact>> chatContacts(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(contacts.contacts(jwt.getClaimAsString("role"), jwt.getClaimAsString("facultyId")));
    }

    @PutMapping
    public ApiResponse<UserProfileResponse> update(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateProfileRequest r) {
        var command = new UpdateProfileCommand(r.fullName(), r.dateOfBirth(), r.phone(), r.email(), r.address(), r.avatar(), r.academicRank(), r.academicDegree());
        return ApiResponse.success(mapper.toResponse(useCase.updateOwnProfile(userId(jwt), command)));
    }

    private UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
