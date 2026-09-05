package com.hau.user.infrastructure.persistence;

import com.hau.user.domain.model.UserProfile;
import com.hau.user.domain.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest @Transactional
class UserProfilePersistenceTest {
    @Autowired UserProfileRepository repository;
    @Test void persistsAndFindsByNormalizedLecturerCode(){UUID id=UUID.randomUUID();repository.save(UserProfile.pending(id,"gv01","User",null,null,"user@hau.edu.vn",null,null,null,Instant.now()));assertThat(repository.findByLecturerCode(" gv01 ")).isPresent().get().extracting(UserProfile::getId).isEqualTo(id);}
}
