package com.authservice.presentation.controller;

import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class JwksController {
    private final RSAKey key;

    public JwksController(RSAKey key) {
        this.key = key;
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        return Map.of("keys", List.of(key.toPublicJWK().toJSONObject()));
    }
}
