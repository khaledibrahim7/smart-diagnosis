package com.khaled.smart_diagnosis.repository;

import com.khaled.smart_diagnosis.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

}
