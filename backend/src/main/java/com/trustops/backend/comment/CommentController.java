package com.trustops.backend.comment;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController //this class receives API requests.
@RequestMapping("/api/v1/comments") //Any request starting with /api/v1/comments  comes here.
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping //send
    @ResponseStatus(HttpStatus.CREATED)
    public Comment create(
            @Valid // configure invariant checks
            @RequestBody //extract json body convert into createCommentRequest object
            CreateCommentRequest request) {
        return commentService.create(request.text());
    }

    @GetMapping //get
    public List<Comment> findAll() {
        return commentService.findAll();
    }
}
