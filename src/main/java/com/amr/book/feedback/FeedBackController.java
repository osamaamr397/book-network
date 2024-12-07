package com.amr.book.feedback;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(name = "feedbacks")
@RequiredArgsConstructor
@Tag(name = "Feedback")
public class FeedBackController {
    private final FeedbackService service;
    @PostMapping
    public ResponseEntity<Integer>saveFeedback(
            @Valid @RequestBody FeedbackRequest request,
            Authentication connectedUser
    ){
        return ResponseEntity.ok(service.save(request,connectedUser));
    }
}
