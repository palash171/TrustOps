package com.trustops.backend.ingestion;

import com.trustops.backend.organization.Organization;
import com.trustops.backend.organization.OrganizationStore;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApiKeyAuthenticatorTests {

    //fake database access so this test only checks authentication logic
    private final OrganizationStore organizationStore =
            Mockito.mock(OrganizationStore.class);

    //real class being tested but it receives fake database access
    private final ApiKeyAuthenticator authenticator =
            new ApiKeyAuthenticator(organizationStore);

    private final Organization organization =
            new Organization(
                    UUID.randomUUID(),
                    "Test Company",
                    "stored-hash",
                    Instant.now()
            );

    @Test
    void returnsOrganizationForValidApiKey() {
        String expectedHash =
                "85860a909898a76de6e5cc70b35833cab7804bd8e215cee1c23a851c296d18a8";

        //tell fake repository what to return for our expected SHA-256 hash
        Mockito.when(
                organizationStore.findByApiKeyHash(expectedHash)
        ).thenReturn(Optional.of(organization));

        //method hashes raw key then searches fake repository
        Organization result =
                authenticator.authenticate("trustops-dev-key");

        assertThat(result).isSameAs(organization);

        //also proves raw key was converted into the correct hash
        Mockito.verify(organizationStore)
                .findByApiKeyHash(expectedHash);
    }

    @Test
    void rejectsMissingApiKeyWithoutSearchingDatabase() {
        assertThrows(
                InvalidApiKeyException.class,
                () -> authenticator.authenticate(null)
        );

        assertThrows(
                InvalidApiKeyException.class,
                () -> authenticator.authenticate("   ")
        );

        //blank keys should fail before repository is called
        Mockito.verifyNoInteractions(organizationStore);
    }

    @Test
    void rejectsUnknownApiKey() {
        //any hashed key returns no organisation
        Mockito.when(
                organizationStore.findByApiKeyHash(
                        Mockito.anyString()
                )
        ).thenReturn(Optional.empty());

        assertThrows(
                InvalidApiKeyException.class,
                () -> authenticator.authenticate("wrong-key")
        );

        Mockito.verify(organizationStore)
                .findByApiKeyHash(Mockito.anyString());
    }
}