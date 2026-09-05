package com.hau.user.application.port.in;

import com.hau.user.application.dto.ActorContext;
import com.hau.user.domain.model.Role;
import com.hau.user.domain.model.UserProfile;
import com.hau.user.domain.repository.PageQuery;
import com.hau.user.domain.repository.PageResult;
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
