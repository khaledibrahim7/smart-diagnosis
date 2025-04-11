package com.khaled.smart_diagnosis.controller;


import com.khaled.smart_diagnosis.DTO.*;
import com.khaled.smart_diagnosis.exception.InvalidCredentialsException;
import com.khaled.smart_diagnosis.exception.UserAlreadyExistsException;
import com.khaled.smart_diagnosis.exception.UserNotFoundException;
import com.khaled.smart_diagnosis.model.PasswordResetToken;
import com.khaled.smart_diagnosis.model.Patient;
import com.khaled.smart_diagnosis.model.Settings;
import com.khaled.smart_diagnosis.repository.PasswordResetTokenRepository;
import com.khaled.smart_diagnosis.repository.PatientRepository;
import com.khaled.smart_diagnosis.repository.SettingsRepository;
import com.khaled.smart_diagnosis.security.JWTUtil;
import com.khaled.smart_diagnosis.service.EmailService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;


import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PatientRepository patientRepository;
    private final SettingsRepository settingsRepository;
    private final JWTUtil jwtUtil;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(PasswordResetTokenRepository passwordResetTokenRepository, PatientRepository patientRepository, SettingsRepository settingsRepository, JWTUtil jwtUtil, EmailService emailService) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.patientRepository = patientRepository;
        this.settingsRepository = settingsRepository;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (patientRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email is already taken!");
        }

        Patient newPatient = new Patient();
        newPatient.setEmail(request.getEmail());
        newPatient.setPassword(passwordEncoder.encode(request.getPassword()));
        newPatient.setFirstName(request.getFirstName());
        newPatient.setLastName(request.getLastName());
        newPatient.setAge(request.getAge());
        newPatient.setPhoneNumber(request.getPhoneNumber());
        newPatient.setGender(request.getGender());

        Patient savedPatient = patientRepository.save(newPatient);

        Settings settings = new Settings();
        settings.setPatient(savedPatient);
        settings.setLanguage("ar");
        settings.setDarkMode(false);

        settingsRepository.save(settings);

        try {
            emailService.sendWelcomeEmail(savedPatient.getEmail(), savedPatient.getFirstName());
        } catch (Exception e) {
            log.error("error while sending email, {}", e.getMessage());

        }

        String token = jwtUtil.generateToken(savedPatient.getEmail());

        return ResponseEntity.ok(new RegisterResponse(
                token,
                savedPatient.getId(),
                savedPatient.getFirstName(),
                savedPatient.getLastName(),
                savedPatient.getEmail(),
                savedPatient.getPhoneNumber(),
                savedPatient.getGender()
        ));
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Patient patient = patientRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), patient.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(patient.getEmail());

        return ResponseEntity.ok(new LoginResponse(
                token,
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName()
        ));
    }


    @PostMapping("/forgot-password")
    @Transactional
    public ResponseEntity<ApiResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        Optional<Patient> patientOptional = patientRepository.findByEmail(request.getEmail());
        if (patientOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Email not found"));
        }

        passwordResetTokenRepository.deleteByPatient(patientOptional.get());

        String token = UUID.randomUUID().toString().substring(0, 6);
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setPatient(patientOptional.get());
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(10));

        passwordResetTokenRepository.save(resetToken);

        emailService.sendResetCode(request.getEmail(), token);

        return ResponseEntity.ok(new ApiResponse(true, "Reset code sent to your email"));
    }


    @PostMapping("/verify-reset-code")
    @Transactional
    public ResponseEntity<ApiResponse> verifyResetCode(@RequestBody VerifyCodeRequest request) {
        Optional<Patient> patientOptional = patientRepository.findByEmail(request.getEmail());
        if (patientOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Email not found"));
        }

        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(request.getToken());

        if (tokenOptional.isEmpty() || !tokenOptional.get().getPatient().getEmail().equals(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Invalid reset code"));
        }

        if (tokenOptional.get().getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Reset code expired"));
        }

        return ResponseEntity.ok(new ApiResponse(true, "Reset code is valid"));
    }

    @PostMapping("/reset-password")
    @Transactional
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(request.getToken());

        if (tokenOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Invalid reset code"));
        }

        PasswordResetToken resetToken = tokenOptional.get();
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Reset code expired"));
        }

        Patient patient = resetToken.getPatient();
        patient.setPassword(passwordEncoder.encode(request.getNewPassword()));
        patientRepository.save(patient);

        passwordResetTokenRepository.delete(resetToken);

        return ResponseEntity.ok(new ApiResponse(true, "Password reset successfully"));
    }
}