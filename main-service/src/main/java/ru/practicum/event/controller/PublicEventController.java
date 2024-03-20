package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.EventSort;
import ru.practicum.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
@Validated
public class PublicEventController {

    private final EventService eventService;

    @GetMapping("/{eventId}")
    public EventFullDto getById(@PathVariable Long eventId, HttpServletRequest httpServletRequest) {
        return eventService.get(eventId, httpServletRequest);
    }

    @GetMapping
    public List<EventShortDto> getAll(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(defaultValue = "EVENT_DATE") EventSort sort,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        return eventService.getAll(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, from, size, sort, request);
    }
}
