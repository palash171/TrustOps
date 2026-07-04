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

    private final CommentStore commentStore = Mockito.mock(CommentStore.class);

    private final CommentManger commentManger = new CommentManger(commentStore);

    @BeforeEach
    void setUp() {

        Mockito.when(commentStore.save // just records that save was called not stored in a database
                        (Mockito.any(Comment.class))) //match any comment object

                /**
                 *  When the fake save() method is called, Mockito creates an object containing information about that call. WE name it 'call'
                 *  call looks like [ Method called: save, arguments: [created]]
                 *  getArgument - Get the first argument that was passed to the mocked method.
                 *  then asnwer returns the first argument passed to save().
                 * */
                .thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void createsAndStoresAComment() {
        Comment created = commentManger.create(" a "); // effectivity calls Comment stores saves method Comment save(Comment comment);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getText()).isEqualTo("a");
        assertThat(created.getStatus())
                .isEqualTo(ModerationStatus.PENDING);
        assertThat(created.getReceivedAt()).isNotNull();

        Mockito.verify(commentStore).save(created); //ensures .save was called upon commentStore
    }

    @Test
    void startsWithNoComments() {
        Mockito.when(commentStore.findAll()).thenReturn(List.of());
        assertThat(commentManger.findAll()).isEmpty();

    }

    @Test
    void updatesGetStatusAndStoresReplacement() {
        Comment created = commentManger.create(" a ");

        Mockito.when(commentStore.findById(created.getId()))
                .thenReturn(Optional.of(created));

        Comment updated = commentManger.updateStatus(created.getId(), ModerationStatus.APPROVED);

        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getText()).isEqualTo(created.getText());
        assertThat(updated.getReceivedAt()).isEqualTo(created.getReceivedAt());
        assertThat(updated.getStatus()).isEqualTo(ModerationStatus.APPROVED);

        Mockito.verify(commentStore).findById(created.getId());
        Mockito.verify(commentStore).save(updated);
    }

    @Test
    void throwsExceptionWhenCommentNotFound() {
        UUID missingId = UUID.randomUUID();

        Mockito.when(commentStore.findById(missingId)).thenReturn(Optional.empty());

        Assertions.assertThrows(CommentNotFoundException.class, () ->
                commentManger.updateStatus(missingId, ModerationStatus.APPROVED)
        );

        Mockito.verify(commentStore).findById(missingId);
        Mockito.verify(commentStore, Mockito.never()).save(Mockito.any(Comment.class));
    }

}
