package com.trustops.backend.comment;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

        /// Changes the stored content

@Service // talks to spring to manage oe comment service object
public class CommentService {

    private final ConcurrentMap<UUID, Comment> comments = new ConcurrentHashMap<>();

    //create a new comment to store inside map
    public Comment create(String text){
        Comment c = new Comment(UUID.randomUUID(), text.strip(), ModerationStatus.PENDING, Instant.now());
        comments.put(c.id(),c); //add comment into map
        return c;

    }
    public Comment updateStatus(UUID id, ModerationStatus status){
        Comment old = comments.get(id);
        if(old == null){ throw new CommentNotFoundException(id); }
        Comment updated = new Comment(id,old.text(),status,old.receivedAt());
        comments.put(id,updated);
        return updated;
    }


    public List<Comment> findAll() {
        return new ArrayList<>(comments.values());
    }
}
