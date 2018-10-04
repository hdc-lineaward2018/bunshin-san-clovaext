package com.example.clova_sample.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Talk {

    String S;

    public String getS() {
        return S;
    }

    public void sets(String text) {
        this.S = text;
    }
}
