package com.khaled.smart_diagnosis.DTO;

import com.khaled.smart_diagnosis.model.Settings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSettingsRequest {
    private Settings settings;
    private String newPassword;
    private String confirmPassword;

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Integer age;
    private String gender;
}
