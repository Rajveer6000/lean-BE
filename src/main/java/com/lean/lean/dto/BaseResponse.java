package com.lean.lean.dto;

import lombok.Data;

import java.util.Map;

@Data
public class BaseResponse {
    private Object data;
    private Result result;
    private Map<String, String> errorFields;
}
