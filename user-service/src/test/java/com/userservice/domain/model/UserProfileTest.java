package com.userservice.domain.model;

import com.userservice.domain.exception.InvalidStatusTransitionException;
import com.userservice.domain.exception.InvalidUserProfileException;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class UserProfileTest {
    private final Instant now=Instant.parse("2026-09-05T00:00:00Z");
    private UserProfile pending(){return UserProfile.pending(UUID.randomUUID()," gv001 ","Nguyen Van A",LocalDate.of(1990,9,6),null,"A@EXAMPLE.COM",null,null,"CNTT",now);}
    @Test void registrationDefaultsToUserPendingAndNormalizesIdentity(){var u=pending();assertThat(u.getLecturerCode()).isEqualTo("GV001");assertThat(u.getEmail()).isEqualTo("a@example.com");assertThat(u.getRole()).isEqualTo(Role.USER);assertThat(u.getStatus()).isEqualTo(UserStatus.PENDING_APPROVAL);}
    @Test void calculatesAgeWithoutPersistingIt(){Clock clock=Clock.fixed(Instant.parse("2026-09-05T00:00:00Z"),ZoneOffset.UTC);assertThat(pending().age(clock)).isEqualTo(35);}
    @Test void enforcesStatusTransitions(){var active=pending().approve(now.plusSeconds(1));assertThat(active.getStatus()).isEqualTo(UserStatus.ACTIVE);assertThat(active.lock(now.plusSeconds(2)).unlock(now.plusSeconds(3)).getStatus()).isEqualTo(UserStatus.ACTIVE);assertThatThrownBy(()->active.approve(now.plusSeconds(2))).isInstanceOf(InvalidStatusTransitionException.class);}
    @Test void subjectAdminMustHaveFaculty(){assertThatThrownBy(()->pending().assignFaculty(null,now.plusSeconds(1)).assignRole(Role.SUBJECT_ADMIN,now.plusSeconds(2))).isInstanceOf(InvalidUserProfileException.class);}
}
