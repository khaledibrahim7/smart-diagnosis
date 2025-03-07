package com.khaled.smart_diagnosis.controller;


import com.khaled.smart_diagnosis.DTO.LoginRequest;
import com.khaled.smart_diagnosis.DTO.LoginResponse;
import com.khaled.smart_diagnosis.DTO.RegisterRequest;
import com.khaled.smart_diagnosis.DTO.RegisterResponse;
import com.khaled.smart_diagnosis.exception.InvalidCredentialsException;
import com.khaled.smart_diagnosis.exception.UserAlreadyExistsException;
import com.khaled.smart_diagnosis.exception.UserNotFoundException;
import com.khaled.smart_diagnosis.model.Patient;
import com.khaled.smart_diagnosis.repository.PatientRepository;
import com.khaled.smart_diagnosis.security.JWTUtil;
import com.khaled.smart_diagnosis.service.EmailService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;





@Slf4j
//@CrossOrigin(origins = "*") // يفضل تحديد نطاق معين بدلاً من السماح للجميع
@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final PatientRepository patientRepository;
    private final JWTUtil jwtUtil;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(PatientRepository patientRepository, JWTUtil jwtUtil, EmailService emailService) {
        this.patientRepository = patientRepository;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
        this.passwordEncoder = new BCryptPasswordEncoder(); // 🔹 تهيئة مشفر كلمة المرور
    }

    // تسجيل مستخدم جديد
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (patientRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email is already taken!");
        }

        // إنشاء وحفظ المستخدم الجديد مع تشفير كلمة المرور
        Patient newPatient = new Patient();
        newPatient.setEmail(request.getEmail());
        newPatient.setPassword(passwordEncoder.encode(request.getPassword())); // 🔹 تشفير كلمة المرور
        newPatient.setFirstName(request.getFirstName());
        newPatient.setLastName(request.getLastName());
        newPatient.setAge(request.getAge());

        Patient savedPatient = patientRepository.save(newPatient);

        // 📩 إرسال إيميل الترحيب بعد التسجيل
        emailService.sendWelcomeEmail(savedPatient.getEmail(), savedPatient.getFirstName());

        // 🔹 إنشاء توكن JWT
        String token = jwtUtil.generateToken(savedPatient.getEmail());

        return ResponseEntity.ok(new RegisterResponse(
                token,
                savedPatient.getId(),
                savedPatient.getFirstName(),
                savedPatient.getLastName(),
                savedPatient.getEmail()
        ));
    }

    // تسجيل الدخول
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // التحقق من وجود المستخدم في قاعدة البيانات
        Patient patient = patientRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // التحقق من صحة كلمة المرور باستخدام BCrypt
        if (!passwordEncoder.matches(request.getPassword(), patient.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        // 🔹 إنشاء توكن للمستخدم بعد التحقق
        String token = jwtUtil.generateToken(patient.getEmail());

        // إرجاع الاستجابة مع التوكن وبيانات المستخدم
        return ResponseEntity.ok(new LoginResponse(
                token,
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName()
        ));
    }
}