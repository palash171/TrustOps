package com.trustops.backend.comment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommentServiceTests {

    private final CommentService commentService = new CommentService();

    @Test
    void createsAndStoresAComment() {
        Comment created = commentService.create("  This is a test comment.  ");

        assertThat(created.id()).isNotNull();
        assertThat(created.text()).isEqualTo("This is a test comment.");
        assertThat(created.receivedAt()).isNotNull();
        assertThat(commentService.findAll()).containsExactly(created);
        Assertions.assertEquals(ModerationStatus.PENDING, created.status());
    }
    @Test
    void startsWithNoComments() {
        Assertions.assertEquals(List.of(), commentService.findAll());
    }

}
