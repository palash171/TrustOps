package com.trustops.backend.comment;


import jakarta.validation.constraints.NotNull;

public record UpdateCommentStatusRequest(
        @NotNull(message = "Moderation status is required")
        ModerationStatus status
) {}
