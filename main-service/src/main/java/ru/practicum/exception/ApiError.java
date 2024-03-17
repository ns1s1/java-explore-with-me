package ru.practicum.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ApiError {
    private final String error;
    private String reason;
    private String status;
    private LocalDateTime timestamp;
}
