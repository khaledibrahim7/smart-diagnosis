package com.khaled.smart_diagnosis.repository;

import com.khaled.smart_diagnosis.model.PasswordResetToken;
import com.khaled.smart_diagnosis.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByPatient(Patient patient);
    void deleteByPatientId(Long patientId);

}
