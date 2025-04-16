package com.khaled.smart_diagnosis.controller;


import com.khaled.smart_diagnosis.DTO.FeedbackDto;
import com.khaled.smart_diagnosis.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Autowired
    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService  = feedbackService;
    }

    @PostMapping
    public ResponseEntity<String> submitFeedback(@RequestBody FeedbackDto feedbackDto) {
        feedbackService.submitComplaint(feedbackDto);
        return ResponseEntity.ok("تم إرسال الشكوى بنجاح");
    }
}