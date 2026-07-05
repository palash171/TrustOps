package com.trustops.backend.organization;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

//one customer company using TrustOps
//Hibernate uses each row in organizations to build one Organization object
@Entity //tell Hibernate this class is stored in the database
@Table(name = "organizations") //connect this class to the organizations table
public class Organization {

    @Id //primary key for one company
    UUID id;

    @Column(nullable = false, length = 200) //company name is required and limited to 200 letters
    private String name;

    /**
     *  store hash rather than the real secret API key to secure key
     */
    @Column(name = "api_key_hash", nullable = false, unique = true, length = 64)
    private String apiKeyHash;

    @Column(name = "created_at", nullable = false) //Java createdAt connects to SQL created_at
    private Instant createdAt;

    //we use this constructor when our Java code creates an organisation object
    public Organization(
            UUID id,
            String name,
            String apiKeyHash,
            Instant createdAt
    ) {
        this.id = id;
        this.name = name;
        this.apiKeyHash = apiKeyHash;
        this.createdAt = createdAt;
    }

    //Hibernate calls this empty constructor when rebuilding an object from a database row
    protected Organization() {
    }

    //getters let the authentication service read the company information
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getApiKeyHash() {
        return apiKeyHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
