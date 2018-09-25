package com.example.clova_sample.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.json.JSONObject;


@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ResponseDTO {

    JSONObject param;
    JSONObject result;
    Boolean success;
}
