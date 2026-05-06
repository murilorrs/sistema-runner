package runner

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"os/exec"
	"strconv"
	"strings"
	"time"

	"github.com/kyriosdata/assinatura/internal/jdk"
)

const defaultPort = 8080

// IsServerRunning verifica se há instância ativa do assinador.jar na porta dada.
func IsServerRunning(port int) bool {
	url := fmt.Sprintf("http://localhost:%d/health", port)
	client := &http.Client{Timeout: 2 * time.Second}
	resp, err := client.Get(url) //nolint:gosec
	if err != nil {
		return false
	}
	defer resp.Body.Close()
	return resp.StatusCode == http.StatusOK
}

// StartServer inicia o assinador.jar em modo servidor em background.
// Salva o PID em ~/.hubsaude/assinador-<port>.pid.
func StartServer(port, timeoutMinutes int) error {
	if IsServerRunning(port) {
		return fmt.Errorf("já existe uma instância do assinador rodando na porta %d", port)
	}

	java, err := jdk.FindJava()
	if err != nil {
		return err
	}
	jarPath, err := FindJar()
	if err != nil {
		return err
	}

	args := []string{
		"-jar", jarPath,
		"--server",
		"--port", strconv.Itoa(port),
	}
	if timeoutMinutes > 0 {
		args = append(args, "--timeout", strconv.Itoa(timeoutMinutes))
	}

	cmd := exec.Command(java, args...)
	cmd.Stdout = nil
	cmd.Stderr = nil

	if err := cmd.Start(); err != nil {
		return fmt.Errorf("falha ao iniciar servidor: %w", err)
	}

	// Aguardar o servidor ficar disponível (até 15s)
	for i := 0; i < 30; i++ {
		time.Sleep(500 * time.Millisecond)
		if IsServerRunning(port) {
			_ = SavePID(cmd.Process.Pid, port)
			return nil
		}
	}

	cmd.Process.Kill() //nolint:errcheck
	return fmt.Errorf("servidor não respondeu após 15 segundos")
}

// StopServer encerra o assinador.jar rodando na porta dada.
// Tenta primeiro via endpoint /shutdown, depois via sinal de processo.
func StopServer(port int) error {
	// Tentar graceful shutdown via HTTP
	url := fmt.Sprintf("http://localhost:%d/shutdown", port)
	client := &http.Client{Timeout: 3 * time.Second}
	resp, err := client.Post(url, "application/json", nil) //nolint:gosec
	if err == nil {
		defer resp.Body.Close()
		RemovePID(port)
		return nil
	}

	// Fallback: matar pelo PID
	pid := LoadPID(port)
	if pid <= 0 {
		return fmt.Errorf("nenhum processo registrado na porta %d", port)
	}

	proc, err := os.FindProcess(pid)
	if err != nil {
		RemovePID(port)
		return fmt.Errorf("processo %d não encontrado: %w", pid, err)
	}

	if err := proc.Kill(); err != nil {
		return fmt.Errorf("falha ao encerrar processo %d: %w", pid, err)
	}

	RemovePID(port)
	return nil
}

// ServerSign envia requisição POST /sign ao servidor em modo HTTP.
func ServerSign(port int, content, token string) (*SignatureResponse, error) {
	body := buildSignJSON(content, token)
	return postJSON(port, "/sign", body)
}

// ServerValidate envia requisição POST /validate ao servidor em modo HTTP.
func ServerValidate(port int, content, signature string) (*SignatureResponse, error) {
	body := fmt.Sprintf(`{"content":%s,"signature":%s}`, jsonStr(content), jsonStr(signature))
	return postJSON(port, "/validate", body)
}

func postJSON(port int, path, body string) (*SignatureResponse, error) {
	url := fmt.Sprintf("http://localhost:%d%s", port, path)
	client := &http.Client{Timeout: 30 * time.Second}
	resp, err := client.Post(url, "application/json", strings.NewReader(body)) //nolint:gosec
	if err != nil {
		return nil, fmt.Errorf("erro ao conectar ao servidor na porta %d: %w", port, err)
	}
	defer resp.Body.Close()

	data, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}

	var result SignatureResponse
	if err := json.Unmarshal(bytes.TrimSpace(data), &result); err != nil {
		return nil, fmt.Errorf("resposta inválida do servidor: %s", string(data))
	}
	return &result, nil
}

func buildSignJSON(content, token string) string {
	if token != "" {
		return fmt.Sprintf(`{"content":%s,"token":%s}`, jsonStr(content), jsonStr(token))
	}
	return fmt.Sprintf(`{"content":%s}`, jsonStr(content))
}

// jsonStr serializa uma string Go para valor JSON (com escaping básico).
func jsonStr(s string) string {
	b, _ := json.Marshal(s)
	return string(b)
}
