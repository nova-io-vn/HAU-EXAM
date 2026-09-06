package com.questionservice.infrastructure.persistence.repository;
import com.questionservice.infrastructure.persistence.entity.SubjectEntity; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface SubjectJpaRepository extends JpaRepository<SubjectEntity,UUID>{List<SubjectEntity> findAllByFacultyIdOrderByCode(String facultyId);}
