package com.kyriosdata.assinador;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArgsParserTest {

    @Test
    void stringArg_retornaValorQuandoFlagExiste() {
        String[] args = {"sign", "--content", "dGVzdGU=", "--token", "/path/token.p11"};

        assertEquals("dGVzdGU=", ArgsParser.stringArg(args, "--content"));
        assertEquals("/path/token.p11", ArgsParser.stringArg(args, "--token"));
    }

    @Test
    void stringArg_retornaNullQuandoFlagNaoExiste() {
        String[] args = {"sign", "--content", "dGVzdGU="};

        assertNull(ArgsParser.stringArg(args, "--token"));
        assertNull(ArgsParser.stringArg(args, "--inexistente"));
    }

    @Test
    void stringArg_retornaNullQuandoFlagEstaNoFinalSemValor() {
        String[] args = {"sign", "--content"};

        assertNull(ArgsParser.stringArg(args, "--content"));
    }

    @Test
    void hasFlag_detectaFlagPresenteEAusente() {
        String[] args = {"--server", "--port", "8080"};

        assertTrue(ArgsParser.hasFlag(args, "--server"));
        assertFalse(ArgsParser.hasFlag(args, "--timeout"));
    }

    @Test
    void intArg_retornaValorParseado() {
        String[] args = {"--server", "--port", "9090", "--timeout", "30"};

        assertEquals(9090, ArgsParser.intArg(args, "--port", 8080));
        assertEquals(30, ArgsParser.intArg(args, "--timeout", 0));
    }

    @Test
    void intArg_retornaDefaultQuandoFlagAusente() {
        String[] args = {"sign", "--content", "dGVzdGU="};

        assertEquals(8080, ArgsParser.intArg(args, "--port", 8080));
        assertEquals(0, ArgsParser.intArg(args, "--timeout", 0));
    }
}
