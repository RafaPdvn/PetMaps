package com.example.petmaps;

public class Usuario { // Armazenar os dados que precisamos do usuário

    // Atibutos
    private String uuid, nome, foto, email, senha, estado, cidade, ong;

    // Construtor
    public Usuario() {
    }

    public Usuario(String uuid, String nome, String foto, String email, String senha, String estado, String cidade, String ong) {
        this.uuid = uuid;
        this.nome = nome;
        this.foto = foto;
        this.email = email;
        this.senha = senha;
        this.estado = estado;
        this.cidade = cidade;
        this.ong = ong;
    }

    // Getters
    public String getUuid() {
        return uuid;
    }

    public String getNome() {
        return nome;
    }

    public String getfoto() {
        return foto;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public String getEstado() {
        return estado;
    }

    public String getCidade() {
        return cidade;
    }

    public String getOng() {
        return ong;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public void setOng(String ong) {
        this.ong = ong;
    }
}
