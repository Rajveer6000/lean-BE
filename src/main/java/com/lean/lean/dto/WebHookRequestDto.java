package com.lean.lean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebHookRequestDto {
    private  String type;
    private  String message;
    private  Object payload;
    private  String event_id;
    private  String timestamp;
}
