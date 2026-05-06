package com.kyriosdata.assinador.domain;

/**
 * DTO para requisição de criação de assinatura digital.
 */
public class SignRequest {

    /** Conteúdo original a ser assinado (em Base64). */
    private String content;

    /** Caminho para o token/smart card PKCS#11 (opcional). */
    private String token;

    public SignRequest() {}

    public SignRequest(String content, String token) {
        this.content = content;
        this.token = token;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
