package com.trustops.backend.ingestion;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest //start the real TrustOps backend
@AutoConfigureMockMvc //create a tool that can send test HTTP requests
@Transactional //undo database changes after each test
class CommentIngestionHttpTests {

    @Autowired //Spring supplies its MockMvc object here
    private MockMvc mockMvc;

    @Test
    void acceptsNewEventAndRecognisesDuplicate() throws Exception {
        //random ID prevents this test colliding with older database comments
        String externalId = UUID.randomUUID().toString();

        String requestBody = """
                {
                    "source": "GENERIC_WEBHOOK",
                    "externalId": "%s",
                    "text": "Comment sent from another company"
                }
                """.formatted(externalId);

        //first delivery should create a new comment
        String firstResponse = mockMvc.perform(
                        post("/api/v1/ingestion/comments")
                                .header("X-API-Key", "trustops-dev-key")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.duplicate").value(false))
                .andExpect(jsonPath("$.comment.externalId").value(externalId))
                .andExpect(jsonPath("$.comment.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        //read the TrustOps comment ID returned by the first request
        String firstCommentId =
                JsonPath.read(firstResponse, "$.comment.id");

        //send the exact same company event again
        mockMvc.perform(
                        post("/api/v1/ingestion/comments")
                                .header("X-API-Key", "trustops-dev-key")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duplicate").value(true))
                //same ID proves we returned the old comment instead of creating another
                .andExpect(jsonPath("$.comment.id").value(firstCommentId));
    }

    @Test
    void rejectsWrongApiKey() throws Exception {
        String requestBody = """
                {
                    "source": "GENERIC_WEBHOOK",
                    "externalId": "wrong-key-test",
                    "text": "This should not be stored"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/ingestion/comments")
                                .header("X-API-Key", "wrong-key")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsMissingApiKey() throws Exception {
        String requestBody = """
                {
                    "source": "GENERIC_WEBHOOK",
                    "externalId": "missing-key-test",
                    "text": "This should not be stored"
                }
                """;

        //there is intentionally no X-API-Key header
        mockMvc.perform(
                        post("/api/v1/ingestion/comments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isUnauthorized());
    }
}