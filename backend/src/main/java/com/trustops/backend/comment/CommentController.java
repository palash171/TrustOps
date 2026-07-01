package com.trustops.backend.comment;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

                ///Reads incoming requests

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
    @PatchMapping("/{id}/status")
    public Comment updateCommentStatus(@PathVariable UUID id, @Valid @RequestBody UpdateCommentStatusRequest update) {
        return commentService.updateStatus(id, update.status());
    }

    @GetMapping //get
    public List<Comment> findAll() {
        return commentService.findAll();
    }
}
