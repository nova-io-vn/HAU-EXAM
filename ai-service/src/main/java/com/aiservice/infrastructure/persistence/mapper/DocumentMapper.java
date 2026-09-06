package com.aiservice.infrastructure.persistence.mapper;

import com.aiservice.domain.model.DocumentMetadata;
import com.aiservice.infrastructure.persistence.entity.DocumentEntity;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {
    public DocumentEntity toEntity(DocumentMetadata d) {
        var e = new DocumentEntity();
        e.id = d.id(); e.ownerId = d.ownerId(); e.originalName = d.originalName();
        e.contentType = d.contentType(); e.size = d.size(); e.storageKey = d.storageKey();
        e.checksum = d.checksum(); e.createdAt = d.createdAt();
        return e;
    }
    public DocumentMetadata toDomain(DocumentEntity e) {
        return new DocumentMetadata(e.id, e.ownerId, e.originalName, e.contentType,
                e.size, e.storageKey, e.checksum, e.createdAt);
    }
}
