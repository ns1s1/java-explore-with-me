package ru.practicum.user.service;

import ru.practicum.user.dto.NewUserRequestDto;
import ru.practicum.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(NewUserRequestDto newUserRequestDto);

    void delete(Long userId);

    List<UserDto> findAll(List<Long> ids, Integer from, Integer size);
}
