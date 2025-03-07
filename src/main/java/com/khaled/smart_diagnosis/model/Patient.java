package com.khaled.smart_diagnosis.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "patient_seq", sequenceName = "ISEQ$$_72926", allocationSize = 1)

public class Patient {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "patient_seq")
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false) // لو العمر مطلوب
    private Integer age;

    @PrePersist
    @PreUpdate
    private void setDefaultValues() {
        if (this.firstName == null || this.firstName.trim().isEmpty()) {
            this.firstName = "Unknown";
        }
        if (this.lastName == null || this.lastName.trim().isEmpty()) {
            this.lastName = "Unknown";
        }
    }
}