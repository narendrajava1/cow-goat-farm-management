package com.cgfms.animal.service;

import com.cgfms.animal.domain.Animal;
import com.cgfms.animal.domain.Herd;
import com.cgfms.animal.dto.request.AnimalCreateRequest;
import com.cgfms.animal.dto.request.AnimalUpdateRequest;
import com.cgfms.animal.dto.response.AnimalResponse;
import com.cgfms.animal.exception.DuplicateTagException;
import com.cgfms.animal.mapper.AnimalMapper;
import com.cgfms.animal.repository.AnimalRepository;
import com.cgfms.animal.repository.HerdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnimalService {
    
    private final AnimalRepository animalRepository;
    private final HerdRepository herdRepository;
    private final AnimalMapper mapper;
    
    @Transactional
    public AnimalResponse registerAnimal(AnimalCreateRequest request, UUID farmId) {
        // Validate tag uniqueness
        if (animalRepository.existsByFarmIdAndTagNumber(farmId, request.getTagNumber())) {
            throw new DuplicateTagException(request.getTagNumber());
        }
        
        Animal animal = mapper.toEntity(request);
        animal.setFarmId(farmId);
        
        // Set herd if provided
        if (request.getHerdId() != null) {
            Herd herd = herdRepository.findById(UUID.fromString(request.getHerdId()))
                .orElseThrow(() -> new RuntimeException("Herd not found: " + request.getHerdId()));
            animal.setHerd(herd);
        }
        
        Animal saved = animalRepository.save(animal);
        
        log.info("Animal registered: tag={}, type={}", saved.getTagNumber(), saved.getAnimalType());
        return mapper.toResponse(saved);
    }
    
    @Transactional(readOnly = true)
    public AnimalResponse getAnimalById(UUID animalId, UUID farmId) {
        return animalRepository.findById(animalId)
            .filter(animal -> animal.getFarmId().equals(farmId))
            .map(mapper::toResponse)
            .orElseThrow(() -> new RuntimeException("Animal not found"));
    }
    
    @Transactional(readOnly = true)
    public List<AnimalResponse> listAnimals(UUID farmId, String animalType, String gender, String status) {
        List<Animal> animals = animalRepository.findByFarmIdAndFilters(farmId, animalType, gender, status);
        return mapper.toResponseList(animals);
    }
    
    @Transactional
    public AnimalResponse updateAnimal(UUID animalId, AnimalUpdateRequest request, UUID farmId) {
        Animal animal = animalRepository.findById(animalId)
            .filter(a -> a.getFarmId().equals(farmId))
            .orElseThrow(() -> new RuntimeException("Animal not found"));
        
        // Check for duplicate tag number (excluding current animal)
        if (!animal.getTagNumber().equals(request.getTagNumber())) {
            if (animalRepository.existsByFarmIdAndTagNumber(farmId, request.getTagNumber())) {
                throw new DuplicateTagException(request.getTagNumber());
            }
        }
        
        mapper.updateEntity(request, animal);
        
        // Set herd if provided
        if (request.getHerdId() != null) {
            Herd herd = herdRepository.findById(UUID.fromString(request.getHerdId()))
                .orElseThrow(() -> new RuntimeException("Herd not found: " + request.getHerdId()));
            animal.setHerd(herd);
        } else {
            animal.setHerd(null);
        }
        
        Animal updated = animalRepository.save(animal);
        return mapper.toResponse(updated);
    }
    
    @Transactional
    public AnimalResponse changeAnimalStatus(UUID animalId, com.cgfms.animal.domain.AnimalStatus status, UUID farmId) {
        Animal animal = animalRepository.findById(animalId)
            .filter(a -> a.getFarmId().equals(farmId))
            .orElseThrow(() -> new RuntimeException("Animal not found"));
        
        // Add business rules for status transitions if needed
        animal.setStatus(status);
        Animal updated = animalRepository.save(animal);
        return mapper.toResponse(updated);
    }
}