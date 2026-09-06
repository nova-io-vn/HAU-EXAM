package com.authservice.presentation.controller;

import com.authservice.application.dto.AuthDtos;
import com.authservice.application.service.AuthApplicationService;
import com.authservice.presentation.request.AuthRequests;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

class AuthControllerTest {
    @Test void controllerUsesAuthPrefixAndPostRoutes() {
        assertThat(AuthController.class.getAnnotation(RequestMapping.class).value()).containsExactly("/api/v1/auth");
        assertThat(postPath("register")).isEqualTo("/register");
        assertThat(postPath("login")).isEqualTo("/login");
        assertThat(postPath("refresh")).isEqualTo("/refresh");
        assertThat(postPath("logout")).isEqualTo("/logout");
        assertThat(postPath("forgot")).isEqualTo("/forgot-password");
        assertThat(postPath("verify")).isEqualTo("/verify-otp");
        assertThat(postPath("reset")).isEqualTo("/reset-password");
    }

    @Test void registerAndLoginDelegateToApplicationService() {
        AuthApplicationService service = mock(AuthApplicationService.class);
        AuthController controller = new AuthController(service);
        when(service.register(any(AuthDtos.RegistrationInput.class), isNull())).thenReturn(new AuthDtos.Registration(com.authservice.domain.model.AccountStatus.PENDING_APPROVAL));
        when(service.login(anyString(), anyString())).thenReturn(new AuthDtos.Session(UUID.randomUUID(), "GV001", "USER", null, "access", "refresh", null, null));
        controller.register(new AuthRequests.Register("GV001", "password123", "Giang vien 1",
                LocalDate.of(1990, 1, 1), null, "gv001@hau.edu.vn", null, null, "CNTT"), null);
        controller.login(new AuthRequests.Login("GV001", "password123"));
        verify(service).register(any(AuthDtos.RegistrationInput.class), isNull());
        verify(service).login("GV001", "password123");
    }

    private String postPath(String methodName) {
        for (Method method : AuthController.class.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) return method.getAnnotation(PostMapping.class).value()[0];
        }
        throw new AssertionError("Missing route: " + methodName);
    }
}
