package com.khaled.smart_diagnosis.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {

    private Long id;
    private String content;
    private boolean fromPatient;
    private LocalDateTime timestamp;
}