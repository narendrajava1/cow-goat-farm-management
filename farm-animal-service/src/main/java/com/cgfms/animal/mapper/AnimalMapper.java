package com.cgfms.animal.mapper;

import com.cgfms.animal.domain.Animal;
import com.cgfms.animal.dto.request.AnimalCreateRequest;
import com.cgfms.animal.dto.request.AnimalUpdateRequest;
import com.cgfms.animal.dto.response.AnimalResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {HerdMapper.class})
public interface AnimalMapper {
    
    @Mapping(target = "herdId", source = "herd.id")
    @Mapping(target = "ageInDays", expression = "java(computeAgeInDays(animal.getDateOfBirth()))")
    @Mapping(target = "lactationStatus", constant = "false")
    AnimalResponse toResponse(Animal animal);
    
    @Mapping(target = "herd", source = "herdId", qualifiedByName = "herdIdToHerd")
    Animal toEntity(AnimalCreateRequest request);
    
    @Mapping(target = "herd", source = "herdId", qualifiedByName = "herdIdToHerd")
    void updateEntity(AnimalUpdateRequest request, @MappingTarget Animal animal);
    
    List<AnimalResponse> toResponseList(List<Animal> animals);
    
    default Integer computeAgeInDays(LocalDate dateOfBirth) {
        if (dateOfBirth == null) return null;
        return java.time.temporal.ChronoUnit.DAYS.between(dateOfBirth, java.time.LocalDate.now());
    }
}