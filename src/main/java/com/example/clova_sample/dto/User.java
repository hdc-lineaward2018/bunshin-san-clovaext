package com.example.clova_sample.dto;

public class User {
    String Name;
    String lineUserID;
    Integer currentSectionSequence = 1;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getLineUserID() {
        return lineUserID;
    }

    public void setLineUserID(String lineUserID) {
        this.lineUserID = lineUserID;
    }

    public Integer getCurrentSectionSequence() {
        return currentSectionSequence;
    }

    public void setCurrentSectionSequence(Integer currentSectionSequence) {
        this.currentSectionSequence = currentSectionSequence;
    }
}
