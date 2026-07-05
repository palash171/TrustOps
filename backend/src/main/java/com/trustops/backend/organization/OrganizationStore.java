package com.trustops.backend.organization;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

//database access for organisations
//Spring creates the real object for this interface when the application starts
public interface OrganizationStore extends JpaRepository<Organization, UUID> {
    //method name tells Spring to SELECT an organisation WHERE api_key_hash matches
    Optional<Organization> findByApiKeyHash(String apiKeyHash); //Optional is empty when the key is unknown
}
