package com.khaled.smart_diagnosis.service;

import com.khaled.smart_diagnosis.DTO.SettingResponse;
import com.khaled.smart_diagnosis.DTO.UpdateSettingsRequest;
import com.khaled.smart_diagnosis.model.ChatSession;
import com.khaled.smart_diagnosis.model.Patient;
import com.khaled.smart_diagnosis.model.Settings;
import com.khaled.smart_diagnosis.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;

@Service
@Slf4j
public class SettingsService {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SettingsRepository settingsRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private ChatSessionRepository chatSessionRepository;
    @Autowired
    private FeedbackRepository feedbackRepository;


    @Transactional
    public Optional<SettingResponse> getSettingsByPatientId(Long patientId) {
        System.out.println("🟠 Fetching settings from DB for patientId: " + patientId);
        Optional<Settings> settingsOpt = settingsRepository.findByPatientId(patientId);
        Optional<Patient> patientOpt = patientRepository.findById(patientId);

        if (settingsOpt.isPresent() && patientOpt.isPresent()) {
            Settings settings = settingsOpt.get();
            Patient patient = patientOpt.get();

            SettingResponse settingResponse = new SettingResponse(
                    patient.getFirstName(),
                    patient.getLastName(),
                    patient.getEmail(),
                    patient.getPhoneNumber(),
                    patient.getAge(),
                    patient.getGender(),
                    settings.getLanguage(),
                    settings.isDarkMode()
            );

            return Optional.of(settingResponse);
        }

        return Optional.empty();
    }

    public SettingResponse createDefaultSettings(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("Patient cannot be null");
        }

        Settings settings = settingsRepository.findByPatientId(patient.getId())
                .orElseGet(() -> {
                    Settings newSettings = new Settings();
                    newSettings.setPatient(patient);
                    newSettings.setLanguage("en");
                    newSettings.setDarkMode(false);
                    settingsRepository.save(newSettings);
                    return newSettings;
                });

        return new SettingResponse(
                patient.getFirstName(),
                patient.getLastName(),
                patient.getEmail(),
                patient.getPhoneNumber(),
                patient.getAge(),
                patient.getGender(),
                settings.getLanguage(),
                settings.isDarkMode()
        );
    }

    @Transactional
    public Optional<SettingResponse> updateSettings(Long patientId, UpdateSettingsRequest request) {
        Settings newSettings = request.getSettings();
        String newPassword = request.getNewPassword();
        String confirmPassword = request.getConfirmPassword();


        if (newPassword != null && !newPassword.equals(confirmPassword)) {
            log.warn("❌ Passwords do not match for patientId: {}", patientId);
            return Optional.empty();
        }


        Optional<Settings> settingsOpt = settingsRepository.findByPatientId(patientId);
        if (!settingsOpt.isPresent()) {
            log.warn("❌ Settings not found for patientId: {}", patientId);
            return Optional.empty();
        }


        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        if (!patientOpt.isPresent()) {
            log.warn("❌ Patient not found for patientId: {}", patientId);
            return Optional.empty();
        }


        Settings settings = settingsOpt.get();
        settings.setLanguage(newSettings.getLanguage());
        settings.setDarkMode(newSettings.isDarkMode());
        settingsRepository.save(settings);


        Patient patient = patientOpt.get();
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setAge(request.getAge());
        patient.setGender(request.getGender());


        if (newPassword != null && !newPassword.isEmpty()) {
            String encryptedPassword = passwordEncoder.encode(newPassword);
            patient.setPassword(encryptedPassword);
            log.info("🔑 Password updated successfully for patientId: {}", patientId);
        }

        patientRepository.save(patient);


        SettingResponse settingResponse = new SettingResponse(
                patient.getFirstName(),
                patient.getLastName(),
                patient.getEmail(),
                patient.getPhoneNumber(),
                patient.getAge(),
                patient.getGender(),
                settings.getLanguage(),
                settings.isDarkMode()
        );

        return Optional.of(settingResponse);
    }



    @Transactional
    public boolean hardDeleteAccount(Long patientId) {
        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        if (patientOpt.isPresent()) {
            passwordResetTokenRepository.deleteByPatientId(patientId);

            feedbackRepository.deleteByPatientId(patientId);

            chatSessionRepository.deleteByPatientId(patientId);

            settingsRepository.findByPatientId(patientId).ifPresent(settingsRepository::delete);

            patientRepository.delete(patientOpt.get());

            log.info("🗑️ Patient and related data deleted permanently for patientId: {}", patientId);
            return true;
        }
        log.warn("❌ Patient not found for deletion. patientId: {}", patientId);
        return false;
    }

}

