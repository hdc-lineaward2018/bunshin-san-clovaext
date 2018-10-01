package com.example.clova_sample.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    String name;
    String lineuserid;
    Integer currentSectionSequence = 0;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getlineuserid() {
        return lineuserid;
    }

    public void setlineuserid(String lineUserID) {
        this.lineuserid = lineUserID;
    }

    public Integer getCurrentSectionSequence() {
        return currentSectionSequence;
    }

    public void setCurrentSectionSequence(Integer currentSectionSequence) {
        this.currentSectionSequence = currentSectionSequence;
    }
}
