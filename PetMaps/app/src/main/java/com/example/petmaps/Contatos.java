package com.example.petmaps;

public class Contatos { // Armazena os dados das últimas mensagens

    // Atributos
    private String uid, nome, ultimaMensagem, foto;
    private long horaEnviada;

    // Getters e Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getUltimaMensagem() {
        return ultimaMensagem;
    }

    public void setUltimaMensagem(String ultimaMensagem) {
        this.ultimaMensagem = ultimaMensagem;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public long getHoraEnviada() {
        return horaEnviada;
    }

    public void setHoraEnviada(long horaEnviada) {
        this.horaEnviada = horaEnviada;
    }
}
