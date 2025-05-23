package com.khaled.smart_diagnosis.service;

import com.khaled.smart_diagnosis.DTO.*;
import com.khaled.smart_diagnosis.exception.*;
import com.khaled.smart_diagnosis.model.PasswordResetToken;
import com.khaled.smart_diagnosis.model.Patient;
import com.khaled.smart_diagnosis.repository.PasswordResetTokenRepository;
import com.khaled.smart_diagnosis.repository.PatientRepository;

import com.khaled.smart_diagnosis.security.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final PatientRepository patientRepository;
    private final SettingsService settingsService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final JWTUtil jwtUtil;
    private final HttpSession session;
    private final PhoneValidationService phoneValidationService;
    private final HttpServletRequest httpRequest;

    public RegisterResponse register(RegisterRequest registerRequest) {

        String clientIp = httpRequest.getRemoteAddr();
        String countryCode = phoneValidationService.getCountryCode(clientIp);

        if (!phoneValidationService.isValidPhoneNumber(registerRequest.getPhoneNumber(), countryCode)) {
            throw new ValidationException("Invalid phone number for country code: " + countryCode);
        }

        validateRegisterRequest(registerRequest);

        Patient patient = new Patient();
        patient.setEmail(registerRequest.getEmail());
        patient.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        patient.setFirstName(registerRequest.getFirstName());
        patient.setLastName(registerRequest.getLastName());
        patient.setAge(registerRequest.getAge());
        patient.setPhoneNumber(registerRequest.getPhoneNumber());
        patient.setGender(registerRequest.getGender());

        Patient savedPatient = patientRepository.save(patient);
        settingsService.createDefaultSettings(savedPatient);

        try {
            emailService.sendWelcomeEmail(savedPatient.getEmail(), savedPatient.getFirstName());
        } catch (Exception e) {
            log.error("Error while sending welcome email: {}", e.getMessage());
        }

        String token = jwtUtil.generateToken(savedPatient.getEmail());

        return new RegisterResponse(
                token,
                savedPatient.getId(),
                savedPatient.getFirstName(),
                savedPatient.getLastName(),
                savedPatient.getEmail(),
                savedPatient.getPhoneNumber(),
                savedPatient.getGender()
        );
    }


    private void validateRegisterRequest(RegisterRequest registerRequest) {
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new PasswordsDoNotMatchException("Passwords do not match!");
        }

        boolean hasUppercase = Pattern.compile("[A-Z]").matcher(registerRequest.getPassword()).find();
        boolean hasLowercase = Pattern.compile("[a-z]").matcher(registerRequest.getPassword()).find();
        boolean hasNumber = Pattern.compile("\\d").matcher(registerRequest.getPassword()).find();
        boolean hasSpecialChar = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]").matcher(registerRequest.getPassword()).find();

        if (registerRequest.getPassword().length() < 5){
            throw new ValidationException("Password should have at least 5 chars");

        }
         if(!hasUppercase && !hasLowercase && !hasNumber && !hasSpecialChar){
            throw new ValidationException("Password should have at least one upper case, one lower case, one number and one special chars");
         }

        if (patientRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email is already registered!");
        }
    }

    public LoginResponse login(LoginRequest request) {
        Patient patient = patientRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), patient.getPassword())) {
            throw new AuthenticationFailedException("Invalid email or password.");
        }

        session.setAttribute("loggedInUser", patient);

        String token = jwtUtil.generateToken(patient.getEmail());

        return new LoginResponse(
                token,
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName()
        );
    }
    @Transactional
    public ApiResponse forgotPassword(ForgotPasswordRequest request) {
        Optional<Patient> patientOptional = patientRepository.findByEmail(request.getEmail());

        String genericMessage = "If an account with that email exists, a reset code has been sent.";

        if (patientOptional.isEmpty()) {
            return new ApiResponse(true, genericMessage);
        }

        Patient patient = patientOptional.get();

        // Delete existing tokens
        passwordResetTokenRepository.deleteByPatient(patient);

        String token = generateSecureCode(6);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setPatient(patient);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(10));

        passwordResetTokenRepository.save(resetToken);
        emailService.sendResetCode(request.getEmail(), token);

        return new ApiResponse(true, genericMessage);
    }

    @Transactional
    public ApiResponse verifyResetCode(VerifyCodeRequest request) {
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(request.getToken());

        if (tokenOptional.isEmpty() ||
                tokenOptional.get().getExpiryDate().isBefore(LocalDateTime.now()) ||
                !tokenOptional.get().getPatient().getEmail().equalsIgnoreCase(request.getEmail())) {

            return new ApiResponse(false, "Invalid or expired reset code");
        }

        return new ApiResponse(true, "Reset code is valid");
    }

    @Transactional
    public ApiResponse resetPassword(ResetPasswordRequest request) {
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(request.getToken());

        if (tokenOptional.isEmpty()) {
            return new ApiResponse(false, "Invalid reset code");
        }

        PasswordResetToken token = tokenOptional.get();

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            return new ApiResponse(false, "Reset code expired");
        }

        if (!isPasswordComplex(request.getNewPassword())) {
            return new ApiResponse(false,
                    "Password must be at least 8 characters, contain uppercase, lowercase, number and special character.");
        }

        Patient patient = token.getPatient();
        patient.setPassword(passwordEncoder.encode(request.getNewPassword()));
        patientRepository.save(patient);
        passwordResetTokenRepository.delete(token);

        return new ApiResponse(true, "Password reset successfully");
    }

    private String generateSecureCode(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }

        return code.toString();
    }

    private boolean isPasswordComplex(String password) {
        return password != null &&
                password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*") &&
                password.matches(".*[!@#$%^&*()\\-+=].*");
    }

}
