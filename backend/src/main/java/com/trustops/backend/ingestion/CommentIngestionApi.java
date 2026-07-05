package com.trustops.backend.ingestion;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//HTTP entrance used by another company's backend
//controller reads HTTP while CommentIngestionService handles the actual workflow
@RestController //Spring sends matching HTTP requests into this class
@RequestMapping("/api/v1/ingestion/comments") //base URL for company comment ingestion
public class CommentIngestionApi {

    private final CommentIngestionService ingestionService; //service that authenticates and stores the event

    //Spring supplies its managed CommentIngestionService object here
    public CommentIngestionApi(
            CommentIngestionService ingestionService
    ) {
        this.ingestionService = ingestionService;
    }

    @PostMapping //run this method for POST /api/v1/ingestion/comments
    public ResponseEntity<IngestionResult> ingest(
            //read the company's key from the HTTP header instead of the JSON body
            @RequestHeader(
                    name = "X-API-Key",
                    required = false //let our code return 401 instead of Spring returning 400
            )
            String apiKey,

            @Valid //run record validation rules before entering the method
            @RequestBody //Jackson turns incoming JSON into IngestCommentRequest
            IngestCommentRequest request
    ) {
        //controller passes clean Java values into the business service
        IngestionResult result =
                ingestionService.ingest(apiKey, request);

        //new comment gives 201 while repeated delivery gives 200
        HttpStatus responseStatus = result.duplicate()
                ? HttpStatus.OK
                : HttpStatus.CREATED;

        return ResponseEntity //lets us return both our chosen status and JSON body
                .status(responseStatus)
                .body(result);
    }
}
