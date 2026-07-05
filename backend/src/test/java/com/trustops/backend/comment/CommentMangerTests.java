package com.trustops.backend.comment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

class CommentMangerTests {

    private final CommentStore commentStore = Mockito.mock(CommentStore.class);

    private final CommentManger commentManger = new CommentManger(commentStore);
    Pageable pageable = PageRequest.of(0, 20);

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

        assertThat(created.getOrganizationId()).isEqualTo(CommentManger.LOCAL_DEMO_ORGANIZATION_ID);

        assertThat(created.getSource()).isEqualTo(ContentSource.TRUSTOPS_DEMO);

        assertThat(created.getExternalId()).isEqualTo(created.getId().toString());
    }

    @Test
    void returnsEmptyPageWhenNoCommentsExist() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Comment> emptyPage = Page.empty(pageable);

        Mockito.when(commentStore.findAllByOrderByReceivedAtDesc(Mockito.any(Pageable.class))).thenReturn(emptyPage);

        Page<Comment> page = commentManger.findAll(pageable);

        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getSize()).isEqualTo(20);
        assertThat(page.getTotalElements()).isEqualTo(0);
        assertThat(page.getContent()).isEmpty();

    }
    @Test
    void returnsPageFilteredByStatus (){

        Pageable pageable = PageRequest.of(0, 20);
        UUID id = UUID.randomUUID();

        Comment pendingComment = new Comment(
                id,
                CommentManger.LOCAL_DEMO_ORGANIZATION_ID,
                ContentSource.TRUSTOPS_DEMO,
                id.toString(),
                "Existing comment",
                ModerationStatus.PENDING,
                Instant.now()
        );

        Page<Comment> page = new PageImpl<>(List.of(pendingComment),pageable,1);

        Mockito.when(commentStore.findAllByStatusOrderByReceivedAtDesc(ModerationStatus.PENDING, pageable)).thenReturn(page);

        Page<Comment> result = commentManger.findByStatus(ModerationStatus.PENDING, pageable);

        assertThat(result.getContent()).containsExactly(pendingComment);

        assertThat(result.getTotalElements()).isEqualTo(1);

        Mockito.verify(commentStore).findAllByStatusOrderByReceivedAtDesc(ModerationStatus.PENDING, pageable);
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

        assertThat(updated.getOrganizationId()).isEqualTo(created.getOrganizationId());

        assertThat(updated.getSource()).isEqualTo(created.getSource());

        assertThat(updated.getExternalId()).isEqualTo(created.getExternalId());
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
    @Test
    void findsCommentById() {
        UUID id = UUID.randomUUID();
        Comment existing = new Comment(
                id,
                CommentManger.LOCAL_DEMO_ORGANIZATION_ID,
                ContentSource.TRUSTOPS_DEMO,
                id.toString(),
                "Existing comment",
                ModerationStatus.PENDING,
                Instant.now()
        );

        Mockito.when(commentStore.findById(id)).thenReturn(Optional.of(existing));

        Comment res = commentManger.findById(id);
        assertThat(res).isEqualTo(existing);
        Mockito.verify(commentStore).findById(id);
    }

}
