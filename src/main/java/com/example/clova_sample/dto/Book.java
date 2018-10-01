package com.example.clova_sample.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Book {

    String id;
    String Name;

    List<Talk> talkList = new ArrayList<Talk>();

    public Book(){
        Talk talk1 = new Talk();
        talk1.setText("テキストひとつめ！おぐらさん！");
        Talk talk2 = new Talk();
        talk2.setText("テキストふたつめ！おぐらちゃん！");
        talkList.add(talk1);
        talkList.add(talk2);
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public List<Talk> getTalkList() {
        return talkList;
    }

    public void setTalkList(List<Talk> talkList) {
        this.talkList = talkList;
    }
}
