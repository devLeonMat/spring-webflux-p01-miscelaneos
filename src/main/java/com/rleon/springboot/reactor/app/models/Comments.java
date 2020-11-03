package com.rleon.springboot.reactor.app.models;

import lombok.ToString;

import java.util.ArrayList;
import java.util.List;


@ToString
public class Comments {

    private List<String> coments;

    public Comments() {
        this.coments = new ArrayList<>();
    }

    public void addComent(String coment) {
        this.coments.add(coment);
    }
}
