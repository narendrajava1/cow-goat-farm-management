package com.cgfms.animal.repository;

import com.cgfms.animal.domain.Herd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HerdRepository extends JpaRepository<Herd, UUID> {
    
    boolean existsByFarmIdAndName(UUID farmId, String name);
}