package com.kyriosdata.assinador;

import com.kyriosdata.assinador.domain.SignRequest;
import com.kyriosdata.assinador.domain.SignatureResponse;
import com.kyriosdata.assinador.domain.ValidateRequest;

/**
 * Ponto de entrada do assinador.jar.
 *
 * <p>Modos de uso:</p>
 * <pre>
 *   # Modo CLI — criar assinatura:
 *   java -jar assinador.jar sign --content &lt;base64&gt; [--token &lt;caminho&gt;]
 *
 *   # Modo CLI — validar assinatura:
 *   java -jar assinador.jar validate --content &lt;base64&gt; --signature &lt;str&gt;
 *
 *   # Modo servidor HTTP:
 *   java -jar assinador.jar --server [--port 8080] [--timeout 10]
 * </pre>
 *
 * <p>Saída no modo CLI: JSON em stdout. Código de saída 0 = sucesso, 1 = erro.</p>
 */
public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println(ajuda());
            System.exit(1);
        }

        // Modo servidor
        if (ArgsParser.hasFlag(args, "--server")) {
            int port = ArgsParser.intArg(args, "--port", 8080);
            int timeout = ArgsParser.intArg(args, "--timeout", 0);
            new AssinadorServer(port, timeout).start();
            // bloqueia até o servidor ser encerrado
            Thread.currentThread().join();
            return;
        }

        String command = args[0];
        SignatureService service = new FakeSignatureService();

        switch (command) {
            case "sign" -> {
                String content = ArgsParser.stringArg(args, "--content");
                String token = ArgsParser.stringArg(args, "--token");

                if (content == null) {
                    System.err.println("{\"valid\":false,\"message\":\"Parâmetro --content é obrigatório\"}");
                    System.exit(1);
                }

                SignRequest req = new SignRequest(content, token);
                SignatureResponse resp = service.sign(req);
                System.out.println(JsonUtil.toJson(resp));
                System.exit(resp.isValid() ? 0 : 1);
            }
            case "validate" -> {
                String content = ArgsParser.stringArg(args, "--content");
                String signature = ArgsParser.stringArg(args, "--signature");

                if (content == null) {
                    System.err.println("{\"valid\":false,\"message\":\"Parâmetro --content é obrigatório\"}");
                    System.exit(1);
                }
                if (signature == null) {
                    System.err.println("{\"valid\":false,\"message\":\"Parâmetro --signature é obrigatório\"}");
                    System.exit(1);
                }

                ValidateRequest req = new ValidateRequest(content, signature);
                SignatureResponse resp = service.validate(req);
                System.out.println(JsonUtil.toJson(resp));
                System.exit(0);
            }
            default -> {
                System.err.println("Comando desconhecido: " + command);
                System.err.println(ajuda());
                System.exit(1);
            }
        }
    }


    private static String ajuda() {
        return """
                Uso:
                  java -jar assinador.jar sign --content <base64> [--token <caminho>]
                  java -jar assinador.jar validate --content <base64> --signature <str>
                  java -jar assinador.jar --server [--port 8080] [--timeout <minutos>]
                """;
    }
}
