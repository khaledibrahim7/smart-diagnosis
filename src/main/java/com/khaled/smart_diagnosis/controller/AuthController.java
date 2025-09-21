package com.khaled.smart_diagnosis.controller;


import com.khaled.smart_diagnosis.DTO.*;
import com.khaled.smart_diagnosis.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {
    private final AuthService authService;

<<<<<<< HEAD
    public AuthController(AuthService authService) {
        this.authService = authService;
=======
    private final PatientRepository patientRepository;
    private final JWTUtil jwtUtil;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(PatientRepository patientRepository, JWTUtil jwtUtil, EmailService emailService) {
        this.patientRepository = patientRepository;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
        this.passwordEncoder = new BCryptPasswordEncoder(); 
>>>>>>> 63038283ae7faba031b780d4587ce2b645cfe388
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
<<<<<<< HEAD
        return ResponseEntity.ok(authService.register(request));
=======
        if (patientRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email is already taken!");
        }

       
        Patient newPatient = new Patient();
        newPatient.setEmail(request.getEmail());
        newPatient.setPassword(passwordEncoder.encode(request.getPassword()));
        newPatient.setFirstName(request.getFirstName());
        newPatient.setLastName(request.getLastName());
        newPatient.setAge(request.getAge());

        Patient savedPatient = patientRepository.save(newPatient);

        emailService.sendWelcomeEmail(savedPatient.getEmail(), savedPatient.getFirstName());

        String token = jwtUtil.generateToken(savedPatient.getEmail());

        return ResponseEntity.ok(new RegisterResponse(
                token,
                savedPatient.getId(),
                savedPatient.getFirstName(),
                savedPatient.getLastName(),
                savedPatient.getEmail()
        ));
>>>>>>> 63038283ae7faba031b780d4587ce2b645cfe388
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
<<<<<<< HEAD
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<StatusResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<StatusResponse> verifyResetCode(@RequestBody VerifyCodeRequest request) {
        return ResponseEntity.ok(authService.verifyResetCode(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<StatusResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
=======
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
>>>>>>> 63038283ae7faba031b780d4587ce2b645cfe388
    }
}
