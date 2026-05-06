// Package jdk detecta ou provisiona automaticamente o JDK 21.
package jdk

import (
	"archive/tar"
	"archive/zip"
	"compress/gzip"
	"fmt"
	"io"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"runtime"
	"strings"
)

const jdkVersion = "21"

// hubSaudePath é o diretório gerenciado pelo Sistema Runner.
func hubSaudePath() string {
	home, _ := os.UserHomeDir()
	return filepath.Join(home, ".hubsaude")
}

// jdkPath é onde o JDK provisionado fica armazenado.
func jdkPath() string {
	return filepath.Join(hubSaudePath(), "jdk")
}

// FindJava retorna o caminho completo do executável `java` (versão >= 21).
// Se não encontrar no PATH nem em ~/.hubsaude/jdk/, baixa e provisiona automaticamente.
func FindJava() (string, error) {
	// 1. Verificar JAVA_HOME
	if jh := os.Getenv("JAVA_HOME"); jh != "" {
		java := filepath.Join(jh, "bin", javaBinary())
		if valid, _ := isJava21Plus(java); valid {
			return java, nil
		}
	}

	// 2. Verificar PATH
	if java, err := exec.LookPath(javaBinary()); err == nil {
		if valid, _ := isJava21Plus(java); valid {
			return java, nil
		}
	}

	// 3. Verificar ~/.hubsaude/jdk/
	provisionedJava := filepath.Join(jdkPath(), "bin", javaBinary())
	if valid, _ := isJava21Plus(provisionedJava); valid {
		return provisionedJava, nil
	}

	// 4. Baixar JDK automaticamente
	fmt.Println("JDK 21 não encontrado. Baixando automaticamente...")
	if err := downloadJDK(); err != nil {
		return "", fmt.Errorf("falha ao baixar JDK: %w", err)
	}

	// Relocar o executável dentro do diretório extraído
	java, err := findJavaInDir(jdkPath())
	if err != nil {
		return "", fmt.Errorf("JDK baixado mas executável não encontrado: %w", err)
	}
	return java, nil
}

// isJava21Plus verifica se o executável existe e é versão >= 21.
func isJava21Plus(path string) (bool, error) {
	if _, err := os.Stat(path); os.IsNotExist(err) {
		return false, nil
	}
	out, err := exec.Command(path, "-version").CombinedOutput()
	if err != nil {
		return false, err
	}
	output := strings.ToLower(string(out))
	for _, v := range []string{`version "21`, `version "22`, `version "23`, `version "24`, `version "25`} {
		if strings.Contains(output, v) {
			return true, nil
		}
	}
	return false, nil
}

// downloadJDK baixa e extrai o JDK 21 (Adoptium Temurin) para ~/.hubsaude/jdk/.
func downloadJDK() error {
	url, ext := jdkDownloadURL()
	if url == "" {
		return fmt.Errorf("plataforma %s/%s não suportada para download automático de JDK", runtime.GOOS, runtime.GOARCH)
	}

	if err := os.MkdirAll(hubSaudePath(), 0755); err != nil {
		return err
	}

	tmpFile := filepath.Join(hubSaudePath(), "jdk-download"+ext)
	fmt.Printf("Baixando JDK 21 de %s ...\n", url)

	if err := downloadFile(url, tmpFile); err != nil {
		return fmt.Errorf("falha no download: %w", err)
	}
	defer os.Remove(tmpFile)

	dest := jdkPath()
	os.RemoveAll(dest)
	os.MkdirAll(dest, 0755)

	fmt.Println("Extraindo JDK...")
	if ext == ".tar.gz" {
		return extractTarGz(tmpFile, dest)
	}
	return extractZip(tmpFile, dest)
}

// jdkDownloadURL retorna a URL de download do JDK 21 (Adoptium Temurin) para a plataforma atual.
func jdkDownloadURL() (url, ext string) {
	base := "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.5%2B11/"
	switch runtime.GOOS {
	case "linux":
		if runtime.GOARCH == "amd64" {
			return base + "OpenJDK21U-jdk_x64_linux_hotspot_21.0.5_11.tar.gz", ".tar.gz"
		}
	case "darwin":
		if runtime.GOARCH == "amd64" {
			return base + "OpenJDK21U-jdk_x64_mac_hotspot_21.0.5_11.tar.gz", ".tar.gz"
		}
		if runtime.GOARCH == "arm64" {
			return base + "OpenJDK21U-jdk_aarch64_mac_hotspot_21.0.5_11.tar.gz", ".tar.gz"
		}
	case "windows":
		if runtime.GOARCH == "amd64" {
			return base + "OpenJDK21U-jdk_x64_windows_hotspot_21.0.5_11.zip", ".zip"
		}
	}
	return "", ""
}

