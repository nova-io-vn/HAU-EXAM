package com.hau.user.presentation.controller;

import com.hau.user.application.dto.UpdateProfileCommand;
import com.hau.user.application.port.in.UserProfileUseCase;
import com.hau.user.presentation.mapper.UserProfileResponseMapper;
import com.hau.user.presentation.request.UpdateProfileRequest;
import com.hau.user.presentation.response.*;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/users/me")
public class UserProfileController {
    private final UserProfileUseCase useCase; private final UserProfileResponseMapper mapper;
    public UserProfileController(UserProfileUseCase useCase,UserProfileResponseMapper mapper){this.useCase=useCase;this.mapper=mapper;}
    @GetMapping public ApiResponse<UserProfileResponse> me(@AuthenticationPrincipal Jwt jwt){return ApiResponse.success(mapper.toResponse(useCase.getOwnProfile(userId(jwt))));}
    @PutMapping public ApiResponse<UserProfileResponse> update(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody UpdateProfileRequest r){
        var command=new UpdateProfileCommand(r.fullName(),r.dateOfBirth(),r.phone(),r.email(),r.address(),r.avatar());
        return ApiResponse.success(mapper.toResponse(useCase.updateOwnProfile(userId(jwt),command)));
    }
    private UUID userId(Jwt jwt){return UUID.fromString(jwt.getSubject());}
}
