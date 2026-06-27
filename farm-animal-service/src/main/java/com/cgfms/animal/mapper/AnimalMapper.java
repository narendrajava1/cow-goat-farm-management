package com.cgfms.animal.mapper;

import com.cgfms.animal.domain.Animal;
import com.cgfms.animal.dto.request.AnimalCreateRequest;
import com.cgfms.animal.dto.request.AnimalUpdateRequest;
import com.cgfms.animal.dto.response.AnimalResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDate;
import java.util.List;

@Mapper(componentModel = "spring")
public interface AnimalMapper {

    @Mapping(target = "ageInDays", expression = "java(computeAgeInDays(animal.getDateOfBirth()))")
    @Mapping(target = "lactationStatus", constant = "false")
    AnimalResponse toResponse(Animal animal);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "farmId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Animal toEntity(AnimalCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "farmId", ignore = true)
    @Mapping(target = "herdId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(AnimalUpdateRequest request, @MappingTarget Animal animal);

    List<AnimalResponse> toResponseList(List<Animal> animals);

    default Integer computeAgeInDays(LocalDate dateOfBirth) {
        if (dateOfBirth == null) return null;
        return Math.toIntExact(java.time.temporal.ChronoUnit.DAYS.between(dateOfBirth, LocalDate.now()));
    }
}