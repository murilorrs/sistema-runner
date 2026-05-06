package runner

import (
	"fmt"
	"os"
	"path/filepath"
	"strconv"
	"strings"
)

func hubSaudePath() string {
	home, _ := os.UserHomeDir()
	return filepath.Join(home, ".hubsaude")
}

func pidFile(port int) string {
	return filepath.Join(hubSaudePath(), fmt.Sprintf("assinador-%d.pid", port))
}

// SavePID persiste o PID e porta em ~/.hubsaude/assinador-<port>.pid.
func SavePID(pid, port int) error {
	if err := os.MkdirAll(hubSaudePath(), 0755); err != nil {
		return err
	}
	return os.WriteFile(pidFile(port), []byte(strconv.Itoa(pid)), 0644)
}

// LoadPID lê o PID salvo para a porta dada. Retorna -1 se não existir.
func LoadPID(port int) int {
	data, err := os.ReadFile(pidFile(port))
	if err != nil {
		return -1
	}
	pid, err := strconv.Atoi(strings.TrimSpace(string(data)))
	if err != nil {
		return -1
	}
	return pid
}

// RemovePID remove o arquivo PID para a porta dada.
func RemovePID(port int) {
	os.Remove(pidFile(port))
}
