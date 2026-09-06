package com.aiservice.infrastructure.extraction;

import com.aiservice.application.port.out.TextExtractor;
import com.aiservice.domain.exception.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

@Component
public class PlainTextExtractor implements TextExtractor {
    public String extract(String type, InputStream data) {
        if (!"text/plain".equals(type)) throw new UnsupportedDocumentException("No extractor for " + type);
        try {
            return new String(data.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DomainException("Extraction failed", e);
        }
    }
}
