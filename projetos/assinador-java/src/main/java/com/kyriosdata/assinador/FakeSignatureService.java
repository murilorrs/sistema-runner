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
     * Aceita padding com '=' ou sem padding (RFC 4648), espaços em branco Unicode
     * entre blocos e alfabeto URL-safe ({@code -} / {@code _}).
     */
    static boolean isBase64(String value) {
        if (value == null) {
            return false;
        }
        String normalized = stripWhitespace(value);
        if (normalized.isEmpty()) {
            return true;
        }
        if (decodeStandardOrUrl(normalized)) {
            return true;
        }
        // Sem '=' no fim: tentar padding explícito conforme comprimento % 4
        if (!normalized.endsWith("=")) {
            int mod = normalized.length() % 4;
            if (mod == 2 && decodeStandardOrUrl(normalized + "==")) {
                return true;
            }
            if (mod == 3 && decodeStandardOrUrl(normalized + "=")) {
                return true;
            }
        }
        return false;
    }

    private static String stripWhitespace(String value) {
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!Character.isWhitespace(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static boolean decodeStandardOrUrl(String s) {
        try {
            Base64.getDecoder().decode(s);
            return true;
        } catch (IllegalArgumentException e) {
            try {
                Base64.getUrlDecoder().decode(s);
                return true;
            } catch (IllegalArgumentException e2) {
                return false;
            }
        }
    }
}
