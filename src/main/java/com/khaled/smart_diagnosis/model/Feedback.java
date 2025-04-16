package com.khaled.smart_diagnosis.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "FEEDBACK")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "complaint_seq")
    @SequenceGenerator(name = "complaint_seq", sequenceName = "complaint_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, length = 1000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

}
