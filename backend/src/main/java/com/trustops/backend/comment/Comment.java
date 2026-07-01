package com.trustops.backend.comment;

import java.time.Instant;
import java.util.UUID;

public record Comment(
        UUID id,
        String text,
        Instant receivedAt
) {
}
