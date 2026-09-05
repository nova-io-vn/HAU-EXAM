package com.hau.user.presentation.controller;

import com.hau.user.application.dto.ActorContext;
import com.hau.user.application.port.in.UserAdministrationUseCase;
import com.hau.user.domain.model.Role;
import com.hau.user.domain.repository.PageQuery;
import com.hau.user.presentation.mapper.UserProfileResponseMapper;
import com.hau.user.presentation.request.*;
import com.hau.user.presentation.response.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/users") @PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class UserAdministrationController {
    private final UserAdministrationUseCase useCase; private final UserProfileResponseMapper mapper;
    public UserAdministrationController(UserAdministrationUseCase useCase,UserProfileResponseMapper mapper){this.useCase=useCase;this.mapper=mapper;}
    @GetMapping public ApiResponse<PageResponse<UserProfileResponse>> list(@AuthenticationPrincipal Jwt jwt,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){var result=useCase.list(actor(jwt),new PageQuery(page,size));return ApiResponse.success(new PageResponse<>(result.content().stream().map(mapper::toResponse).toList(),result.page(),result.size(),result.totalElements(),result.totalPages()));}
    @GetMapping("/{id}") public ApiResponse<UserProfileResponse> get(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id){return response(useCase.get(actor(jwt),id));}
    @PostMapping("/{id}/approve") public ApiResponse<UserProfileResponse> approve(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@RequestHeader(value="X-Correlation-Id",required=false) String c){return response(useCase.approve(actor(jwt),id,correlation(c)));}
    @PostMapping("/{id}/reject") public ApiResponse<UserProfileResponse> reject(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@RequestHeader(value="X-Correlation-Id",required=false) String c){return response(useCase.reject(actor(jwt),id,correlation(c)));}
    @PutMapping("/{id}/role") public ApiResponse<UserProfileResponse> role(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody AssignRoleRequest r,@RequestHeader(value="X-Correlation-Id",required=false) String c){return response(useCase.assignRole(actor(jwt),id,r.role(),correlation(c)));}
    @PutMapping("/{id}/faculty") public ApiResponse<UserProfileResponse> faculty(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody AssignFacultyRequest r,@RequestHeader(value="X-Correlation-Id",required=false) String c){return response(useCase.assignFaculty(actor(jwt),id,r.facultyId(),correlation(c)));}
    @PostMapping("/{id}/lock") public ApiResponse<UserProfileResponse> lock(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@RequestHeader(value="X-Correlation-Id",required=false) String c){return response(useCase.lock(actor(jwt),id,correlation(c)));}
    @PostMapping("/{id}/unlock") public ApiResponse<UserProfileResponse> unlock(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@RequestHeader(value="X-Correlation-Id",required=false) String c){return response(useCase.unlock(actor(jwt),id,correlation(c)));}
    private ApiResponse<UserProfileResponse> response(com.hau.user.domain.model.UserProfile u){return ApiResponse.success(mapper.toResponse(u));}
    private ActorContext actor(Jwt jwt){return new ActorContext(UUID.fromString(jwt.getSubject()),Role.valueOf(jwt.getClaimAsString("role")),jwt.getClaimAsString("facultyId"));}
    private UUID correlation(String value){try{return value==null?UUID.randomUUID():UUID.fromString(value);}catch(IllegalArgumentException e){return UUID.randomUUID();}}
}
