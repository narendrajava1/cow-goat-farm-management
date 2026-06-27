package com.cgfms.animal.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.cgfms.animal.exception.InvalidStatusTransitionException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * R2DBC entity — no JPA/Hibernate annotations.
 * Associations are resolved as foreign-key IDs; reactive repositories handle joins via DTO projections.
 */
@Table("animals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Animal {

    @Id
    private UUID id;

    @Column("farm_id")
    private UUID farmId;

    @Column("tag_number")
    private String tagNumber;

    @Column("animal_type")
    private AnimalType animalType;

    private Gender gender;

    private AnimalStatus status = AnimalStatus.ACTIVE;

    @Column("date_of_birth")
    private LocalDate dateOfBirth;

    // R2DBC stores FK as a scalar UUID; the owning Herd entity is resolved lazily by the service layer
    @Column("herd_id")
    private UUID herdId;

    private Instant createdAt;

    private Instant updatedAt;

    // Business method — encapsulate status transitions
    public void markAsSold(LocalDate effectiveDate) {
        if (this.status != AnimalStatus.ACTIVE) {
            throw new InvalidStatusTransitionException(
                "Only ACTIVE animals can be sold. Current: " + this.status);
        }
        this.status = AnimalStatus.SOLD;
    }
}
