package com.example.petmaps;

public class Mensagem { // Armazenar os dados das mensagens

    // Atibutos
    private String texto, idEnvio, idRecebido;
    private long tempoEnvio;

    // Getters e Setters
    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(String idEnvio) {
        this.idEnvio = idEnvio;
    }

    public String getIdRecebido() {
        return idRecebido;
    }

    public void setIdRecebido(String idRecebido) {
        this.idRecebido = idRecebido;
    }

    public long getTempoEnvio() {
        return tempoEnvio;
    }

    public void setTempoEnvio(long tempoEnvio) {
        this.tempoEnvio = tempoEnvio;
    }
}
