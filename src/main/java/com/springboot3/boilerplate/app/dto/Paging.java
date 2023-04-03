package com.springboot3.boilerplate.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Paging {
    private int page;

    private int pageSize;

    private Long totalCount;
}