package com.example.clova_sample.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Book {

    String lineuserid;
    String bookid;
    String name;
    List<String> talklist = new ArrayList<>();

    public String getlineuserid() {
        return lineuserid;
    }

    public void setlineuserid(String lineuserid) {
        this.lineuserid = lineuserid;
    }

    public String getbookid() {
        return bookid;
    }

    public void setbookid(String bookid) {
        this.bookid = bookid;
    }

    public String getname() {
        return name;
    }

    public void setname(String name) {
        this.name = name;
    }

    public List<String> gettalklist() {
        return talklist;
    }

    public void settalklist(List<String> talklist) {
        this.talklist = talklist;
    }

}
