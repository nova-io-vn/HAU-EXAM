package com.aiservice.application.port.out;

public interface AiRuntimeMetadataPort {
    RuntimeMetadata current();
    record RuntimeMetadata(String provider, String model) { }
}
