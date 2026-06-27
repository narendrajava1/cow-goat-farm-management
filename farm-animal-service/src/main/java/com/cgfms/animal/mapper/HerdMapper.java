package com.cgfms.animal.mapper;

import com.cgfms.animal.domain.Herd;
import com.cgfms.animal.dto.request.HerdCreateRequest;
import com.cgfms.animal.dto.response.HerdResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HerdMapper {
    
    HerdResponse toResponse(Herd herd);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "farmId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Herd toEntity(HerdCreateRequest request);
    
    List<HerdResponse> toResponseList(List<Herd> herds);
}