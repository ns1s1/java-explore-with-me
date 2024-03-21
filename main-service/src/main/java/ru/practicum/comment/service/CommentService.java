package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;

import java.util.List;

public interface CommentService {

    CommentDto create(Long userId, Long eventId, NewCommentDto newCommentDto);

    CommentDto updateCommentByAdmin(Long commentId, NewCommentDto newCommentDto);

    CommentDto updateComment(Long commentId, Long userId, NewCommentDto newCommentDto);

    CommentDto getCommentByAdmin(Long commentId);

    List<CommentDto> getComments(Long eventId);

    CommentDto getUserCommentById(Long userId, Long commentId);

    void deleteComment(Long commentId, Long userId);

    void deleteCommentByAdmin(Long commentId);

    List<CommentDto> getComments(Long eventId, int from, int size);
}
