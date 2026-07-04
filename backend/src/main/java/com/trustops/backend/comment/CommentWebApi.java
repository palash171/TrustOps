package com.trustops.backend.comment;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public List<Comment> findAll(@RequestParam(name ="status", required = false) ModerationStatus status) {
        // return comment by give status or return all comments if status is not given
        // seek for status in the url given and attach that value to variable status

        if (status == null) return commentManger.findAll();
        return commentManger.findByStatus(status);
    }


}
