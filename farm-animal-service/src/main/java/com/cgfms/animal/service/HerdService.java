package com.cgfms.animal.service;

import com.cgfms.animal.domain.Animal;
import com.cgfms.animal.domain.Herd;
import com.cgfms.animal.dto.request.HerdCreateRequest;
import com.cgfms.animal.dto.response.HerdResponse;
import com.cgfms.animal.exception.DuplicateTagException;
import com.cgfms.animal.mapper.HerdMapper;
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
public class HerdService {
    
    private final HerdRepository herdRepository;
    private final AnimalRepository animalRepository;
    private final HerdMapper mapper;
    
    @Transactional
    public HerdResponse createHerd(HerdCreateRequest request, UUID farmId) {
        // Validate unique herd name for farm
        if (herdRepository.existsByFarmIdAndName(farmId, request.getName())) {
            throw new DuplicateTagException(request.getName());
        }
        
        Herd herd = mapper.toEntity(request);
        herd.setFarmId(farmId);
        Herd saved = herdRepository.save(herd);
        
        log.info("Herd created: name={}", saved.getName());
        return mapper.toResponse(saved);
    }
    
    @Transactional(readOnly = true)
    public HerdResponse getHerdById(UUID herdId, UUID farmId) {
        return herdRepository.findById(herdId)
            .filter(herd -> herd.getFarmId().equals(farmId))
            .map(mapper::toResponse)
            .orElseThrow(() -> new RuntimeException("Herd not found"));
    }
    
    @Transactional
    public HerdResponse updateHerd(UUID herdId, HerdCreateRequest request, UUID farmId) {
        Herd herd = herdRepository.findById(herdId)
            .filter(h -> h.getFarmId().equals(farmId))
            .orElseThrow(() -> new RuntimeException("Herd not found"));
        
        // Check for duplicate name if it's changing
        if (!herd.getName().equals(request.getName())) {
            if (herdRepository.existsByFarmIdAndName(farmId, request.getName())) {
                throw new DuplicateTagException(request.getName());
            }
        }
        
        herd.setName(request.getName());
        herd.setDescription(request.getDescription());
        herd.setHerdType(request.getHerdType());
        
        Herd updated = herdRepository.save(herd);
        return mapper.toResponse(updated);
    }
    
    @Transactional
    public void deleteHerd(UUID herdId, UUID farmId) {
        Herd herd = herdRepository.findById(herdId)
            .filter(h -> h.getFarmId().equals(farmId))
            .orElseThrow(() -> new RuntimeException("Herd not found"));
        
        // Check if herd has animals
        List<Animal> animalsInHerd = animalRepository.findByFarmIdAndHerdId(farmId, herdId);
        if (!animalsInHerd.isEmpty()) {
            throw new RuntimeException("Cannot delete herd with animals assigned to it");
        }
        
        herdRepository.deleteById(herdId);
    }
    
    @Transactional(readOnly = true)
    public List<Animal> getAnimalsByHerdId(UUID herdId, UUID farmId) {
        return animalRepository.findByFarmIdAndHerdId(farmId, herdId);
    }
}