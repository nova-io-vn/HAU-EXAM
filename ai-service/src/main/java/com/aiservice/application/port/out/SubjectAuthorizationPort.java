package com.aiservice.application.port.out;
import java.util.UUID;
public interface SubjectAuthorizationPort { boolean assigned(UUID subjectId, UUID userId); }
