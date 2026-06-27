package com.cgfms.animal.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * R2DBC entity — no JPA/Hibernate annotations.
 */
@Table("herds")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Herd {

    @Id
    private UUID id;

    @Column("farm_id")
    private UUID farmId;

    private String name;

    private String description;

    @Column("herd_type")
    private HerdType herdType;

    private Instant createdAt;

    private Instant updatedAt;
}