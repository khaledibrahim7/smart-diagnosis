package com.khaled.smart_diagnosis.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    private String email;
    private String password;
    private String confirmPassword;
    private String firstName;
    private String lastName;
    private Integer age;
    private String PhoneNumber;
    private String countryCode;
    private String gender;


}
