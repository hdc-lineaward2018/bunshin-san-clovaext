package com.example.clova_sample.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    String name;
    String lineuserid;
    String currentbookid;
    Integer currentsectionsequence = 0;

    public String getname() {
        return name;
    }

    public void setname(String name) {
        this.name = name;
    }

    public String getlineuserid() {
        return lineuserid;
    }

    public void setlineuserid(String lineUserID) {
        this.lineuserid = lineUserID;
    }

    public Integer getcurrentsectionsequence() {
        return currentsectionsequence;
    }

    public void setcurrentsectionsequence(Integer currentSectionSequence) {
        this.currentsectionsequence = currentSectionSequence;
    }

    public String getcurrentbookid() {
        return currentbookid;
    }

    public void setcurrentbookid(String currentbookid) {
        this.currentbookid = currentbookid;
    }

}
