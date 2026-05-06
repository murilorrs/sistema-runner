package com.kyriosdata.assinador.domain;

/**
 * DTO para requisição de validação de assinatura digital.
 */
public class ValidateRequest {

    /** Conteúdo original que foi assinado (em Base64). */
    private String content;

    /** Assinatura digital a ser validada. */
    private String signature;

    public ValidateRequest() {}

    public ValidateRequest(String content, String signature) {
        this.content = content;
        this.signature = signature;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
}
