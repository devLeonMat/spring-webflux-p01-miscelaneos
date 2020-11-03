package com.rleon.springboot.reactor.app.models;

import lombok.Data;

@Data
public class UserComment {


    private Usuario usuario;
    private Comments comments;


    public UserComment(Usuario usuario, Comments comments) {
        this.usuario = usuario;
        this.comments = comments;
    }
}
