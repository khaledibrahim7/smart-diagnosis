package com.khaled.smart_diagnosis.service;

import com.khaled.smart_diagnosis.DTO.SettingResponse;
import com.khaled.smart_diagnosis.model.Patient;
import com.khaled.smart_diagnosis.model.Settings;
import com.khaled.smart_diagnosis.repository.PatientRepository;
import com.khaled.smart_diagnosis.repository.SettingsRepository;
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
    public Optional<SettingResponse> updateSettings(Long patientId, Settings newSettings, String newPassword) {
        return settingsRepository.findByPatientId(patientId).flatMap(settings -> {
            settings.setLanguage(newSettings.getLanguage());
            settings.setDarkMode(newSettings.isDarkMode());
            settingsRepository.save(settings);

            Optional<Patient> patientOpt = patientRepository.findById(patientId);
            if (patientOpt.isPresent()) {
                Patient patient = patientOpt.get();

                if (newPassword != null && !newPassword.isEmpty()) {
                    String encryptedPassword = passwordEncoder.encode(newPassword);
                    patient.setPassword(encryptedPassword);
                    patientRepository.save(patient);
                    log.info("🔑 Password updated successfully for patientId: {}", patientId);
                }

                return Optional.of(new SettingResponse(
                        patient.getFirstName(),
                        patient.getLastName(),
                        patient.getEmail(),
                        patient.getPhoneNumber(),
                        patient.getAge(),
                        patient.getGender(),
                        settings.getLanguage(),
                        settings.isDarkMode()
                ));
            }

            return Optional.empty();
        });
    }


    @Transactional
    public boolean hardDeleteAccount(Long patientId) {
        Optional<Patient> patientOpt = patientRepository.findById(patientId);

        if (patientOpt.isPresent()) {
            patientRepository.delete(patientOpt.get());
            System.out.println("🗑️ Patient and settings deleted permanently.");
            return true;
        }

        System.out.println("❌ Patient not found for hard deletion.");
        return false;
    }


}

