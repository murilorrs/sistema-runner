package com.kyriosdata.assinador;

import com.kyriosdata.assinador.domain.SignRequest;
import com.kyriosdata.assinador.domain.SignatureResponse;
import com.kyriosdata.assinador.domain.ValidateRequest;
import java.util.Base64;

/**
 * Implementação simulada do serviço de assinatura digital.
 *
 * <p>NÃO realiza criptografia real. Valida os parâmetros de entrada
 * e retorna respostas pré-construídas para fins de teste e integração.</p>
 */
public class FakeSignatureService implements SignatureService {

    public static final String FAKE_SIGNATURE = "MOCKED_SIGNATURE_BASE64_==";

    @Override
    public SignatureResponse sign(SignRequest request) {
        if (request == null) {
            return erro("Requisição ausente");
        }

        // Validar content: obrigatório e deve ser Base64 válido
        String content = request.getContent();
        if (content == null || content.isBlank()) {
            return erro("Parâmetro 'content' é obrigatório");
        }
        if (!isBase64(content)) {
            return erro("Parâmetro 'content' deve estar em formato Base64 válido");
        }

        // token é opcional — se informado, deve ser um caminho não vazio
        String token = request.getToken();
        if (token != null && token.isBlank()) {
            return erro("Parâmetro 'token', quando informado, não pode ser vazio");
        }

        return new SignatureResponse(FAKE_SIGNATURE, true, "Assinatura criada com sucesso");
    }

    @Override
    public SignatureResponse validate(ValidateRequest request) {
        if (request == null) {
            return erro("Requisição ausente");
        }

        // Validar content: obrigatório e Base64
        String content = request.getContent();
        if (content == null || content.isBlank()) {
            return erro("Parâmetro 'content' é obrigatório");
        }
        if (!isBase64(content)) {
            return erro("Parâmetro 'content' deve estar em formato Base64 válido");
        }

        // Validar signature: obrigatória e não vazia
        String signature = request.getSignature();
        if (signature == null || signature.isBlank()) {
            return erro("Parâmetro 'signature' é obrigatório");
        }

        boolean isValid = FAKE_SIGNATURE.equals(signature);
        String message = isValid ? "Assinatura é válida" : "Assinatura é inválida";
        return new SignatureResponse(signature, isValid, message);
    }

    // --- utilitário ---

    private static SignatureResponse erro(String message) {
        return new SignatureResponse(null, false, message);
    }

    /**
     * Verifica se a string é um Base64 válido (incluindo Base64 URL-safe).
     * Aceita padding com '=' ou sem padding (Base64 compacto).
     */
    static boolean isBase64(String value) {
        String normalized = value.replaceAll("\\s", "");
        try {
            Base64.getDecoder().decode(normalized);
            return true;
        } catch (IllegalArgumentException e) {
            try {
                Base64.getUrlDecoder().decode(normalized);
                return true;
            } catch (IllegalArgumentException e2) {
                return false;
            }
        }
    }
}
