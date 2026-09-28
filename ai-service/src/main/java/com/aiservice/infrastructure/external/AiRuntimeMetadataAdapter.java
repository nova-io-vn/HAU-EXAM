package com.aiservice.infrastructure.external;

import com.aiservice.application.port.out.AiRuntimeMetadataPort;
import com.aiservice.infrastructure.persistence.repository.AiSettingsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AiRuntimeMetadataAdapter implements AiRuntimeMetadataPort {
    private final AiSettingsRepository settings; private final String fallbackModel;
    public AiRuntimeMetadataAdapter(AiSettingsRepository settings,
            @Value("${ai.provider.model:gemini-2.5-flash}") String fallbackModel) { this.settings=settings; this.fallbackModel=fallbackModel; }
    public RuntimeMetadata current(){return settings.findAll().stream().findFirst()
            .map(e->new RuntimeMetadata(e.provider,e.model)).orElse(new RuntimeMetadata("GEMINI",fallbackModel));}
}
