package com.aiservice.application.port.out;
import java.io.InputStream;
public interface DocumentTextExtractor { boolean supports(String filename,String contentType); String extract(String filename,String contentType,InputStream data); }
