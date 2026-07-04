package com.trustops.backend.comment;

import jakarta.validation.Valid;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

                ///Reads incoming requests

@RestController //this class receives API requests.
@RequestMapping("/api/v1/comments") //Any request starting with /api/v1/comments  comes here.
public class CommentWebApi {

    private final CommentManger commentManger;

    public CommentWebApi(CommentManger commentManger) {
        this.commentManger = commentManger;
    }

    @PostMapping //send
    @ResponseStatus(HttpStatus.CREATED)
    public Comment create(
            @Valid // configure invariant checks
            @RequestBody //extract json body convert into createCommentRequest object
            CreateCommentRequest request) {
        return commentManger.create(request.text());
    }
    @PatchMapping("/{id}/status")
    public Comment updateCommentStatus(@PathVariable UUID id, @Valid @RequestBody UpdateCommentStatusRequest update) {
        return commentManger.updateStatus(id, update.status());
    }

    @GetMapping //get
    public Page<Comment> findAll(@RequestParam(name ="status", required = false) ModerationStatus status, @PageableDefault(size = 20)Pageable pageable) {
        // return comment by give status or return all comments if status is not given
        // seek for status in the url given and attach that value to variable status

        if (status == null) return commentManger.findAll(pageable);
        return commentManger.findByStatus(status, pageable);
    }

    @GetMapping("/{id}")
    public Comment findById(@PathVariable UUID id) {
        return commentManger.findById(id);
    }

}
