package cmd

import (
	"fmt"
	"runtime"

	"github.com/spf13/cobra"
)

var version = "0.1.0"

var versionCmd = &cobra.Command{
	Use:   "version",
	Short: "Exibe a versão atual do CLI",
	Long:  `Exibe a versão atual do CLI assinatura junto com informações de plataforma.`,
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Printf("Assinatura CLI v%s %s/%s\n", version, runtime.GOOS, runtime.GOARCH)
	},
}

func init() {
	rootCmd.AddCommand(versionCmd)
}
