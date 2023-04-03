package com.springboot3.boilerplate.app.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ListResponse {
    private List<?> result;

    private Paging paging;

    public ListResponse(List<?> result) {
        this.result = result;

    }

    public ListResponse(List<?> result, Paging paging) {
        this.result = result;
        this.paging = paging;
    }
}