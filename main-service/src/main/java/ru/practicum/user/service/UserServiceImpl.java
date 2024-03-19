package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.ValidationException;
import ru.practicum.user.dto.NewUserRequestDto;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.user.dto.UserDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.mapper.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;


    @Transactional
    @Override
    public UserDto create(NewUserRequestDto newUserRequestDto) {
        User user = userMapper.convertToUser(newUserRequestDto);

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException("User с такой почтой или именем уже существует");
        }

        return userMapper.convertToUserDto(user);
    }

    @Override
    public List<UserDto> findAll(List<Long> ids, int from, int size) {
        Pageable page = PageRequest.of(from / size, size, Sort.by("id").ascending());

        if (ids == null) {
            return userRepository.findAll(page).stream()
                    .map(userMapper::convertToUserDto)
                    .collect(Collectors.toList());
        } else {
            return userRepository.findAllByIdIn(ids, page).stream()
                    .map(userMapper::convertToUserDto)
                    .collect(Collectors.toList());
        }

    }

    @Transactional
    @Override
    public void delete(Long userId) {
        try {
            userRepository.deleteById(userId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("User with id = ");
        }
    }
}
