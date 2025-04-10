package com.khaled.smart_diagnosis.repository;

import com.khaled.smart_diagnosis.model.Patient;
import com.khaled.smart_diagnosis.model.Settings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SettingsRepository extends JpaRepository<Settings, Long> {
    @Query("SELECT s FROM Settings s JOIN FETCH s.patient WHERE s.patient.id = :patientId")
    Optional<Settings> findByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Settings s WHERE s.patient.id = :patientId")
    boolean existsByPatientId(@Param("patientId") Long patientId);


}

