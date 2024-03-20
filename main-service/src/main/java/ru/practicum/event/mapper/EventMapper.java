package ru.practicum.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "views", ignore = true)
    Event convertToEvent(NewEventDto newEventDto);

    EventFullDto convertToEventFullDto(Event event);

    EventShortDto convertToEventShortDto(Event event);
}
