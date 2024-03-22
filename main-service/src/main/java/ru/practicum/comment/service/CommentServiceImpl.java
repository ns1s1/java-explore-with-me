package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;


    @Transactional
    @Override
    public CommentDto create(Long userId, Long eventId, NewCommentDto newCommentDto) {
        User author = getUserById(userId);
        Event event = getEventById(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event еще не опубликован");
        }

        Comment comment = commentMapper.convertToComment(newCommentDto);
        comment.setAuthor(author);
        comment.setEvent(event);

        return commentMapper.convertToCommentDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentDto> getComments(Long eventId) {
        Event event = getEventById(eventId);

        return commentRepository.findAllByEvent(event).stream()
                .map(commentMapper::convertToCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto getUserCommentById(Long userId, Long commentId) {
        getUserById(userId);
        Comment comment = getCommentById(commentId);

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ValidationException(
                    String.format("Пользователю с id = %d недоступен комментарий с id = %d", userId, commentId));
        }

        return commentMapper.convertToCommentDto(comment);
    }

    @Override
    public CommentDto getCommentByAdmin(Long commentId) {
        Comment comment = getCommentById(commentId);

        return commentMapper.convertToCommentDto(comment);
    }

    @Transactional
    @Override
    public void deleteComment(Long commentId, Long userId) {
        getUserById(userId);
        Comment comment = getCommentById(commentId);
        checkAuthorComment(userId, comment);

        commentRepository.delete(comment);
    }

    @Transactional
    @Override
    public void deleteCommentByAdmin(Long commentId) {
        getCommentById(commentId);

        commentRepository.deleteById(commentId);
    }

    @Override
    public List<CommentDto> getComments(Long eventId, int from, int size) {
        getEventById(eventId);
        Pageable page = PageRequest.of(from / size, size, Sort.by("id").descending());

        return commentRepository.findAllByEventId(eventId, page).stream()
                .map(commentMapper::convertToCommentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CommentDto updateComment(Long commentId, Long userId, NewCommentDto newCommentDto) {
        Comment comment = getCommentById(commentId);
        getUserById(userId);
        checkAuthorComment(userId, comment);

        comment.setText(newCommentDto.getText());

        return commentMapper.convertToCommentDto(commentRepository.save(comment));
    }

    @Transactional
    @Override
    public CommentDto updateCommentByAdmin(Long commentId, NewCommentDto newCommentDto) {
        Comment comment = getCommentById(commentId);
        comment.setText(newCommentDto.getText());

        return commentMapper.convertToCommentDto(commentRepository.save(comment));
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event c данным id не найден"));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User с данным id не найден"));
    }

    private Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment с данным id не найден"));
    }

    private void checkAuthorComment(Long userId, Comment comment) {
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ValidationException(String.format(
                    "Пользователю с id = %d недоступен комментарий с id = %d", userId, comment.getAuthor().getId()));
        }
    }
}
