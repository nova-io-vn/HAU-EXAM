package com.hau.auth.infrastructure.persistence.mapper;

import com.hau.auth.domain.model.AuthAccount;
import com.hau.auth.infrastructure.persistence.entity.AuthAccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AuthAccountPersistenceMapper {

    AuthAccountEntity toEntity(AuthAccount account);

    AuthAccount toDomain(AuthAccountEntity entity);
}
