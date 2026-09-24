package com.aiservice.application.service;

import com.aiservice.application.port.out.*;
import com.aiservice.domain.exception.*;
import com.aiservice.domain.model.*;

import java.io.*;
import java.time.*;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentService {
    public static final Set<String> TYPES = Set.of("text/plain","text/markdown","application/pdf","application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document","application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet","application/vnd.ms-powerpoint","application/vnd.openxmlformats-officedocument.presentationml.presentation");
    private final StoragePort storage;
    private final DocumentRepository repo;
    private final Clock clock;
    private final long max;

    public DocumentService(StoragePort s, DocumentRepository r, Clock c, org.springframework.core.env.Environment e) {
        storage = s;
        repo = r;
        clock = c;
        max = Long.parseLong(e.getProperty("ai.document.max-size-bytes", "10485760"));
    }

    @Transactional
    public DocumentMetadata upload(UUID owner, String name, String type, long size, InputStream data) {
        String normalized = type == null ? "" : type.toLowerCase(Locale.ROOT);
        String extension = name == null || !name.contains(".") ? "" : name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!TYPES.contains(normalized) || !Set.of("txt","md","pdf","doc","docx","xls","xlsx","ppt","pptx").contains(extension) || !matches(extension, normalized)) throw new UnsupportedDocumentException("Unsupported document format");
        if (size <= 0 || size > max) throw new UnsupportedDocumentException("Document size is invalid");
        var f = storage.store(name, data);
        return repo.save(new DocumentMetadata(UUID.randomUUID(), owner, name, normalized, size, f.key(), f.checksum(), Instant.now(clock)));
    }

    private boolean matches(String ext,String type){return switch(ext){case "txt"->"text/plain".equals(type);case "md"->"text/plain".equals(type)||"text/markdown".equals(type);case "pdf"->"application/pdf".equals(type);case "doc"->"application/msword".equals(type);case "docx"->"application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(type);case "xls"->"application/vnd.ms-excel".equals(type);case "xlsx"->"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(type);case "ppt"->"application/vnd.ms-powerpoint".equals(type);case "pptx"->"application/vnd.openxmlformats-officedocument.presentationml.presentation".equals(type);default->false;};}

    @Transactional(readOnly = true)
    public DocumentMetadata get(UUID id, UUID user) {
        var d = repo.findById(id).orElseThrow(() -> new NotFoundException("Document not found"));
        if (!d.ownerId().equals(user)) throw new NotFoundException("Document not found");
        return d;
    }
}
