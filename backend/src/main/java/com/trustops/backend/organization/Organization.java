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
    @
}