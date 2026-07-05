package com.trustops.backend.ingestion;

import com.trustops.backend.organization.Organization;
import com.trustops.backend.organization.OrganizationStore;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

//turns the secret API key into the company that owns it
//the raw API key is never stored in PostgreSQL
@Service //Spring creates one authenticator object and injects it where needed
public class ApiKeyAuthenticator {

    private final OrganizationStore organizationStore; //used to search companies by the hashed key

    //Spring creates this object and supplies OrganizationStore
    public ApiKeyAuthenticator(OrganizationStore organizationStore) {
        this.organizationStore = organizationStore;
    }

    public Organization authenticate(String rawApiKey) {
        //missing or blank key means the caller has not proved who they are
        if (rawApiKey == null || rawApiKey.isBlank()) throw new InvalidApiKeyException();

        String apiKeyHash = sha256(rawApiKey); //hash incoming key so we compare hash to hash

        //return company if found otherwise throw 401 exception
        return organizationStore.findByApiKeyHash(apiKeyHash).orElseThrow(InvalidApiKeyException::new);
    }

    //helper method only handles changing a String key into a SHA-256 String
    private String sha256(String value) {
        try {MessageDigest digest = MessageDigest.getInstance("SHA-256"); //use javs inbuilt hashing

            byte[] hashedBytes = digest.digest(value.getBytes(StandardCharsets.UTF_8)); //converts the string into a consistent byte format

            return HexFormat.of().formatHex(hashedBytes); //turn unreadable bytes into 64 readable characters
        } catch (NoSuchAlgorithmException exception) {
            //Every Java runtime has SHA-256, If it not= broken server configuration,
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
