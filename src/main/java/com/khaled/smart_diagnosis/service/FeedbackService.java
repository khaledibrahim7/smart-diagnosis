package com.khaled.smart_diagnosis.service;


import com.khaled.smart_diagnosis.DTO.FeedbackDto;
import com.khaled.smart_diagnosis.model.Feedback;
import com.khaled.smart_diagnosis.model.Patient;
import com.khaled.smart_diagnosis.repository.FeedbackRepository;
import com.khaled.smart_diagnosis.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final PatientRepository patientRepository;

    @Autowired
    public FeedbackService( FeedbackRepository feedbackRepository, PatientRepository patientRepository) {
        this.feedbackRepository = feedbackRepository;
        this.patientRepository = patientRepository;
    }

    public void submitComplaint(FeedbackDto dto) {
        Patient patient = patientRepository.findByEmail(dto.getPatientEmail())
                .orElseThrow(() -> new RuntimeException("Patient not found with email: " + dto.getPatientEmail()));

        Feedback feedback = new Feedback();
        feedback.setType(dto.getType());
        feedback.setDescription(dto.getDescription());
        feedback.setPatient(patient);

       feedbackRepository.save(feedback);
    }
}
