package com.trustops.backend.organization;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * One customer company using TrustOps.
 *
 * Spring/Hibernate creates Organization objects from rows in the
 * organizations table.
 */

@Entity
@Table(name = "organizations")
public class Organization {

    @Id
    UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    /**
     * We store a hash rather than the real secret API key.
     * If the database leaked, the raw key would not be sitting here.
     */
    @Column(name = "api_key_hash", nullable = false, unique = true, length = 64)
    private String apiKeyHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

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

    /**
     * Hibernate needs a no-argument constructor when it rebuilds
     * an object from a database row.
     */
    protected Organization() {
    }

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
}