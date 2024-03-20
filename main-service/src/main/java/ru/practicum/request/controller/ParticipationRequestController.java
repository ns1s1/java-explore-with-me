package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.ParticipationRequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/requests")
public class ParticipationRequestController {
    private final ParticipationRequestService participationRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@RequestParam Long eventId, @PathVariable Long userId) {
        return participationRequestService.create(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        return participationRequestService.cancelRequest(userId, requestId);
    }

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public List<ParticipationRequestDto> getRequestByUserId(@PathVariable Long userId) {
        return participationRequestService.getRequestByUserId(userId);
    }


}
