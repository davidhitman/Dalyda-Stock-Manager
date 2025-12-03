package com.example.stockmanager.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GenericResponse<T> {
    private String message;
    private T data;
}
