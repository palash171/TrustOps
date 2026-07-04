package com.trustops.backend.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentStore extends JpaRepository<Comment, UUID> {
/**
 * Method signature itself creates a sql script like
 * SELECT *
 * FROM comments
 * WHERE status = ?
 * ORDER BY received_at DESC;
 *
**/
    Page<Comment> findAllByOrderByReceivedAtDesc(Pageable pageable);
    Page<Comment> findAllByStatusOrderByReceivedAtDesc(ModerationStatus status, Pageable pageable);
}