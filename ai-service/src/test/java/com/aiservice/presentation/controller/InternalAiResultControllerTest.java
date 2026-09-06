package com.aiservice.presentation.controller;

import com.aiservice.application.service.AiWorkspaceService;
import com.aiservice.infrastructure.security.InternalServiceTokenVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InternalAiResultControllerTest {
    @Test
    void returnsResultForValidInternalToken() {
        AiWorkspaceService workspace = mock(AiWorkspaceService.class);
        UUID jobId = UUID.randomUUID();
        when(workspace.resultInternal(jobId)).thenReturn("[{\"content\":\"Question\"}]");
        var controller = new AiWorkspaceController(workspace, new ObjectMapper(),
                new InternalServiceTokenVerifier("test-token"));

        assertThat(controller.internalResult(jobId, "test-token").data()).isNotNull();
    }

    @Test
    void rejectsInvalidInternalTokenWithUnauthorized() {
        var controller = new AiWorkspaceController(mock(AiWorkspaceService.class), new ObjectMapper(),
                new InternalServiceTokenVerifier("test-token"));

        assertThatThrownBy(() -> controller.internalResult(UUID.randomUUID(), "wrong"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }
}
