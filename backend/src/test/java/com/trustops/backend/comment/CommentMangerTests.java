package com.trustops.backend.comment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;

class CommentMangerTests {

    private final CommentStore commentStore =
            Mockito.mock(CommentStore.class);

    private final CommentManger commentManger =
            new CommentManger(commentStore);

    @BeforeEach
    void setUp() {
        Mockito.when(
                commentStore.save(Mockito.any(Comment.class))
        ).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void createsAndStoresAComment() {
        Mockito.when(
                commentStore.save(Mockito.any(Comment.class))
        ).thenAnswer(call -> call.getArgument(0));
        Comment created = commentManger.create(" a ");
        assertThat(created.id()).isNotNull();
        assertThat(created.text()).isEqualTo("a");
        assertThat(created.receivedAt()).isNotNull();
        Mockito.verify(commentStore).save(created);
        Assertions.assertEquals(ModerationStatus.PENDING, created.status());
    }
    @Test
    void startsWithNoComments() {
        Assertions.assertEquals(List.of(), commentManger.findAll());
    }
    @Test
    void updatesStatusAndMap() {
        Comment created = commentManger.create(" a ");
        Mockito.when(commentStore.findById(created.id()))
                .thenReturn(Optional.of(created));

        Comment updated = commentManger.updateStatus(created.id(), ModerationStatus.APPROVED);
    }
    @Test
    void shouldThrowExceptionWhenCommentNotFound() {
        Assertions.assertThrows(
                CommentNotFoundException.class,
                () -> commentManger.updateStatus(
                        UUID.randomUUID(), // random ID which is not in the repository
                        ModerationStatus.APPROVED
                )
        );
    }

}
