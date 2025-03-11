package com.khaled.smart_diagnosis.service;

import com.khaled.smart_diagnosis.DTO.LoginResponse;
import com.khaled.smart_diagnosis.DTO.RegisterRequest;
import com.khaled.smart_diagnosis.exception.*;
import com.khaled.smart_diagnosis.model.Patient;
import com.khaled.smart_diagnosis.repository.PatientRepository;
import com.khaled.smart_diagnosis.security.JWTUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final HttpSession session; 

    public void register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordsDoNotMatchException("Passwords do not match!");
        }

        if (patientRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email is already registered!");
        }

        Patient patient = new Patient();
        patient.setEmail(request.getEmail());
        patient.setPassword(passwordEncoder.encode(request.getPassword()));
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setAge(request.getAge());

        patientRepository.save(patient);
    }

    public void login(String email, String password) {
        Patient patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (!passwordEncoder.matches(password, patient.getPassword())) {
            throw new AuthenticationFailedException("Invalid email or password.");
        }

    
        session.setAttribute("loggedInUser", patient);
    }

    public void logout() {
        session.invalidate(); 
    }
}
