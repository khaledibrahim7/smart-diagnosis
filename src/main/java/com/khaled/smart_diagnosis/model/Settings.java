package com.khaled.smart_diagnosis.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Settings {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "id", nullable = false, unique = true)
    @JsonManagedReference
    private Patient patient;

    @Column(nullable = false)
    private String language = "en";

    @Column(nullable = false)
    private boolean darkMode = false;
}

