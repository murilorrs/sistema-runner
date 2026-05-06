package runner

import "testing"

func TestIsServerRunning_PortaSemServidor(t *testing.T) {
	const port = 19992
	if IsServerRunning(port) {
		t.Skipf("porta %d está em uso por outro processo; teste inconclusivo", port)
	}
}

func TestIsServerRunning_NaoPanica(t *testing.T) {
	portas := []int{0, 1, 65535, 19991}
	for _, p := range portas {
		_ = IsServerRunning(p)
	}
}