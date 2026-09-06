package com.userservice.application.port.in;

import com.userservice.application.dto.ActorContext;
import com.userservice.domain.model.Role;
import com.userservice.domain.model.UserProfile;
import com.userservice.domain.repository.PageQuery;
import com.userservice.domain.repository.PageResult;
import java.util.UUID;
public interface UserAdministrationUseCase {
    PageResult<UserProfile> list(ActorContext actor, PageQuery query);
    UserProfile get(ActorContext actor, UUID id);
    UserProfile approve(ActorContext actor, UUID id, UUID correlationId);
    UserProfile reject(ActorContext actor, UUID id, UUID correlationId);
    UserProfile assignRole(ActorContext actor, UUID id, Role role, UUID correlationId);
    UserProfile assignFaculty(ActorContext actor, UUID id, String facultyId, UUID correlationId);
    UserProfile lock(ActorContext actor, UUID id, UUID correlationId);
    UserProfile unlock(ActorContext actor, UUID id, UUID correlationId);
}
