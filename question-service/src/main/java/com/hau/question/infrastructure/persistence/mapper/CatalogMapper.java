package com.hau.question.infrastructure.persistence.mapper;
import com.hau.question.domain.model.*; import com.hau.question.infrastructure.persistence.entity.*; import org.mapstruct.Mapper;
@Mapper(componentModel="spring") public interface CatalogMapper {
 SubjectEntity toEntity(Subject value); Subject toDomain(SubjectEntity value);
 ChapterEntity toEntity(Chapter value); Chapter toDomain(ChapterEntity value);
 TopicEntity toEntity(Topic value); Topic toDomain(TopicEntity value);
}
