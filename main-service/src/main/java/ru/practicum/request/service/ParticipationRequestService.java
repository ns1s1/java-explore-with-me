package ru.practicum.request.service;

import ru.practicum.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {

    ParticipationRequestDto create(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    EventRequestStatusUpdateResult updateEventRequests(
            Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);

    List<ParticipationRequestDto> getRequestByUserId(Long userId);

    List<ParticipationRequestDto> getAllParticipationRequestsFromEventByOwner(Long userId, Long eventId);
}
