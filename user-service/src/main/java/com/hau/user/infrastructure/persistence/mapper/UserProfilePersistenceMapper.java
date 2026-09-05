package com.hau.user.infrastructure.persistence.mapper;

import com.hau.user.domain.model.UserProfile;
import com.hau.user.infrastructure.persistence.entity.UserProfileEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
@Mapper(componentModel="spring",unmappedTargetPolicy=ReportingPolicy.ERROR)
public interface UserProfilePersistenceMapper {
    UserProfileEntity toEntity(UserProfile profile);
    @Mapping(target="approve",ignore=true)
    @Mapping(target="reject",ignore=true)
    @Mapping(target="lock",ignore=true)
    @Mapping(target="unlock",ignore=true)
    UserProfile toDomain(UserProfileEntity entity);
}
