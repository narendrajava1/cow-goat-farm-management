package com.cgfms.animal.mapper;

import com.cgfms.animal.domain.Animal;
import com.cgfms.animal.dto.request.AnimalCreateRequest;
import com.cgfms.animal.dto.request.AnimalUpdateRequest;
import com.cgfms.animal.dto.response.AnimalResponse;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-25T17:11:18+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Homebrew)"
)
@Component
public class AnimalMapperImpl implements AnimalMapper {

    @Override
    public AnimalResponse toResponse(Animal animal) {
        if ( animal == null ) {
            return null;
        }

        AnimalResponse animalResponse = new AnimalResponse();

        animalResponse.setId( animal.getId() );
        animalResponse.setFarmId( animal.getFarmId() );
        animalResponse.setTagNumber( animal.getTagNumber() );
        animalResponse.setAnimalType( animal.getAnimalType() );
        animalResponse.setGender( animal.getGender() );
        animalResponse.setStatus( animal.getStatus() );
        animalResponse.setDateOfBirth( animal.getDateOfBirth() );
        if ( animal.getHerdId() != null ) {
            animalResponse.setHerdId( animal.getHerdId().toString() );
        }
        animalResponse.setCreatedAt( animal.getCreatedAt() );
        animalResponse.setUpdatedAt( animal.getUpdatedAt() );

        animalResponse.setAgeInDays( computeAgeInDays(animal.getDateOfBirth()) );
        animalResponse.setLactationStatus( false );

        return animalResponse;
    }

    @Override
    public Animal toEntity(AnimalCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Animal.AnimalBuilder animal = Animal.builder();

        animal.tagNumber( request.getTagNumber() );
        animal.animalType( request.getAnimalType() );
        animal.gender( request.getGender() );
        animal.dateOfBirth( request.getDateOfBirth() );
        animal.herdId( request.getHerdId() );

        return animal.build();
    }

    @Override
    public void updateEntity(AnimalUpdateRequest request, Animal animal) {
        if ( request == null ) {
            return;
        }

        animal.setTagNumber( request.getTagNumber() );
        animal.setAnimalType( request.getAnimalType() );
        animal.setGender( request.getGender() );
        animal.setDateOfBirth( request.getDateOfBirth() );
    }

    @Override
    public List<AnimalResponse> toResponseList(List<Animal> animals) {
        if ( animals == null ) {
            return null;
        }

        List<AnimalResponse> list = new ArrayList<AnimalResponse>( animals.size() );
        for ( Animal animal : animals ) {
            list.add( toResponse( animal ) );
        }

        return list;
    }
}
