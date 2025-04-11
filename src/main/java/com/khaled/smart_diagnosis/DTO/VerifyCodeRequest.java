package com.khaled.smart_diagnosis.DTO;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class VerifyCodeRequest {
    private String email;
    private String token;
}
