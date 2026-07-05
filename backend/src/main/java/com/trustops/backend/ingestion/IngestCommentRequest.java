package com.trustops.backend.ingestion;

import com.trustops.backend.comment.ContentSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * the exact JSON an external company is allowed to send.
 */
public record IngestCommentRequest(
        @NotNull(message = "Content source is required") //reject JSON with no source
        ContentSource source, //Jackson turns JSON word into ContentSource enum

        @NotBlank(message = "External ID is required") //reject null, empty, or only spaces
        @Size(max = 255, message = "External ID must be 255 characters or fewer") //matches SQL column size
        String externalId, //ID company already uses in its own system

        @NotBlank(message = "Comment text is required")
        @Size(max = 5_000, message = "Comment text must be 5,000 characters or fewer")
        String text //actual content TrustOps needs to moderate
    )
{} //record automatically gives source(), externalId(), and text() methods
