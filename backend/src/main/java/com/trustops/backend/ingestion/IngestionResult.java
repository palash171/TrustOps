package com.trustops.backend.ingestion;

import com.trustops.backend.comment.Comment;

/**
 * Contains the comment reagrdless of it twas a repeat
 * record conversted to json via jackson
 */
public record IngestionResult(
        Comment comment, //newly saved comment or old comment we already had
        boolean duplicate //true means same company event was already stored
) {} //Jackson turns both record values into response JSON
