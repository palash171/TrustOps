package com.trustops.backend.ingestion;

import com.trustops.backend.comment.Comment;
import com.trustops.backend.comment.CommentStore;
import com.trustops.backend.comment.ModerationStatus;
import com.trustops.backend.organization.Organization;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Authenticate company
 * check whether event already exists
 * return existing comment OR save a new one
 * survive two identical requests arriving together
 */

@Service //Spring creates and shares one ingestion workflow object
public class CommentIngestionService {

    private final ApiKeyAuthenticator authenticator; //finds company from its API key
    private final CommentStore commentStore; //reads and writes comment rows

    //Spring injects both managed objects through this constructor
    public CommentIngestionService(
            ApiKeyAuthenticator authenticator,
            CommentStore commentStore
    ) {
        this.authenticator = authenticator;
        this.commentStore = commentStore;
    }

    public IngestionResult ingest(
            String rawApiKey,
            IngestCommentRequest request
    ) {
        //never trust an organisation ID from JSON, the API key decides ownership
        Organization organization =
                authenticator.authenticate(rawApiKey);

        String externalId = request.externalId().strip(); //remove accidental spaces around customer ID

        //check normal duplicate case before trying to INSERT anything
        Optional<Comment> existing =
                commentStore
                        .findByOrganizationIdAndSourceAndExternalId(
                                organization.getId(),
                                request.source(),
                                externalId
                        );

        //if company already sent this event return old row and say it was duplicate
        if (existing.isPresent()) {
            return new IngestionResult(
                    existing.get(),
                    true
            );
        }

        //no old event exists so build a new PENDING comment owned by authenticated company
        Comment comment = new Comment(
                UUID.randomUUID(),
                organization.getId(),
                request.source(),
                externalId,
                request.text().strip(),
                ModerationStatus.PENDING,
                Instant.now()
        );

        try {
            //saveAndFlush sends INSERT now so PostgreSQL checks unique constraint inside this try
            Comment saved =
                    commentStore.saveAndFlush(comment);

            return new IngestionResult(saved, false);
        } catch (DataIntegrityViolationException exception) {
            //two same requests may pass first check together but PostgreSQL only lets one INSERT win
            //find the winning row and return it instead of making duplicate data
            return commentStore
                    .findByOrganizationIdAndSourceAndExternalId(
                            organization.getId(),
                            request.source(),
                            externalId
                    )
                    .map(found ->
                            new IngestionResult(found, true)
                    )
                    .orElseThrow(() -> exception);
        }
    }
}
