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

    //customer company that owns this comment
    //UUID points to organizations.id and PostgreSQL foreign key checks it exists
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    //Where the customer says the comment originated.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ContentSource source;


     // ID assigned by the customer's own system.
    @Column(name = "external_id", nullable = false, length = 255)
    private String externalId;

    /** above  after hibernating
     * INSERT INTO comments (comment_id,body, moderation_status,received_at)
     * VALUES (id,'Hello','PENDING',time);
    */
    //constructor
    public Comment(
            UUID id,
            UUID organizationId,
            ContentSource source,
            String externalId,
            String text,
            ModerationStatus status,
            Instant receivedAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.source = source;
        this.externalId = externalId;
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
    public UUID getOrganizationId() {return organizationId;}
    public ContentSource getSource() {return source;}
    public String getExternalId() {return externalId;}
}
