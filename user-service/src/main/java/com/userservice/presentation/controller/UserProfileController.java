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

@RestController
@RequestMapping("/api/v1/users/me")
public class UserProfileController {
    private final UserProfileUseCase useCase;
    private final UserProfileResponseMapper mapper;
    private final ImageStorageService imageStorage;

    public UserProfileController(UserProfileUseCase useCase, UserProfileResponseMapper mapper, ImageStorageService imageStorage) {
        this.useCase = useCase; this.mapper = mapper; this.imageStorage = imageStorage;
    }

    @PutMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UserProfileResponse> avatar(@AuthenticationPrincipal Jwt jwt, @RequestPart("file") MultipartFile file) throws java.io.IOException {
        var command = new ImageUploadCommand(file.getBytes(), file.getOriginalFilename(), file.getContentType(), file.getSize());
        var image = imageStorage.upload(command, "hau-exam/avatars/" + userId(jwt));
        return ApiResponse.success(mapper.toResponse(useCase.updateOwnAvatar(userId(jwt), image)));
    }

    @GetMapping
    public ApiResponse<UserProfileResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(mapper.toResponse(useCase.getOwnProfile(userId(jwt))));
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
