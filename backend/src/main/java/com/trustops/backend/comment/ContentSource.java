package com.trustops.backend.comment;

/**
 * Where the comment came from
 * TRUSTOPS_DEMO -> content created for testing purposes
 * GENERIC_WEBHOOK -> content sent from another company
 * COMPANY_FORUM -> future implementation
 */
public enum ContentSource {
    TRUSTOPS_DEMO, //content typed into our local dashboard form
    GENERIC_WEBHOOK, //content automatically sent by another company's backend
    COMPANY_FORUM //future direct company forum integration
}
