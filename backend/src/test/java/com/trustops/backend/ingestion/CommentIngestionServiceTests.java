package com.trustops.backend.ingestion;

import com.trustops.backend.comment.Comment;
import com.trustops.backend.comment.CommentStore;
import com.trustops.backend.comment.ContentSource;
import com.trustops.backend.comment.ModerationStatus;
import com.trustops.backend.organization.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommentIngestionServiceTests {

    private static final String API_KEY =
            "trustops-dev-key";

    private final ApiKeyAuthenticator authenticator =
            Mockito.mock(ApiKeyAuthenticator.class);

    private final CommentStore commentStore =
            Mockito.mock(CommentStore.class);

    private final CommentIngestionService ingestionService =
            new CommentIngestionService(
                    authenticator,
                    commentStore
            );

    private final Organization organization =
            new Organization(
                    UUID.randomUUID(),
                    "Test Company",
                    "stored-hash",
                    Instant.now()
            );

    @BeforeEach
    void setUp() {
        //every test treats this API key as belonging to our test company
        Mockito.when(
                authenticator.authenticate(API_KEY)
        ).thenReturn(organization);
    }

    @Test
    void storesNewEventOnce() {
        IngestCommentRequest request =
                new IngestCommentRequest(
                        ContentSource.COMPANY_FORUM,
                        "  comment-123  ",
                        "  Please review this  "
                );

        //database says this company event does not exist yet
        Mockito.when(
                commentStore
                        .findByOrganizationIdAndSourceAndExternalId(
                                organization.getId(),
                                ContentSource.COMPANY_FORUM,
                                "comment-123"
                        )
        ).thenReturn(Optional.empty());

        //fake save returns the same Comment it receives
        Mockito.when(
                commentStore.saveAndFlush(
                        Mockito.any(Comment.class)
                )
        ).thenAnswer(call -> call.getArgument(0));

        IngestionResult result =
                ingestionService.ingest(API_KEY, request);

        assertThat(result.duplicate()).isFalse();
        assertThat(result.comment().getOrganizationId())
                .isEqualTo(organization.getId());
        assertThat(result.comment().getSource())
                .isEqualTo(ContentSource.COMPANY_FORUM);
        assertThat(result.comment().getExternalId())
                .isEqualTo("comment-123");
        assertThat(result.comment().getText())
                .isEqualTo("Please review this");
        assertThat(result.comment().getStatus())
                .isEqualTo(ModerationStatus.PENDING);
        assertThat(result.comment().getReceivedAt())
                .isNotNull();

        //capture exact Comment passed into saveAndFlush
        ArgumentCaptor<Comment> commentCaptor =
                ArgumentCaptor.forClass(Comment.class);

        Mockito.verify(commentStore)
                .saveAndFlush(commentCaptor.capture());

        assertThat(commentCaptor.getValue())
                .isSameAs(result.comment());
    }

    @Test
    void returnsExistingEventWithoutSavingAgain() {
        Comment existing = existingComment();

        Mockito.when(
                commentStore
                        .findByOrganizationIdAndSourceAndExternalId(
                                organization.getId(),
                                ContentSource.COMPANY_FORUM,
                                "comment-123"
                        )
        ).thenReturn(Optional.of(existing));

        IngestionResult result =
                ingestionService.ingest(
                        API_KEY,
                        request()
                );

        assertThat(result.duplicate()).isTrue();
        assertThat(result.comment()).isSameAs(existing);

        //duplicate path must never perform another INSERT
        Mockito.verify(
                commentStore,
                Mockito.never()
        ).saveAndFlush(Mockito.any(Comment.class));
    }

    @Test
    void recoversWhenTwoSameEventsArriveTogether() {
        Comment winningComment = existingComment();

        /**
         * First lookup returns empty.
         * Lookup after PostgreSQL rejects INSERT returns winning row.
         */
        Mockito.when(
                commentStore
                        .findByOrganizationIdAndSourceAndExternalId(
                                organization.getId(),
                                ContentSource.COMPANY_FORUM,
                                "comment-123"
                        )
        ).thenReturn(
                Optional.empty(),
                Optional.of(winningComment)
        );

        //simulate PostgreSQL unique constraint rejecting second INSERT
        Mockito.when(
                commentStore.saveAndFlush(
                        Mockito.any(Comment.class)
                )
        ).thenThrow(
                new DataIntegrityViolationException(
                        "duplicate event"
                )
        );

        IngestionResult result =
                ingestionService.ingest(
                        API_KEY,
                        request()
                );

        assertThat(result.duplicate()).isTrue();
        assertThat(result.comment())
                .isSameAs(winningComment);

        Mockito.verify(commentStore)
                .saveAndFlush(Mockito.any(Comment.class));

        //one lookup before INSERT and another after constraint failure
        Mockito.verify(
                commentStore,
                Mockito.times(2)
        ).findByOrganizationIdAndSourceAndExternalId(
                organization.getId(),
                ContentSource.COMPANY_FORUM,
                "comment-123"
        );
    }

    //helper avoids rebuilding the same request in multiple tests
    private IngestCommentRequest request() {
        return new IngestCommentRequest(
                ContentSource.COMPANY_FORUM,
                "comment-123",
                "Please review this"
        );
    }

    //helper creates the row that won or already existed
    private Comment existingComment() {
        return new Comment(
                UUID.randomUUID(),
                organization.getId(),
                ContentSource.COMPANY_FORUM,
                "comment-123",
                "Please review this",
                ModerationStatus.PENDING,
                Instant.now()
        );
    }
}