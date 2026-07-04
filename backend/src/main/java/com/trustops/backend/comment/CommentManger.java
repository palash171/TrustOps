package com.trustops.backend.comment;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/// Changes the stored content

@Service // talks to spring to manage oe comment service object
public class CommentManger {

    private final CommentStore commentStore;

    public CommentManger(CommentStore cr) {
        commentStore = cr;
    }
    //create a new comment to store inside map
    public Comment create(String text){
        Comment c = new Comment(UUID.randomUUID(), text.strip(), ModerationStatus.PENDING, Instant.now());
        return commentStore.save(c); //add comment into table

    }
    public Comment updateStatus(UUID id, ModerationStatus status){
        Comment old = commentStore.findById(id).orElseThrow(() -> new CommentNotFoundException(id));
        Comment updated = new Comment(id,old.getText(),status,old.getReceivedAt());
        return commentStore.save(updated);
    }


    public List<Comment> findAll() {
        return commentStore.findAll();
    }
    public List<Comment> findByStatus(ModerationStatus status) {
        return commentStore.findAllByStatus(status);
    }
}
