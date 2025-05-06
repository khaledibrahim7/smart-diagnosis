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
public class ChatSessionDTO {
    private Long id;
    private String title;
    private LocalDateTime createdAt;
}
