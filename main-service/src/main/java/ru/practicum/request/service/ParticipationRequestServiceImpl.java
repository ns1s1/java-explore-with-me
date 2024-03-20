package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.EventStatus;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.ParticipationRequestStatus;
import ru.practicum.request.repository.ParticipationRequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final RequestMapper requestMapper;

    @Transactional
    @Override
    public ParticipationRequestDto create(Long userId, Long eventId) {
        Event event = getEventById(eventId);
        User requester = getUserById(userId);

        validateNewRequest(event, userId, eventId);

        ParticipationRequest participationRequest = new ParticipationRequest();
        participationRequest.setRequester(requester);
        participationRequest.setEvent(event);
        participationRequest.setStatus(ParticipationRequestStatus.PENDING);

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            participationRequest.setStatus(ParticipationRequestStatus.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }


        return requestMapper.convertToParticipationRequestDto(participationRequestRepository.save(participationRequest));
    }

    @Transactional
    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = participationRequestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("ParticipationRequest c данным id не существует"));
        if (request.getStatus() == ParticipationRequestStatus.CONFIRMED) {
            throw new ValidationException("ParticipationRequest уже подтверждена");
        }
        request.setStatus(ParticipationRequestStatus.CANCELED);

        Event event = getEventById(request.getEvent().getId());
        event.setConfirmedRequests(event.getConfirmedRequests() - 1);
        eventRepository.save(event);


        return requestMapper.convertToParticipationRequestDto(participationRequestRepository.save(request));
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateEventRequests(
            Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(
                () -> new NotFoundException(String.format("Event c id = %d и Userid = %d не найден", eventId, userId)));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("User с данным id не является инициатором");
        }

        List<ParticipationRequest> participationRequests = participationRequestRepository
                .findAllByIdInAndAndEventId(eventRequestStatusUpdateRequest.getRequestIds(), eventId);

        if (participationRequests.size() != eventRequestStatusUpdateRequest.getRequestIds().size()) {
            throw new NotFoundException(
                    "Количество запросов участия не совпадает с количеством идентификаторов запросов");
        }

        for (ParticipationRequest request : participationRequests) {
            if (request.getStatus() != ParticipationRequestStatus.PENDING) {
                throw new ValidationException("Только запросы со статусом «PENDING» могут быть приняты или отклонены");
            }
        }

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        if (eventRequestStatusUpdateRequest.getStatus() == EventStatus.REJECTED) {
            rejectedRequests = rejectParticipationRequests(participationRequests);
            return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
        }

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            return new EventRequestStatusUpdateResult(
                    participationRequests.stream()
                            .map(requestMapper::convertToParticipationRequestDto)
                            .collect(Collectors.toList()), new ArrayList<>());
        }

        if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ValidationException("Достигнуто максимальное количество участников");
        }
        processEventParticipationRequests(event, participationRequests, confirmedRequests, rejectedRequests);
        eventRepository.save(event);

        return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }

    @Override
    public List<ParticipationRequestDto> getRequestByUserId(Long userId) {
        getUserById(userId);

        return participationRequestRepository.findAllByRequesterId(userId).stream()
                .map(requestMapper::convertToParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ParticipationRequestDto> getAllParticipationRequestsFromEventByOwner(Long userId, Long eventId) {
        List<ParticipationRequest> requests = participationRequestRepository
                .findAllByEventIdAndEventInitiatorId(eventId, userId);

        return requests.stream()
                .map(requestMapper::convertToParticipationRequestDto)
                .collect(Collectors.toList());
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event c данным id не найден"));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User с данным id не найден"));
    }

    private void validateNewRequest(Event event, Long userId, Long eventId) {
        if (event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("User с данным id не инициатор события");
        }
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() - event.getConfirmedRequests() <= 0) {
            throw new ValidationException("Превышен лимит участников события");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ValidationException("Событие не опубликовано");
        }
        if (participationRequestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ValidationException("Попытка добаления дубликата");
        }
    }

    private List<ParticipationRequestDto> rejectParticipationRequests(List<ParticipationRequest> participationRequests) {

        return participationRequests.stream()
                .map(participationRequest -> {
                    participationRequest.setStatus(ParticipationRequestStatus.REJECTED);
                    participationRequestRepository.save(participationRequest);
                    return requestMapper.convertToParticipationRequestDto(participationRequest);
                })
                .collect(Collectors.toList());
    }

    private void processEventParticipationRequests(Event event, List<ParticipationRequest> participationRequests,
                                                   List<ParticipationRequestDto> confirmedRequests,
                                                   List<ParticipationRequestDto> rejectedRequests) {

        for (ParticipationRequest participationRequest : participationRequests) {
            if (event.getConfirmedRequests() < event.getParticipantLimit()) {
                participationRequest.setStatus(ParticipationRequestStatus.CONFIRMED);
                participationRequestRepository.save(participationRequest);
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                confirmedRequests.add(requestMapper.convertToParticipationRequestDto(participationRequest));
            } else {
                participationRequest.setStatus(ParticipationRequestStatus.REJECTED);
                participationRequestRepository.save(participationRequest);
                rejectedRequests.add(requestMapper.convertToParticipationRequestDto(participationRequest));
            }
        }
    }

}
