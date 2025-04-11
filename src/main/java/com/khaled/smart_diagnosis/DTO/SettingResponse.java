package com.khaled.smart_diagnosis.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettingResponse {

    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Integer age;
    private String gender;


    private String language;
    private boolean darkMode;

    public SettingResponse(String passwordsDoNotMatch) {
    }
}
