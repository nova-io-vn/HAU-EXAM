package com.authservice.infrastructure.persistence.mapper;

import com.authservice.domain.model.AuthAccount;
import com.authservice.infrastructure.persistence.entity.AuthAccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AuthAccountPersistenceMapper {

    AuthAccountEntity toEntity(AuthAccount account);

    AuthAccount toDomain(AuthAccountEntity entity);
}
