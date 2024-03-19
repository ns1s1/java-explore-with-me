package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.event.model.EventSort;
import ru.practicum.event.model.EventState;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    EventFullDto create(Long userId, NewEventDto newEventDto);

    EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    EventFullDto updateUserEventById(Long eventId, Long userId, UpdateEventUserRequest eventDto);

    List<EventShortDto> getUserEvents(Long userId, int from, int size);

    List<EventShortDto> getAll(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                               LocalDateTime rangeEnd, Boolean onlyAvailable, int from, int size,
                               EventSort sort, HttpServletRequest request);

    List<EventFullDto> getAdminEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                      LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    EventFullDto getUserEventById(Long userId, Long eventId);

    EventFullDto get(Long eventId, HttpServletRequest request);
}
