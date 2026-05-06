package cmd

import (
	"os"

	"github.com/spf13/cobra"
)

var rootCmd = &cobra.Command{
	Use:   "assinatura",
	Short: "CLI para assinatura digital via assinador.jar",
	Long: `Sistema Runner — CLI multiplataforma que permite criar e validar
assinaturas digitais simuladas sem necessidade de configurar Java manualmente.

Exemplos:
  assinatura sign --content dGVzdGU=
  assinatura validate --content dGVzdGU= --signature MOCKED_SIGNATURE_BASE64_==
  assinatura server start
  assinatura server stop`,
}

func Execute() {
	if err := rootCmd.Execute(); err != nil {
		os.Exit(1)
	}
}

func init() {
	// Flags globais podem ser definidas aqui
}
