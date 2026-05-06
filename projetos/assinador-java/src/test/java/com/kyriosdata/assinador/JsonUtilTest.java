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
}
