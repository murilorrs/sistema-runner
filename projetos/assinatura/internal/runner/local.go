// Package runner gerencia a invocação do assinador.jar (modo local e modo servidor).
package runner

import (
	"bytes"
	"encoding/json"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"

	"github.com/kyriosdata/assinatura/internal/jdk"
)

// SignatureResponse espelha a resposta JSON do assinador.jar.
type SignatureResponse struct {
	Signature string `json:"signature"`
	Valid     bool   `json:"valid"`
	Message   string `json:"message"`
}

// FindJar localiza o assinador.jar. Procura em:
//  1. Mesmo diretório do executável
//  2. ~/.hubsaude/assinador.jar
func FindJar() (string, error) {
	// Junto ao executável
	exe, err := os.Executable()
	if err == nil {
		candidate := filepath.Join(filepath.Dir(exe), "assinador.jar")
		if _, err := os.Stat(candidate); err == nil {
			return candidate, nil
		}
	}

	// ~/.hubsaude/assinador.jar
	home, _ := os.UserHomeDir()
	candidate := filepath.Join(home, ".hubsaude", "assinador.jar")
	if _, err := os.Stat(candidate); err == nil {
		return candidate, nil
	}

	return "", fmt.Errorf(
		"assinador.jar não encontrado. Coloque-o no mesmo diretório que este CLI ou em ~/.hubsaude/assinador.jar",
	)
}

// LocalSign executa assinador.jar sign via java -jar (modo local / cold start).
func LocalSign(content, token string) (*SignatureResponse, error) {
	args := buildSignArgs(content, token)
	return runJar(args)
}

// LocalValidate executa assinador.jar validate via java -jar (modo local).
func LocalValidate(content, signature string) (*SignatureResponse, error) {
	args := buildValidateArgs(content, signature)
	return runJar(args)
}

func runJar(args []string) (*SignatureResponse, error) {
	java, err := jdk.FindJava()
	if err != nil {
		return nil, err
	}

	jarPath, err := FindJar()
	if err != nil {
		return nil, err
	}

	cmdArgs := append([]string{"-jar", jarPath}, args...)
	cmd := exec.Command(java, cmdArgs...)

	var stdout, stderr bytes.Buffer
	cmd.Stdout = &stdout
	cmd.Stderr = &stderr

	if err := cmd.Run(); err != nil {
		// código de saída 1 = resposta de erro válida do assinador
		if stdout.Len() > 0 {
			return parseResponse(stdout.Bytes())
		}
		return nil, fmt.Errorf("erro ao executar assinador.jar: %w\n%s", err, stderr.String())
	}

	return parseResponse(stdout.Bytes())
}

func parseResponse(data []byte) (*SignatureResponse, error) {
	var resp SignatureResponse
	if err := json.Unmarshal(bytes.TrimSpace(data), &resp); err != nil {
		return nil, fmt.Errorf("resposta inválida do assinador.jar: %s", string(data))
	}
	return &resp, nil
}

func buildSignArgs(content, token string) []string {
	args := []string{"sign", "--content", content}
	if token != "" {
		args = append(args, "--token", token)
	}
	return args
}

func buildValidateArgs(content, signature string) []string {
	return []string{"validate", "--content", content, "--signature", signature}
}
