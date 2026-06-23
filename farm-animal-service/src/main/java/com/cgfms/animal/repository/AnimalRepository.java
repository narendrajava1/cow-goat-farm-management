package com.cgfms.animal.repository;

import com.cgfms.animal.domain.Animal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, UUID> {
    
    @Query("SELECT a FROM Animal a WHERE a.farmId = :farmId AND a.tagNumber = :tagNumber")
    Optional<Animal> findByFarmIdAndTagNumber(@Param("farmId") UUID farmId, 
                                              @Param("tagNumber") String tagNumber);
    
    boolean existsByFarmIdAndTagNumber(UUID farmId, String tagNumber);
    
    @Query("SELECT a FROM Animal a WHERE a.farmId = :farmId AND a.herd.id = :herdId")
    List<Animal> findByFarmIdAndHerdId(@Param("farmId") UUID farmId, 
                                       @Param("herdId") UUID herdId);
    
    @Query("SELECT a FROM Animal a WHERE a.farmId = :farmId " +
           "AND (:animalType IS NULL OR a.animalType = :animalType) " +
           "AND (:gender IS NULL OR a.gender = :gender) " +
           "AND (:status IS NULL OR a.status = :status)")
    List<Animal> findByFarmIdAndFilters(@Param("farmId") UUID farmId,
                                        @Param("animalType") String animalType,
                                        @Param("gender") String gender,
                                        @Param("status") String status);
}