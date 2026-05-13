package runner

import (
	"encoding/json"
	"testing"
)

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

func TestBuildSignJSON_SemToken(t *testing.T) {
	got := buildSignJSON("dGVzdGU=", "")
	want := `{"content":"dGVzdGU="}`
	if got != want {
		t.Errorf("buildSignJSON sem token: quer %q, obteve %q", want, got)
	}
}

func TestBuildSignJSON_ComToken(t *testing.T) {
	got := buildSignJSON("dGVzdGU=", "/dev/token")
	want := `{"content":"dGVzdGU=","token":"/dev/token"}`
	if got != want {
		t.Errorf("buildSignJSON com token: quer %q, obteve %q", want, got)
	}
}

func TestJsonStr_EscapeAspasEQuebra(t *testing.T) {
	in := "a\"b\nc"
	wantBytes, err := json.Marshal(in)
	if err != nil {
		t.Fatal(err)
	}
	want := string(wantBytes)
	if got := jsonStr(in); got != want {
		t.Errorf("jsonStr deve coincidir com json.Marshal: quer %q, obteve %q", want, got)
	}
}
