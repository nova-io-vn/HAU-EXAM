package com.userservice.infrastructure.persistence;

import com.userservice.domain.model.Faculty;
import com.userservice.domain.repository.FacultyRepository;
import com.userservice.domain.repository.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FacultyPersistenceTest {
    @Autowired FacultyRepository repository;
    @Test void optionalKeywordAndActiveFiltersAreTypeSafe(){
        Instant now=Instant.now();
        repository.save(new Faculty(UUID.randomUUID(),"CNTT","Cong nghe thong tin","",true,now,now));
        repository.save(new Faculty(UUID.randomUUID(),"KIENTRUC","Kien truc","",false,now,now));
        assertThat(repository.search(new com.userservice.domain.repository.FacultyQuery(null,null,0,20)).totalElements()).isEqualTo(2);
        assertThat(repository.search(new com.userservice.domain.repository.FacultyQuery("",null,0,20)).totalElements()).isEqualTo(2);
        assertThat(repository.search(new com.userservice.domain.repository.FacultyQuery("   ",null,0,20)).totalElements()).isEqualTo(2);
        assertThat(repository.search(new com.userservice.domain.repository.FacultyQuery("cntt",null,0,20)).content()).hasSize(1);
        assertThat(repository.search(new com.userservice.domain.repository.FacultyQuery(null,true,0,20)).content()).hasSize(1);
        assertThat(repository.search(new com.userservice.domain.repository.FacultyQuery("cong",true,0,20)).content()).hasSize(1);
    }
    @Test void keywordSearchKeepsPagination(){
        Instant now=Instant.now();
        for(int i=0;i<3;i++)repository.save(new Faculty(UUID.randomUUID(),"TEST"+i,"Test faculty "+i,"",true,now,now));
        PageResult<Faculty> result=repository.search(new com.userservice.domain.repository.FacultyQuery("test",true,1,2));
        assertThat(result.page()).isEqualTo(1);assertThat(result.size()).isEqualTo(2);assertThat(result.totalElements()).isEqualTo(3);
    }
}
