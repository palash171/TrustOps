package com.trustops.backend.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentStore extends JpaRepository<Comment, UUID> {
    List<Comment> findAllByOrderByReceivedAtDesc();
    List<Comment> findAllByStatusOrderByReceivedAtDesc(ModerationStatus status);
}