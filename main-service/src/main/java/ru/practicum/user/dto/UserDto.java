package ru.practicum.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    @NotNull
    private Long id;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;
}
