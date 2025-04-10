package com.khaled.smart_diagnosis.controller;


import com.khaled.smart_diagnosis.DTO.SettingResponse;
import com.khaled.smart_diagnosis.model.Settings;
import com.khaled.smart_diagnosis.service.SettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Validated
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/settings")

public class SettingsController {

    private final SettingsService settingsService;

    @Autowired
    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    // 🔹 Get settings for a specific patient
    @GetMapping("/{patientId}")
    public ResponseEntity<SettingResponse> getSettings(@PathVariable Long patientId) {
        log.info("🔵 [GET] Fetching settings for patientId: {}", patientId);

        return settingsService.getSettingsByPatientId(patientId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("❌ No settings found for patientId: {}", patientId);
                    return ResponseEntity.notFound().build();
                });
    }
    @PutMapping("/{patientId}")
    public ResponseEntity<SettingResponse> updateSettings(
            @PathVariable Long patientId,
            @Valid @RequestBody Settings newSettings,
            @RequestParam(required = false) String newPassword
    ) {
        log.info("🟠 [PUT] Updating settings for patientId: {}", patientId);

        Optional<SettingResponse> updatedSettingResponse = settingsService.updateSettings(patientId, newSettings, newPassword);

        return updatedSettingResponse
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("❌ Failed to update settings, patientId not found: {}", patientId);
                    return ResponseEntity.notFound().build();
                });
    }


    // 🗑️ Delete account and settings for a specific patient
    @DeleteMapping("/{patientId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long patientId) {
        log.info("🗑️ [DELETE] Deleting account and settings for patientId: {}", patientId);

        boolean deleted = settingsService.hardDeleteAccount(patientId);
        if (deleted) {
            log.info("✅ Account and settings deleted for patientId: {}", patientId);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("❌ Failed to delete account. Patient not found: {}", patientId);
            return ResponseEntity.notFound().build();
        }
    }
}

