package com.hau.question.infrastructure.persistence.repository;
import com.hau.question.infrastructure.persistence.entity.SubjectEntity; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface SubjectJpaRepository extends JpaRepository<SubjectEntity,UUID>{List<SubjectEntity> findAllByFacultyIdOrderByCode(String facultyId);}
