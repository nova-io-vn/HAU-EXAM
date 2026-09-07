package com.userservice.infrastructure.persistence.adapter;

import com.userservice.domain.model.Faculty;
import com.userservice.domain.repository.*;
import com.userservice.infrastructure.persistence.entity.FacultyEntity;
import com.userservice.infrastructure.persistence.repository.JpaFacultyRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import org.springframework.stereotype.Repository;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Repository public class FacultyPersistenceAdapter implements FacultyRepository {
    private final JpaFacultyRepository repo;
    public FacultyPersistenceAdapter(JpaFacultyRepository repo){this.repo=repo;}
    public PageResult<Faculty> search(FacultyQuery q){
        String keyword=nullIfBlank(q.keyword());
        Specification<FacultyEntity> spec=(root,query,cb)->{
            var predicates=new java.util.ArrayList<Predicate>();
            if(keyword!=null){String pattern="%"+keyword.toLowerCase(Locale.ROOT)+"%";predicates.add(cb.or(cb.like(cb.lower(root.<String>get("code")),pattern),cb.like(cb.lower(root.<String>get("name")),pattern)));}
            if(q.active()!=null)predicates.add(cb.equal(root.get("active"),q.active()));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        var p=repo.findAll(spec,PageRequest.of(Math.max(0,q.page()),Math.min(100,Math.max(1,q.size()))));
        return new PageResult<>(p.getContent().stream().map(this::toDomain).toList(),p.getNumber(),p.getSize(),p.getTotalElements(),p.getTotalPages());
    }
    public Optional<Faculty> findById(UUID id){return repo.findById(id).map(this::toDomain);}
    public Optional<Faculty> findByCode(String code){return repo.findByCode(code.trim().toUpperCase(Locale.ROOT)).map(this::toDomain);}
    public Faculty save(Faculty f){FacultyEntity e=repo.findById(f.id()).orElseGet(FacultyEntity::new);e.setId(f.id());e.setCode(f.code());e.setName(f.name());e.setDescription(f.description());e.setActive(f.active());e.setCreatedAt(f.createdAt());e.setUpdatedAt(f.updatedAt());return toDomain(repo.save(e));}
    private Faculty toDomain(FacultyEntity e){return new Faculty(e.getId(),e.getCode(),e.getName(),e.getDescription(),e.isActive(),e.getCreatedAt(),e.getUpdatedAt());}
    private String nullIfBlank(String v){return v==null||v.isBlank()?null:v.trim();}
}
