package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.service.CommentService;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/comments")
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping("/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(@PathVariable Long userId, @PathVariable Long eventId,
                             @RequestBody @Valid NewCommentDto newCommentDto) {

        return commentService.create(userId, eventId, newCommentDto);
    }

    @GetMapping("/{commentId}")
    public CommentDto getUserCommentByid(@PathVariable Long userId, @PathVariable Long commentId) {

        return commentService.getUserCommentById(userId, commentId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@PathVariable("commentId") Long commentId,
                                    @PathVariable("userId") Long userId, @RequestBody @Valid NewCommentDto newCommentDto) {

        return commentService.updateComment(commentId, userId, newCommentDto);
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long commentId, @PathVariable Long userId) {

        commentService.deleteComment(commentId, userId);
    }
}
