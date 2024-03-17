package ru.practicum.event.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventSort;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.where;
import static ru.practicum.event.repository.EventRepository.*;


@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final StatsClient statsClient;
    private final ObjectMapper objectMapper;


    @Override
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        Event event = eventMapper.convertToEvent(newEventDto);
        User initiator = getUserById(userId);
        Category category = getCategoryById(newEventDto.getCategory());

        event.setInitiator(initiator);
        if (newEventDto.getPaid() == null) {
            event.setPaid(false);
        }
        if (newEventDto.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }
        event.setCreatedOn(LocalDateTime.now());
        event.setCategory(category);
        event.setState(EventState.PENDING);

        return eventMapper.convertToEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = getEventById(eventId);

        updateAdminEvent(event, updateEventAdminRequest);

        if (updateEventAdminRequest.getStateAction() != null) {
            switch (updateEventAdminRequest.getStateAction()) {
                case REJECT_EVENT:
                    if (event.getState().equals(EventState.PUBLISHED)) {
                        throw new ValidationException(
                                "Событие нельзя опубликовать, если оно не находится в состоянии ожидания");
                    }
                    event.setState(EventState.CANCELED);
                    break;
                case PUBLISH_EVENT:
                    if (!event.getState().equals(EventState.PENDING)) {
                        throw new ValidationException(
                                "Мероприятие может быть отклонено только, если оно еще не опубликовано");
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
            }
        }

        return eventMapper.convertToEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto updateUserEventById(Long eventId, Long userId, UpdateEventUserRequest updateEventUserRequest) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Event c id = " + eventId + " и Userid = " + userId + " не найден"));

        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ValidationException("Event не должен быть опубликован");
        }

        User user = getUserById(userId);
        event.setInitiator(user);

        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }
        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }
        if (updateEventUserRequest.getCategory() != null) {
            Category category = getCategoryById(updateEventUserRequest.getCategory());
            event.setCategory(category);
        }
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);

        if (updateEventUserRequest.getStateAction() != null) {
            switch (updateEventUserRequest.getStateAction()) {
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
            }
        }
        return eventMapper.convertToEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        Pageable page = PageRequest.of(from / size, size, Sort.by("id").descending());

        return eventRepository.findAllByInitiatorId(userId, page).stream()
                .map(eventMapper::convertToEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventShortDto> getAll(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                      LocalDateTime rangeEnd, Boolean onlyAvailable, Integer from, Integer size,
                                      EventSort sort, HttpServletRequest httpServletRequest) {

        if ((rangeStart != null && rangeEnd != null) && (rangeStart.isAfter(rangeEnd) || rangeStart.isEqual(rangeEnd))) {
            throw new BadRequestException("Дата окончания не может быть раньше даты начала");
        }

        Pageable page;
        if (sort.equals(EventSort.VIEWS)) {
            page = PageRequest.of(from / size, size, Sort.by("views"));
        } else {
            page = PageRequest.of(from / size, size, Sort.by("eventDate"));
        }

        Page<Event> eventsPage = eventRepository.findAll(where(hasText(text))
                .and(hasCategories(categories))
                .and(hasPaid(paid))
                .and(hasRangeStart(rangeStart))
                .and(hasRangeEnd(rangeEnd))
                .and(hasAvailable(onlyAvailable)), page);

        addStatsClient(httpServletRequest);
        Map<Long, Long> viewStatsMap = getViews(eventsPage.toList());
        for (Event event : eventsPage.toList()) {
            Long viewsFromStatistic = viewStatsMap.getOrDefault(event.getId(), 0L);
            event.setViews(viewsFromStatistic);
        }

        return eventsPage.stream()
                .filter(event -> event.getPublishedOn() != null)
                .map(eventMapper::convertToEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventFullDto> getAdminEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                             LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                             Integer from, Integer size) {
        if ((rangeStart != null && rangeEnd != null) && (rangeEnd.isBefore(rangeStart))) {
            throw new BadRequestException("Дата окончания не может быть раньше даты начала");
        }

        Pageable page = PageRequest.of(from / size, size, Sort.unsorted());

        if (users != null || states != null || categories != null || rangeStart != null || rangeEnd != null) {

            Page<Event> events = eventRepository.findAll(where(hasUsers(users))
                            .and(hasStates(states))
                            .and(hasCategories(categories))
                            .and(hasRangeStart(rangeStart))
                            .and(hasRangeEnd(rangeEnd)),
                    page);

            return events.stream()
                    .map(eventMapper::convertToEventFullDto)
                    .collect(Collectors.toList());
        } else {
            return eventRepository.findAll(page).stream()
                    .map(eventMapper::convertToEventFullDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public EventFullDto getUserEventById(Long eventId, Long userId) {
        Event event = getEventById(eventId);

        return eventMapper.convertToEventFullDto(event);
    }

    @Override
    public EventFullDto get(Long eventId, HttpServletRequest httpServletRequest) {
        Event event = eventRepository.findByIdAndStateIs(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event с данным id не найден"));

        addStatsClient(httpServletRequest);
        Map<Long, Long> viewsForEvent = getViews(List.of(event));
        Long views = viewsForEvent.getOrDefault(eventId, 0L);
        event.setViews(views);

        return eventMapper.convertToEventFullDto(event);
    }

    private void addStatsClient(HttpServletRequest request) {
        EndpointHitDto endpointHitDto = EndpointHitDto.builder()
                .app("main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();

        statsClient.create(endpointHitDto);
    }

    private Map<Long, Long> getViews(List<Event> events) {
        List<String> uris = events.stream()
                .map(event -> String.format("/events/%s", event.getId()))
                .collect(Collectors.toList());

        List<LocalDateTime> start = events.stream()
                .map(Event::getCreatedOn)
                .collect(Collectors.toList());
        LocalDateTime earliestDate = start.stream()
                .min(LocalDateTime::compareTo)
                .orElse(null);
        Map<Long, Long> viewStats = new HashMap<>();

        if (earliestDate != null) {
            ResponseEntity<Object> response = statsClient.getStatistic(earliestDate, LocalDateTime.now(),
                    uris, true);

            List<ViewStatsDto> viewStatsList = objectMapper.convertValue(response.getBody(), new TypeReference<>() {
            });

            viewStats = viewStatsList.stream()
                    .filter(statsDto -> statsDto.getUri().startsWith("/events/"))
                    .collect(Collectors.toMap(
                            statsDto -> Long.parseLong(statsDto.getUri().substring("/events/".length())),
                            ViewStatsDto::getHits
                    ));
        }
        return viewStats;
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event c данным id не найден"));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User с данным id не найден"));
    }

    private Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category c данным id не найдена"));
    }

    private void updateAdminEvent(Event event, UpdateEventAdminRequest updateEventAdminRequest) {
        if (updateEventAdminRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }

        if (updateEventAdminRequest.getTitle() != null) {
            event.setTitle(updateEventAdminRequest.getTitle());
        }

        if (updateEventAdminRequest.getDescription() != null) {
            event.setDescription(updateEventAdminRequest.getDescription());
        }

        if (updateEventAdminRequest.getCategory() != null) {
            Category category = getCategoryById(updateEventAdminRequest.getCategory());
            event.setCategory(category);
        }

        if (updateEventAdminRequest.getLocation() != null) {
            event.setLocation(updateEventAdminRequest.getLocation());
        }

        if (updateEventAdminRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }

        if (updateEventAdminRequest.getEventDate() != null) {
            event.setEventDate(updateEventAdminRequest.getEventDate());
        }

        if (updateEventAdminRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }

        if (updateEventAdminRequest.getPaid() != null) {
            event.setPaid(updateEventAdminRequest.getPaid());
        }
    }
}
