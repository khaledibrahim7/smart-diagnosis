package com.khaled.smart_diagnosis.ResponseWrapper;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseWrapper<T> {

    private T data;
    private String message;
    private boolean success;

}
