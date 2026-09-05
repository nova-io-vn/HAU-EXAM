package com.hau.ai.application.port.out;import com.hau.ai.domain.model.AiJob;import java.util.*;public interface AiJobRepository{AiJob save(AiJob j);Optional<AiJob> findById(UUID id);}
