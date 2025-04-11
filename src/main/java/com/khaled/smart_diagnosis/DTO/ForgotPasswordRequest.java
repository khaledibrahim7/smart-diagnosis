package com.khaled.smart_diagnosis.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequest {

    private String email;
    private String token;
    private String newPassword;
}
