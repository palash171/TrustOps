package com.trustops.backend.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

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

    /**
     * Finds the event that a particular customer/source already sent.
     *Spring turns this method name into a query using all three fields.
     * SELECT *
     * FROM comments
     * WHERE organization_id = ?
     * AND source = ?
     * AND external_id = ?;
     */
    Optional<Comment> findByOrganizationIdAndSourceAndExternalId(
            UUID organizationId, //which company owns the event
            ContentSource source, //which connected system sent it
            String externalId //ID that system assigned to it
    );
}
