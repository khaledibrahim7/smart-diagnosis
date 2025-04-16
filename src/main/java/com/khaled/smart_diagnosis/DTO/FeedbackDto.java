package com.khaled.smart_diagnosis.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDto {

    private String type;
    private String description;
    private String patientEmail;
}
