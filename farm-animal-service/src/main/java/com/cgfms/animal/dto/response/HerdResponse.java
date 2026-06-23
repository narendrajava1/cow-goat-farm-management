package com.cgfms.animal.dto.response;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class HerdResponse {
    private UUID id;
    private UUID farmId;
    private String name;
    private String description;
    private com.cgfms.animal.domain.HerdType herdType;
    private Instant createdAt;
    private Instant updatedAt;
}