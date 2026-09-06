package com.userservice.infrastructure.persistence;

import com.userservice.domain.model.UserProfile;
import com.userservice.domain.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest @ActiveProfiles("test") @Transactional
class UserProfilePersistenceTest {
    @Autowired UserProfileRepository repository;
    @Test void persistsAndFindsByNormalizedLecturerCode(){UUID id=UUID.randomUUID();repository.save(UserProfile.pending(id,"gv01","User",null,null,"user@hau.edu.vn",null,null,null,Instant.now()));assertThat(repository.findByLecturerCode(" gv01 ")).isPresent().get().extracting(UserProfile::getId).isEqualTo(id);}
}
