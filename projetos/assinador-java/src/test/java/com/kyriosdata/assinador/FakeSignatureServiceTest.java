package com.kyriosdata.assinador;

import com.kyriosdata.assinador.domain.SignRequest;
import com.kyriosdata.assinador.domain.SignatureResponse;
import com.kyriosdata.assinador.domain.ValidateRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FakeSignatureServiceTest {

    private final SignatureService service = new FakeSignatureService();

    // ===== sign =====

    @Test
    void sign_conteudoValido_retornaAssinaturaSimulada() {
        SignRequest req = new SignRequest();
        req.setContent("dGVzdGU="); // "teste" em Base64

        SignatureResponse resp = service.sign(req);

        assertNotNull(resp);
        assertTrue(resp.isValid());
        assertEquals(FakeSignatureService.FAKE_SIGNATURE, resp.getSignature());
        assertEquals("Assinatura criada com sucesso", resp.getMessage());
    }

    @Test
    void sign_contentNulo_retornaErro() {
        SignRequest req = new SignRequest();
        req.setContent(null);

        SignatureResponse resp = service.sign(req);

        assertFalse(resp.isValid());
        assertNull(resp.getSignature());
        assertTrue(resp.getMessage().contains("content"));
    }

    @Test
    void sign_contentVazio_retornaErro() {
        SignRequest req = new SignRequest();
        req.setContent("   ");

        SignatureResponse resp = service.sign(req);

        assertFalse(resp.isValid());
    }

    @Test
    void sign_contentNaoBase64_retornaErro() {
        SignRequest req = new SignRequest();
        req.setContent("isso não é base64!!@#$");

        SignatureResponse resp = service.sign(req);

        assertFalse(resp.isValid());
        assertTrue(resp.getMessage().contains("Base64"));
    }

    @Test
    void sign_requisicaoNula_retornaErro() {
        SignatureResponse resp = service.sign(null);

        assertFalse(resp.isValid());
    }

    @Test
    void sign_tokenInformado_sucesso() {
        SignRequest req = new SignRequest("dGVzdGU=", "/caminho/token.p11");

        SignatureResponse resp = service.sign(req);

        assertTrue(resp.isValid());
    }

    @Test
    void sign_tokenVazio_retornaErro() {
        SignRequest req = new SignRequest("dGVzdGU=", "   ");

        SignatureResponse resp = service.sign(req);

        assertFalse(resp.isValid());
        assertTrue(resp.getMessage().contains("token"));
    }

    // ===== validate =====

    @Test
    void validate_assinaturaCorreta_retornaValido() {
        ValidateRequest req = new ValidateRequest("dGVzdGU=", FakeSignatureService.FAKE_SIGNATURE);

        SignatureResponse resp = service.validate(req);

        assertTrue(resp.isValid());
        assertEquals("Assinatura é válida", resp.getMessage());
    }

    @Test
    void validate_assinaturaErrada_retornaInvalido() {
        ValidateRequest req = new ValidateRequest("dGVzdGU=", "ASSINATURA_ERRADA_==");

        SignatureResponse resp = service.validate(req);

        assertFalse(resp.isValid());
        assertEquals("Assinatura é inválida", resp.getMessage());
    }

    @Test
    void validate_contentNulo_retornaErro() {
        ValidateRequest req = new ValidateRequest(null, FakeSignatureService.FAKE_SIGNATURE);

        SignatureResponse resp = service.validate(req);

        assertFalse(resp.isValid());
        assertTrue(resp.getMessage().contains("content"));
    }

    @Test
    void validate_signatureNula_retornaErro() {
        ValidateRequest req = new ValidateRequest("dGVzdGU=", null);

        SignatureResponse resp = service.validate(req);

        assertFalse(resp.isValid());
        assertTrue(resp.getMessage().contains("signature"));
    }

    @Test
    void validate_contentNaoBase64_retornaErro() {
        ValidateRequest req = new ValidateRequest("não é base64", "alguma-sig");

        SignatureResponse resp = service.validate(req);

        assertFalse(resp.isValid());
        assertTrue(resp.getMessage().contains("Base64"));
    }

    // ===== isBase64 (package-private) =====

    @Test
    void isBase64_stringValidaSimples() {
        assertTrue(FakeSignatureService.isBase64("dGVzdGU="));
    }

    @Test
    void isBase64_base64ValidoSemPadding() {
        assertTrue(FakeSignatureService.isBase64("dGVzdGU"));
    }

    @Test
    void isBase64_base64UrlSafe() {
        assertTrue(FakeSignatureService.isBase64("dGVzdGU-_"));
    }

    @Test
    void isBase64_comEspacos() {
        assertTrue(FakeSignatureService.isBase64("dGVz dGU="));
        assertTrue(FakeSignatureService.isBase64("dGVz\ndGU="));
    }

    @Test
    void isBase64_base64MuitoLongo() {
        String base64 = "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXpBQkNERUZHSElKS0xNTk9QUVJTVFVWV1hZWjAxMjM0NTY3ODk=";
        assertTrue(FakeSignatureService.isBase64(base64));
    }

    @Test
    void isBase64_invalido_caractereInvalido() {
        assertFalse(FakeSignatureService.isBase64("não_é_base64!@#$"));
    }

    @Test
    void isBase64_invalido_espacosInvalidos() {
        assertFalse(FakeSignatureService.isBase64("dGVzdGU=="));
    }

    @Test
    void isBase64_stringVazia() {
        // Tecnicamente vazio é base64 válido (decodifica para vazio)
        assertTrue(FakeSignatureService.isBase64(""));
    }

    @Test
    void isBase64_invalidoComCaracteresEspeciais() {
        assertFalse(FakeSignatureService.isBase64("dGVzdGU=@#$"));
    }
}
