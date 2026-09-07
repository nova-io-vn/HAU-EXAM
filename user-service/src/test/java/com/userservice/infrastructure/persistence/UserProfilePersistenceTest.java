package com.userservice.infrastructure.persistence;

import com.userservice.domain.model.UserProfile;
import com.userservice.domain.repository.UserProfileRepository;
import com.userservice.domain.repository.PageQuery;
import com.userservice.domain.model.UserStatus;
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

    @Test void searchSupportsNullableKeywordAndCombinedFilters(){
        Instant now=Instant.now();
        repository.save(UserProfile.pending(UUID.randomUUID(),"CNTT01","Nguyễn Văn A",null,null,"cntt01@hau.edu.vn",null,null,"CNTT",now));
        repository.save(UserProfile.pending(UUID.randomUUID(),"KTX02","Trần Văn B",null,null,"ktx02@hau.edu.vn",null,null,"KIENTRUC",now));
        assertThat(repository.findAll(new PageQuery(0,20,null,null,null,null,null)).totalElements()).isEqualTo(2);
        assertThat(repository.findAll(new PageQuery(0,20,"cntt",null,null,null,null)).content()).hasSize(1);
        assertThat(repository.findAll(new PageQuery(0,20,"NGUYỄN","CNTT",null,UserStatus.PENDING_APPROVAL,null)).content()).hasSize(1);
        assertThat(repository.findAll(new PageQuery(0,1,"cntt",null,null,null,null)).totalPages()).isEqualTo(1);
    }
}
