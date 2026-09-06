package com.authservice.infrastructure.persistence.adapter;

import com.authservice.domain.model.AuthAccount;
import com.authservice.domain.repository.AuthAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthAccountPersistenceAdapterTest {

    @Autowired
    private AuthAccountRepository repository;

    @Test
    void savesAndFindsAccountUsingNormalizedLecturerCode() {
        AuthAccount account = AuthAccount.pending(
                "gv001", "$2a$12$foundationHash", "gv001@hau.edu.vn", "CNTT", Instant.now());

        repository.save(account);

        assertThat(repository.findByLecturerCode(" gv001 "))
                .isPresent()
                .get()
                .extracting(AuthAccount::getLecturerCode)
                .isEqualTo("GV001");
        assertThat(repository.findByLecturerCode("GV001").orElseThrow().getRole()).isEqualTo("USER");
    }
}
