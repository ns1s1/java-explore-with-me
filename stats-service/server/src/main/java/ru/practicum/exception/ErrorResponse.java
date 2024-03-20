package ru.practicum.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private String reason;
    private String status;
    private LocalDateTime timestamp;
}
