package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.EndpointHitDto;
import ru.practicum.model.EndpointHit;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EndpointHitMapper {

    EndpointHitDto convertToEndpointHitDto(EndpointHit endpointHit);

    EndpointHit convertToEndpointHit(EndpointHitDto endpointHitDto);
}
