package com.kyriosdata.assinador;

import com.kyriosdata.assinador.domain.SignatureResponse;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Servidor HTTP embutido para o assinador.jar.
 *
 * <p>Expõe os endpoints:
 * <ul>
 *   <li>POST /sign — cria assinatura simulada</li>
 *   <li>POST /validate — valida assinatura simulada</li>
 *   <li>GET  /health — health check</li>
 *   <li>POST /shutdown — encerra o servidor</li>
 * </ul>
 * </p>
 */
public class AssinadorServer {

    private final int port;
    private final int timeoutMinutes; // 0 = sem timeout
    private final SignatureService service = new FakeSignatureService();
    private final AtomicLong lastActivity = new AtomicLong(System.currentTimeMillis());
    private HttpServer httpServer;

    public AssinadorServer(int port, int timeoutMinutes) {
        this.port = port;
        this.timeoutMinutes = timeoutMinutes;
    }

    public void start() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.createContext("/sign", new SignHandler());
        httpServer.createContext("/validate", new ValidateHandler());
        httpServer.createContext("/health", new HealthHandler());
        httpServer.createContext("/shutdown", new ShutdownHandler());
        httpServer.setExecutor(Executors.newFixedThreadPool(4));
        httpServer.start();

        System.out.println("Assinador servidor iniciado na porta " + port);

        if (timeoutMinutes > 0) {
            agendarTimeout();
        }
    }

    private void agendarTimeout() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "timeout-checker");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            long idleMs = System.currentTimeMillis() - lastActivity.get();
            if (idleMs >= (long) timeoutMinutes * 60 * 1000) {
                System.out.println("Timeout de inatividade atingido (" + timeoutMinutes + " min). Encerrando.");
                httpServer.stop(1);
                System.exit(0);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    private void registrarAtividade() {
        lastActivity.set(System.currentTimeMillis());
    }

    private void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String readBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    // --- Handlers ---

    private class SignHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            registrarAtividade();
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                sendJson(ex, 405, "{\"message\":\"Método não permitido\"}");
                return;
            }
            String body = readBody(ex);
            SignatureResponse resp = service.sign(JsonUtil.toSignRequest(body));
            int status = resp.isValid() ? 200 : 400;
            sendJson(ex, status, JsonUtil.toJson(resp));
        }
    }

    private class ValidateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            registrarAtividade();
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                sendJson(ex, 405, "{\"message\":\"Método não permitido\"}");
                return;
            }
            String body = readBody(ex);
            SignatureResponse resp = service.validate(JsonUtil.toValidateRequest(body));
            sendJson(ex, 200, JsonUtil.toJson(resp));
        }
    }

    private class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            sendJson(ex, 200, "{\"status\":\"ok\",\"port\":" + port + "}");
        }
    }

    private class ShutdownHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                sendJson(ex, 405, "{\"message\":\"Método não permitido\"}");
                return;
            }
            sendJson(ex, 200, "{\"message\":\"Servidor encerrando\"}");
            httpServer.stop(1);
            System.exit(0);
        }
    }
}
