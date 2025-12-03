package com.example.stockmanager.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class PageDto {

    private Integer page;

    private Integer size;
}
