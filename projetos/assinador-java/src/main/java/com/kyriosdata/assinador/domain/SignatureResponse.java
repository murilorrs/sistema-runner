package com.kyriosdata.assinador.domain;

/**
 * DTO de resposta para operações de assinatura e validação.
 */
public class SignatureResponse {

    /** Assinatura gerada (pode ser null em caso de erro). */
    private String signature;

    /** Indica se a operação foi bem-sucedida ou a assinatura é válida. */
    private boolean valid;

    /** Mensagem descritiva do resultado ou do erro. */
    private String message;

    public SignatureResponse() {}

    public SignatureResponse(String signature, boolean valid, String message) {
        this.signature = signature;
        this.valid = valid;
        this.message = message;
    }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
