package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.event.model.Location;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewEventDto {

    @NotBlank
    @Size(max = 2000, min = 20)
    private String annotation;

    @NotNull
    private Long category;

    @NotBlank
    @Size(max = 7000, min = 20)
    private String description;

    @NotNull
    @FutureOrPresent
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull
    private Location location;

    private Boolean paid = false;

    @PositiveOrZero
    private Long participantLimit = 0L;

    private Boolean requestModeration = true;

    @NotBlank
    @Size(max = 120, min = 3)
    private String title;
}
