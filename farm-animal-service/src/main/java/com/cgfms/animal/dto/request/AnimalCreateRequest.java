package com.cgfms.animal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class AnimalCreateRequest {
    
    @NotBlank(message = "Tag number is required")
    private String tagNumber;
    
    @NotNull(message = "Animal type is required")
    private com.cgfms.animal.domain.AnimalType animalType;
    
    private com.cgfms.animal.domain.Gender gender;
    
    private LocalDate dateOfBirth;

    private UUID herdId;
}