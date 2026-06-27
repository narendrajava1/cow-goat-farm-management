package com.cgfms.animal.mapper;

import com.cgfms.animal.domain.Herd;
import com.cgfms.animal.dto.request.HerdCreateRequest;
import com.cgfms.animal.dto.response.HerdResponse;
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
public class HerdMapperImpl implements HerdMapper {

    @Override
    public HerdResponse toResponse(Herd herd) {
        if ( herd == null ) {
            return null;
        }

        HerdResponse herdResponse = new HerdResponse();

        herdResponse.setId( herd.getId() );
        herdResponse.setFarmId( herd.getFarmId() );
        herdResponse.setName( herd.getName() );
        herdResponse.setDescription( herd.getDescription() );
        herdResponse.setHerdType( herd.getHerdType() );
        herdResponse.setCreatedAt( herd.getCreatedAt() );
        herdResponse.setUpdatedAt( herd.getUpdatedAt() );

        return herdResponse;
    }

    @Override
    public Herd toEntity(HerdCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Herd.HerdBuilder herd = Herd.builder();

        herd.name( request.getName() );
        herd.description( request.getDescription() );
        herd.herdType( request.getHerdType() );

        return herd.build();
    }

    @Override
    public List<HerdResponse> toResponseList(List<Herd> herds) {
        if ( herds == null ) {
            return null;
        }

        List<HerdResponse> list = new ArrayList<HerdResponse>( herds.size() );
        for ( Herd herd : herds ) {
            list.add( toResponse( herd ) );
        }

        return list;
    }
}
