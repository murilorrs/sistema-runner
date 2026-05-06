package jdk

import (
	"os"
	"testing"
)

func TestJavaBinary_NaoVazio(t *testing.T) {
	bin := javaBinary()
	if bin == "" {
		t.Error("javaBinary() não deve retornar string vazia")
	}
}

func TestIsJava21Plus_CaminhoInexistente_RetornaFalso(t *testing.T) {
	valid, _ := isJava21Plus("/caminho/que/nao/existe/java")
	if valid {
		t.Error("esperado false para executável inexistente")
	}
}

func TestFindJavaInDir_DiretorioVazio_RetornaErro(t *testing.T) {
	dir, err := os.MkdirTemp("", "jdk-test-*")
	if err != nil {
		t.Fatalf("falha ao criar diretório temporário: %v", err)
	}
	defer os.RemoveAll(dir)

	_, err = findJavaInDir(dir)
	if err == nil {
		t.Error("esperado erro ao procurar java em diretório vazio")
	}
}

func TestJdkDownloadURL_ConsistenciaURLExtensao(t *testing.T) {
	url, ext := jdkDownloadURL()
	if url != "" && ext == "" {
		t.Error("se URL é preenchida, ext também deve ser preenchida")
	}
	if url == "" && ext != "" {
		t.Error("se URL é vazia, ext também deve ser vazia")
	}
}

func TestJdkPath_DentroDeHubSaude(t *testing.T) {
	home, _ := os.UserHomeDir()
	path := jdkPath()
	if path == "" {
		t.Error("jdkPath() não deve retornar string vazia")
	}
	if len(path) <= len(home) {
		t.Errorf("jdkPath() deve ser subdiretório de home: %s", path)
	}
}