package com.cgfms.animal.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnimalStatusUpdateRequest {
    
    @NotNull(message = "Status is required")
    private com.cgfms.animal.domain.AnimalStatus status;
}