func downloadFile(url, dest string) error {
	resp, err := http.Get(url) //nolint:gosec
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("HTTP %d ao baixar %s", resp.StatusCode, url)
	}

	f, err := os.Create(dest)
	if err != nil {
		return err
	}
	defer f.Close()

	_, err = io.Copy(f, resp.Body)
	return err
}

func extractTarGz(src, dest string) error {
	f, err := os.Open(src)
	if err != nil {
		return err
	}
	defer f.Close()

	gz, err := gzip.NewReader(f)
	if err != nil {
		return err
	}
	defer gz.Close()

	tr := tar.NewReader(gz)
	// O tar.gz do JDK tem um diretório raiz como jdk-21.0.5+11/
	// Queremos extrair o conteúdo desse diretório raiz para dest/
	var rootDir string

	for {
		hdr, err := tr.Next()
		if err == io.EOF {
			break
		}
		if err != nil {
			return err
		}

		// Detectar o diretório raiz no primeiro entry
		parts := strings.SplitN(hdr.Name, "/", 2)
		if rootDir == "" {
			rootDir = parts[0]
		}

		// Strip do diretório raiz
		relPath := hdr.Name
		if strings.HasPrefix(relPath, rootDir+"/") {
			relPath = strings.TrimPrefix(relPath, rootDir+"/")
		}
		if relPath == "" {
			continue
		}

		target := filepath.Join(dest, filepath.FromSlash(relPath))

		switch hdr.Typeflag {
		case tar.TypeDir:
			os.MkdirAll(target, 0755)
		case tar.TypeReg:
			os.MkdirAll(filepath.Dir(target), 0755)
			out, err := os.OpenFile(target, os.O_CREATE|os.O_WRONLY|os.O_TRUNC, os.FileMode(hdr.Mode))
			if err != nil {
				return err
			}
			_, err = io.Copy(out, tr)
			out.Close()
			if err != nil {
				return err
			}
		case tar.TypeSymlink:
			os.MkdirAll(filepath.Dir(target), 0755)
			os.Symlink(hdr.Linkname, target) //nolint:errcheck
		}
	}
	return nil
}

func extractZip(src, dest string) error {
	r, err := zip.OpenReader(src)
	if err != nil {
		return err
	}
	defer r.Close()

	var rootDir string
	for _, f := range r.File {
		parts := strings.SplitN(f.Name, "/", 2)
		if rootDir == "" {
			rootDir = parts[0]
		}

		relPath := f.Name
		if strings.HasPrefix(relPath, rootDir+"/") {
			relPath = strings.TrimPrefix(relPath, rootDir+"/")
		}
		if relPath == "" {
			continue
		}

		target := filepath.Join(dest, filepath.FromSlash(relPath))

		if f.FileInfo().IsDir() {
			os.MkdirAll(target, 0755)
			continue
		}

		os.MkdirAll(filepath.Dir(target), 0755)
		rc, err := f.Open()
		if err != nil {
			return err
		}
		out, err := os.OpenFile(target, os.O_CREATE|os.O_WRONLY|os.O_TRUNC, f.Mode())
		if err != nil {
			rc.Close()
			return err
		}
		_, err = io.Copy(out, rc)
		rc.Close()
		out.Close()
		if err != nil {
			return err
		}
	}
	return nil
}

// findJavaInDir procura o executável java dentro de dir (inclusive em subdiretórios bin/).
func findJavaInDir(dir string) (string, error) {
	bin := javaBinary()
	candidates := []string{
		filepath.Join(dir, "bin", bin),
		filepath.Join(dir, "jdk", "bin", bin),
		filepath.Join(dir, "Contents", "Home", "bin", bin), // macOS .tar.gz layout
	}
	for _, c := range candidates {
		if _, err := os.Stat(c); err == nil {
			return c, nil
		}
	}
	// busca recursiva limitada
	var found string
	filepath.WalkDir(dir, func(path string, d os.DirEntry, err error) error { //nolint:errcheck
		if err != nil || found != "" {
			return nil
		}
		if !d.IsDir() && d.Name() == bin {
			found = path
		}
		return nil
	})
	if found != "" {
		return found, nil
	}
	return "", fmt.Errorf("executável java não encontrado em %s", dir)
}

func javaBinary() string {
	if runtime.GOOS == "windows" {
		return "java.exe"
	}
	return "java"
}
