package com.kyriosdata.assinador;

import com.kyriosdata.assinador.domain.SignRequest;
import com.kyriosdata.assinador.domain.SignatureResponse;
import com.kyriosdata.assinador.domain.ValidateRequest;

/**
 * Utilitário simples de serialização/desserialização JSON sem dependências externas.
 * Cobre apenas as estruturas usadas neste projeto.
 */
public class JsonUtil {

    private JsonUtil() {}

    /** Serializa um SignatureResponse para JSON. */
    public static String toJson(SignatureResponse r) {
        String sig = r.getSignature() == null ? "null" : "\"" + escape(r.getSignature()) + "\"";
        String msg = r.getMessage() == null ? "null" : "\"" + escape(r.getMessage()) + "\"";
        return String.format("{\"signature\":%s,\"valid\":%b,\"message\":%s}", sig, r.isValid(), msg);
    }

    /** Desserializa JSON para SignRequest. */
    public static SignRequest toSignRequest(String json) {
        SignRequest req = new SignRequest();
        req.setContent(extractString(json, "content"));
        req.setToken(extractString(json, "token"));
        return req;
    }

    /** Desserializa JSON para ValidateRequest. */
    public static ValidateRequest toValidateRequest(String json) {
        ValidateRequest req = new ValidateRequest();
        req.setContent(extractString(json, "content"));
        req.setSignature(extractString(json, "signature"));
        return req;
    }

    /**
     * Extrai o valor de uma chave string de um JSON simples (sem aninhamento).
     * Retorna null se a chave não existir.
     */
    static String extractString(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIdx = json.indexOf(search);
        if (keyIdx < 0) return null;

        int colon = json.indexOf(':', keyIdx + search.length());
        if (colon < 0) return null;

        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;

        if (start >= json.length()) return null;

        if (json.charAt(start) == '"') {
            // String value
            StringBuilder sb = new StringBuilder();
            int i = start + 1;
            while (i < json.length()) {
                char c = json.charAt(i);
                if (c == '\\' && i + 1 < json.length()) {
                    char next = json.charAt(i + 1);
                    switch (next) {
                        case '"' -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        default -> { sb.append('\\'); sb.append(next); }
                    }
                    i += 2;
                } else if (c == '"') {
                    break;
                } else {
                    sb.append(c);
                    i++;
                }
            }
            return sb.toString();
        } else if (json.startsWith("null", start)) {
            return null;
        }
        return null;
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
