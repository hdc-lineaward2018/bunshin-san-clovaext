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
    List<Talk> talklist = new ArrayList<Talk>();

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

    public List<Talk> gettalklist() {
        return talklist;
    }

    public void settalklist(List<Talk> talklist) {
        this.talklist = talklist;
    }

}
