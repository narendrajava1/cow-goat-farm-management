package com.cgfms.animal.dto.response;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class AnimalResponse {
    private UUID id;
    private UUID farmId;
    private String tagNumber;
    private com.cgfms.animal.domain.AnimalType animalType;
    private com.cgfms.animal.domain.Gender gender;
    private com.cgfms.animal.domain.AnimalStatus status;
    private LocalDate dateOfBirth;
    private String herdId;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Computed fields
    private Integer ageInDays;
    private boolean lactationStatus;
}