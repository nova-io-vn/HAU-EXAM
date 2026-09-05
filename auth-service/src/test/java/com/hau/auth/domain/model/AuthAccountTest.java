package com.hau.auth.domain.model;

import com.hau.auth.domain.exception.InvalidAuthAccountException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthAccountTest {

    @Test
    void pendingAccountNormalizesLecturerCodeAndCannotAuthenticate() {
        AuthAccount account = AuthAccount.pending(" gv001 ", "bcrypt-hash", Instant.now());

        assertThat(account.getLecturerCode()).isEqualTo("GV001");
        assertThat(account.getStatus()).isEqualTo(AccountStatus.PENDING_APPROVAL);
        assertThat(account.canAuthenticate()).isFalse();
    }

    @Test
    void onlyActiveAccountCanAuthenticate() {
        Instant now = Instant.now();
        AuthAccount account = AuthAccount.pending("GV001", "bcrypt-hash", now)
                .changeStatus(AccountStatus.ACTIVE, now.plusSeconds(1));

        assertThat(account.canAuthenticate()).isTrue();
    }

    @Test
    void rejectsBlankLecturerCode() {
        assertThatThrownBy(() -> AuthAccount.pending(" ", "bcrypt-hash", Instant.now()))
                .isInstanceOf(InvalidAuthAccountException.class)
                .hasMessageContaining("lecturerCode");
    }
}
