package com.khaled.smart_diagnosis.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterResponse {

    private String token;
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private  String phoneNumber;
    private String gender;
}
