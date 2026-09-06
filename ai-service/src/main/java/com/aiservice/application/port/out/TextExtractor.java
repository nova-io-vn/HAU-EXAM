package com.aiservice.application.port.out;
import java.io.InputStream;

public interface TextExtractor {
    String extract(String contentType, InputStream data);
}
