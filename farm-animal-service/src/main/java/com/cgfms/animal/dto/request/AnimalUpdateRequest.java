package com.cgfms.animal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AnimalUpdateRequest {
    
    @NotBlank(message = "Tag number is required")
    private String tagNumber;
    
    @NotNull(message = "Animal type is required")
    private com.cgfms.animal.domain.AnimalType animalType;
    
    private com.cgfms.animal.domain.Gender gender;
    
    private LocalDate dateOfBirth;
    
    private String herdId;
}