package com.aiservice.application.port.out;

import java.util.UUID;

public interface UserDirectoryPort {
    String displayName(UUID userId);
}
