package com.kyriosdata.assinador;

import com.kyriosdata.assinador.domain.SignRequest;
import com.kyriosdata.assinador.domain.SignatureResponse;
import com.kyriosdata.assinador.domain.ValidateRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DomainDtoTest {

    @Test
    void signRequest_construtorCompleto() {
        SignRequest req = new SignRequest("dGVzdGU=", "/token.p11");

        assertEquals("dGVzdGU=", req.getContent());
        assertEquals("/token.p11", req.getToken());
    }

    @Test
    void signRequest_construtorVazioESetters() {
        SignRequest req = new SignRequest();
        req.setContent("YWJj");
        req.setToken(null);

        assertEquals("YWJj", req.getContent());
        assertNull(req.getToken());
    }

    @Test
    void validateRequest_construtorCompleto() {
        ValidateRequest req = new ValidateRequest("dGVzdGU=", "sig123");

        assertEquals("dGVzdGU=", req.getContent());
        assertEquals("sig123", req.getSignature());
    }

    @Test
    void validateRequest_settersAlteramValores() {
        ValidateRequest req = new ValidateRequest();
        req.setContent("dGVzdGU=");
        req.setSignature("outra-assinatura");

        assertEquals("dGVzdGU=", req.getContent());
        assertEquals("outra-assinatura", req.getSignature());
    }

    @Test
    void signatureResponse_construtorCompleto() {
        SignatureResponse resp = new SignatureResponse("SIG==", true, "OK");

        assertEquals("SIG==", resp.getSignature());
        assertTrue(resp.isValid());
        assertEquals("OK", resp.getMessage());
    }

    @Test
    void signatureResponse_settersAlteramValores() {
        SignatureResponse resp = new SignatureResponse();
        resp.setSignature(null);
        resp.setValid(false);
        resp.setMessage("Erro de validação");

        assertNull(resp.getSignature());
        assertFalse(resp.isValid());
        assertEquals("Erro de validação", resp.getMessage());
    }
}
