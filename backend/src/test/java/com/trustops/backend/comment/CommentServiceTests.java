package com.trustops.backend.comment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommentServiceTests {

    private final CommentService commentService = new CommentService();

    @Test
    void createsAndStoresAComment() {
        Comment created = commentService.create(" a ");
        assertThat(created.id()).isNotNull();
        assertThat(created.text()).isEqualTo("a");
        assertThat(created.receivedAt()).isNotNull();
        assertThat(commentService.findAll()).containsExactly(created);
        Assertions.assertEquals(ModerationStatus.PENDING, created.status());
    }
    @Test
    void startsWithNoComments() {
        Assertions.assertEquals(List.of(), commentService.findAll());
    }
    @Test
    void updatesStatusAndMap() {
        Comment created = commentService.create(" a ");
       Comment updated = commentService.updateStatus(created.id(), ModerationStatus.APPROVED);
       Assertions.assertEquals(ModerationStatus.APPROVED, updated.status());
       assertThat(commentService.findAll()).contains(updated);
       assertThat(commentService.findAll()).doesNotContain(created);
    }
    @Test
    void shouldThrowExceptionWhenCommentNotFound() {
        Assertions.assertThrows(
                CommentNotFoundException.class,
                () -> commentService.updateStatus(
                        UUID.randomUUID(), //random id which is not in the map
                        ModerationStatus.APPROVED
                )
        );
    }

}
