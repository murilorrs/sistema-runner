package com.kyriosdata.assinador;

import com.kyriosdata.assinador.domain.SignRequest;
import com.kyriosdata.assinador.domain.SignatureResponse;
import com.kyriosdata.assinador.domain.ValidateRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilTest {

    @Test
    void toJson_responseValida() {
        SignatureResponse r = new SignatureResponse("SIG123==", true, "OK");
        String json = JsonUtil.toJson(r);
        assertTrue(json.contains("\"signature\":\"SIG123==\""));
        assertTrue(json.contains("\"valid\":true"));
        assertTrue(json.contains("\"message\":\"OK\""));
    }

    @Test
    void toJson_signatureNula() {
        SignatureResponse r = new SignatureResponse(null, false, "Erro");
        String json = JsonUtil.toJson(r);
        assertTrue(json.contains("\"signature\":null"));
        assertTrue(json.contains("\"valid\":false"));
    }

    @Test
    void toSignRequest_extraiCampos() {
        String json = "{\"content\":\"dGVzdGU=\",\"token\":\"/caminho/token\"}";
        SignRequest req = JsonUtil.toSignRequest(json);
        assertEquals("dGVzdGU=", req.getContent());
        assertEquals("/caminho/token", req.getToken());
    }

    @Test
    void toSignRequest_tokenAusente() {
        String json = "{\"content\":\"dGVzdGU=\"}";
        SignRequest req = JsonUtil.toSignRequest(json);
        assertEquals("dGVzdGU=", req.getContent());
        assertNull(req.getToken());
    }

    @Test
    void toValidateRequest_extraiCampos() {
        String json = "{\"content\":\"dGVzdGU=\",\"signature\":\"MOCKED_SIGNATURE_BASE64_==\"}";
        ValidateRequest req = JsonUtil.toValidateRequest(json);
        assertEquals("dGVzdGU=", req.getContent());
        assertEquals("MOCKED_SIGNATURE_BASE64_==", req.getSignature());
    }

    // ===== extractString (package-private) =====

    @Test
    void extractString_stringValida() {
        String json = "{\"key\":\"value\"}";
        String result = JsonUtil.extractString(json, "key");
        assertEquals("value", result);
    }

    @Test
    void extractString_stringComEspacos() {
        String json = "{\"key\" : \"valor com espaços\"}";
        String result = JsonUtil.extractString(json, "key");
        assertEquals("valor com espaços", result);
    }

    @Test
    void extractString_stringComAspasEscapadas() {
        String json = "{\"key\":\"valor \\\"com\\\" aspas\"}";
        String result = JsonUtil.extractString(json, "key");
        assertEquals("valor \"com\" aspas", result);
    }

    @Test
    void extractString_stringComBarrasEscapadas() {
        String json = "{\"key\":\"caminho\\\\arquivo\"}";
        String result = JsonUtil.extractString(json, "key");
        assertEquals("caminho\\arquivo", result);
    }

    @Test
    void extractString_stringComCaracteresEspeciais() {
        String json = "{\"key\":\"valor\\ncom\\nnewlines\"}";
        String result = JsonUtil.extractString(json, "key");
        assertEquals("valor\ncom\nnewlines", result);
    }

    @Test
    void extractString_valuoNull() {
        String json = "{\"key\":null}";
        String result = JsonUtil.extractString(json, "key");
        assertNull(result);
    }

    @Test
    void extractString_chaveNaoEncontrada() {
        String json = "{\"outra\":\"value\"}";
        String result = JsonUtil.extractString(json, "key");
        assertNull(result);
    }

    @Test
    void extractString_jsonVazio() {
        String json = "{}";
        String result = JsonUtil.extractString(json, "key");
        assertNull(result);
    }

    @Test
    void extractString_stringVazia() {
        String json = "{\"key\":\"\"}";
        String result = JsonUtil.extractString(json, "key");
        assertEquals("", result);
    }

    @Test
    void toJson_mensagemNula() {
        SignatureResponse r = new SignatureResponse("SIG", true, null);
        assertTrue(JsonUtil.toJson(r).contains("\"message\":null"));
    }

    @Test
    void toJson_escapaAspasNaMensagem() {
        SignatureResponse r = new SignatureResponse("SIG", false, "Erro \"detalhe\"");
        String json = JsonUtil.toJson(r);

        assertTrue(json.contains("\\\"detalhe\\\""));
    }

    @Test
    void toValidateRequest_signatureAusente() {
        String json = "{\"content\":\"dGVzdGU=\"}";
        ValidateRequest req = JsonUtil.toValidateRequest(json);

        assertEquals("dGVzdGU=", req.getContent());
        assertNull(req.getSignature());
    }

    @Test
    void toSignRequest_e_toValidateRequest_preservamConteudo() {
        String signJson = "{\"content\":\"dGVzdGU=\",\"token\":\"/caminho\"}";
        String validateJson = "{\"content\":\"dGVzdGU=\",\"signature\":\"SIG==\"}";

        assertEquals("dGVzdGU=", JsonUtil.toSignRequest(signJson).getContent());
        assertEquals("dGVzdGU=", JsonUtil.toValidateRequest(validateJson).getContent());
    }
}
