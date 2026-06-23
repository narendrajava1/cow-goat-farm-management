package com.cgfms.animal;

import com.cgfms.animal.domain.Animal;
import com.cgfms.animal.domain.AnimalStatus;
import com.cgfms.animal.dto.request.AnimalCreateRequest;
import com.cgfms.animal.exception.DuplicateTagException;
import com.cgfms.animal.repository.AnimalRepository;
import com.cgfms.animal.service.AnimalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnimalServiceTest {

    @Mock
    private AnimalRepository animalRepository;

    @InjectMocks
    private AnimalService animalService;

    @Test
    public void testRegisterAnimal_Success() {
        // Given
        AnimalCreateRequest request = new AnimalCreateRequest();
        request.setTagNumber("TAG123");
        request.setAnimalType(com.cgfms.animal.domain.AnimalType.COW);
        
        UUID farmId = UUID.randomUUID();
        
        when(animalRepository.existsByFarmIdAndTagNumber(farmId, "TAG123")).thenReturn(false);
        when(animalRepository.save(any(Animal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        assertDoesNotThrow(() -> animalService.registerAnimal(request, farmId));

        // Then
        verify(animalRepository).existsByFarmIdAndTagNumber(farmId, "TAG123");
        verify(animalRepository).save(any(Animal.class));
    }

    @Test
    public void testRegisterAnimal_DuplicateTag() {
        // Given
        AnimalCreateRequest request = new AnimalCreateRequest();
        request.setTagNumber("TAG123");
        request.setAnimalType(com.cgfms.animal.domain.AnimalType.COW);
        
        UUID farmId = UUID.randomUUID();
        
        when(animalRepository.existsByFarmIdAndTagNumber(farmId, "TAG123")).thenReturn(true);

        // When & Then
        assertThrows(DuplicateTagException.class, () -> animalService.registerAnimal(request, farmId));
        verify(animalRepository).existsByFarmIdAndTagNumber(farmId, "TAG123");
    }

    @Test
    public void testGetAnimalById_Found() {
        // Given
        UUID animalId = UUID.randomUUID();
        UUID farmId = UUID.randomUUID();
        
        Animal animal = new Animal();
        animal.setId(animalId);
        animal.setFarmId(farmId);
        animal.setTagNumber("TAG123");
        animal.setAnimalType(com.cgfms.animal.domain.AnimalType.COW);
        
        when(animalRepository.findById(animalId)).thenReturn(Optional.of(animal));

        // When
        assertDoesNotThrow(() -> animalService.getAnimalById(animalId, farmId));

        // Then
        verify(animalRepository).findById(animalId);
    }

    @Test
    public void testChangeAnimalStatus_Success() {
        // Given
        UUID animalId = UUID.randomUUID();
        UUID farmId = UUID.randomUUID();
        
        Animal animal = new Animal();
        animal.setId(animalId);
        animal.setFarmId(farmId);
        animal.setStatus(AnimalStatus.ACTIVE);
        
        when(animalRepository.findById(animalId)).thenReturn(Optional.of(animal));
        when(animalRepository.save(any(Animal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        assertDoesNotThrow(() -> animalService.changeAnimalStatus(animalId, AnimalStatus.SOLD, farmId));

        // Then
        verify(animalRepository).findById(animalId);
        verify(animalRepository).save(any(Animal.class));
    }
}