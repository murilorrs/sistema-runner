package runner

import "testing"

func TestSavePID_LoadPID_RoundTrip(t *testing.T) {
	const port = 19999
	defer RemovePID(port)

	if err := SavePID(12345, port); err != nil {
		t.Fatalf("SavePID falhou: %v", err)
	}

	pid := LoadPID(port)
	if pid != 12345 {
		t.Errorf("esperado PID 12345, obtido %d", pid)
	}
}

func TestLoadPID_PortaInexistente(t *testing.T) {
	pid := LoadPID(19998)
	if pid != -1 {
		t.Errorf("esperado -1 para porta sem PID registrado, obtido %d", pid)
	}
}

func TestRemovePID_LimpaArquivo(t *testing.T) {
	const port = 19997
	if err := SavePID(99999, port); err != nil {
		t.Fatalf("SavePID falhou: %v", err)
	}
	RemovePID(port)
	pid := LoadPID(port)
	if pid != -1 {
		t.Errorf("esperado -1 após remoção, obtido %d", pid)
	}
}

func TestSavePID_PortasDiferentesNaoColidem(t *testing.T) {
	const portA, portB = 19994, 19993
	defer RemovePID(portA)
	defer RemovePID(portB)

	if err := SavePID(1111, portA); err != nil {
		t.Fatalf("SavePID portA falhou: %v", err)
	}
	if err := SavePID(2222, portB); err != nil {
		t.Fatalf("SavePID portB falhou: %v", err)
	}

	if LoadPID(portA) != 1111 {
		t.Errorf("PID portA corrompido")
	}
	if LoadPID(portB) != 2222 {
		t.Errorf("PID portB corrompido")
	}
}