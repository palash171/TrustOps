package com.trustops.backend.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

        /// only the incoming create JSONe

public record CreateCommentRequest(
        // invariant checks
        @NotBlank(message = "Comment text is required")
        @Size(max = 5_000, message = "Comment text must be 5,000 characters or fewer")
        String text
) {
}
