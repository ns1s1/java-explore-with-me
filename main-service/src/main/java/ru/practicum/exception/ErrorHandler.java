package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException ex) {
        log.error("Получен статус 404 NOT_FOUND {}", ex.getMessage());
        return new ApiError(
                ex.getMessage(),
                "The required object was not found",
                HttpStatus.NOT_FOUND.name(),
                LocalDateTime.now());
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handlerBadRequestExceptionException(final BadRequestException ex) {
        log.error("Получен статус 400 BAD_REQUESt {}", ex.getMessage(), ex);
        return new ApiError(
                ex.getMessage(),
                "Incorrectly made request.",
                HttpStatus.BAD_REQUEST.name(),
                LocalDateTime.now());
    }

    @ExceptionHandler({})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handlerValidationException(final ValidationException ex) {
        log.debug("Получен статус 409 CONFLICT {}", ex.getMessage());
        return new ApiError(
                ex.getMessage(),
                "Integrity constraint has been violated",
                HttpStatus.CONFLICT.name(),
                LocalDateTime.now());
    }

    @ExceptionHandler({})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handlerDataIntegrityViolationException(final DataIntegrityViolationException ex) {
        log.debug("Получен статус 409 CONFLICT {}", ex.getMessage());
        return new ApiError(
                ex.getMessage(),
                "Integrity constraint has been violated",
                HttpStatus.CONFLICT.name(),
                LocalDateTime.now());
    }
}

