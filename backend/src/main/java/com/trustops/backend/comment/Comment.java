package com.trustops.backend.comment;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity //entire class will be stored inside a data base
@Table(name = "comments") // store in table called contents
public class Comment {


    @Id //each feilds uniquely identities each row
    private UUID id;

    @Column(nullable = false, length = 5000)
    private String text;

    @Enumerated(EnumType.STRING) //store enums word
    @Column(nullable = false, length=20)
    private ModerationStatus status;

    @Column(nullable = false)
    private Instant receivedAt;

    /** what the above looks like after hibernating
     * INSERT INTO comments (comment_id,body, moderation_status,received_at)
     * VALUES (id,'Hello','PENDING',time);
    */
    //constructor
    public Comment(UUID id, String text, ModerationStatus status, Instant receivedAt) {
        this.id = id;
        this.text = text;
        this.status = status;
        this.receivedAt = receivedAt;
    }

    //default constructor
    protected Comment() {}

    //getters
    public UUID getId() {return id;}
    public String getText() {return text;}
    public ModerationStatus getStatus() {return status;}
    public Instant getReceivedAt() {return receivedAt;}
}
