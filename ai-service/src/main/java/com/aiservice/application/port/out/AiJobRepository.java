package com.aiservice.application.port.out;

import com.aiservice.domain.model.AiJob;
import java.util.*;

public interface AiJobRepository {
    AiJob save(AiJob j);
    Optional<AiJob> findById(UUID id);
    com.aiservice.application.model.WorkspacePage<AiJob> findByOwner(UUID owner, int page, int size);
}