package com.hau.auth.infrastructure.persistence.adapter;

import com.hau.auth.domain.model.AuthAccount;
import com.hau.auth.domain.repository.AuthAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AuthAccountPersistenceAdapterTest {

    @Autowired
    private AuthAccountRepository repository;

    @Test
    void savesAndFindsAccountUsingNormalizedLecturerCode() {
        AuthAccount account = AuthAccount.pending(
                "gv001", "$2a$12$foundationHash", Instant.now());

        repository.save(account);

        assertThat(repository.findByLecturerCode(" gv001 "))
                .isPresent()
                .get()
                .extracting(AuthAccount::getLecturerCode)
                .isEqualTo("GV001");
    }
}
