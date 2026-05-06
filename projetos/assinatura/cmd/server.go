package cmd

import (
	"fmt"
	"os"

	"github.com/kyriosdata/assinatura/internal/runner"
	"github.com/spf13/cobra"
)

var serverCmd = &cobra.Command{
	Use:   "server",
	Short: "Gerencia o assinador.jar em modo servidor HTTP",
	Long:  `Inicia, para e consulta o status do assinador.jar rodando como servidor HTTP.`,
}

var serverStartCmd = &cobra.Command{
	Use:   "start",
	Short: "Inicia o assinador.jar em modo servidor HTTP",
	Long: `Inicia o assinador.jar em background como servidor HTTP.

Exemplos:
  assinatura server start
  assinatura server start --port 9090
  assinatura server start --port 8080 --timeout 30`,
	RunE: func(cmd *cobra.Command, args []string) error {
		port, _ := cmd.Flags().GetInt("port")
		timeout, _ := cmd.Flags().GetInt("timeout")

		if runner.IsServerRunning(port) {
			fmt.Printf("Servidor assinador já está rodando na porta %d\n", port)
			return nil
		}

		fmt.Printf("Iniciando servidor assinador na porta %d...\n", port)
		if err := runner.StartServer(port, timeout); err != nil {
			fmt.Fprintf(os.Stderr, "Erro ao iniciar servidor: %v\n", err)
			os.Exit(1)
		}

		fmt.Printf("✓ Servidor assinador iniciado na porta %d\n", port)
		if timeout > 0 {
			fmt.Printf("  Auto-encerramento após %d minutos de inatividade\n", timeout)
		}
		return nil
	},
}

var serverStopCmd = &cobra.Command{
	Use:   "stop",
	Short: "Encerra o servidor assinador",
	Long: `Encerra o servidor assinador na porta padrão ou na porta especificada.

Exemplos:
  assinatura server stop
  assinatura server stop --port 9090`,
	RunE: func(cmd *cobra.Command, args []string) error {
		port, _ := cmd.Flags().GetInt("port")

		if !runner.IsServerRunning(port) {
			fmt.Printf("Nenhum servidor assinador ativo na porta %d\n", port)
			return nil
		}

		fmt.Printf("Encerrando servidor assinador na porta %d...\n", port)
		if err := runner.StopServer(port); err != nil {
			fmt.Fprintf(os.Stderr, "Erro ao encerrar servidor: %v\n", err)
			os.Exit(1)
		}

		fmt.Printf("✓ Servidor assinador encerrado\n")
		return nil
	},
}

var serverStatusCmd = &cobra.Command{
	Use:   "status",
	Short: "Exibe o status do servidor assinador",
	Long: `Verifica se o servidor assinador está ativo em uma porta.

Exemplos:
  assinatura server status
  assinatura server status --port 9090`,
	Run: func(cmd *cobra.Command, args []string) {
		port, _ := cmd.Flags().GetInt("port")

		if runner.IsServerRunning(port) {
			fmt.Printf("✓ Servidor assinador ATIVO na porta %d\n", port)
			pid := runner.LoadPID(port)
			if pid > 0 {
				fmt.Printf("  PID: %d\n", pid)
			}
		} else {
			fmt.Printf("✗ Servidor assinador NÃO está ativo na porta %d\n", port)
		}
	},
}

func init() {
	serverStartCmd.Flags().Int("port", 8080, "Porta em que o servidor vai escutar")
	serverStartCmd.Flags().Int("timeout", 0, "Encerrar automaticamente após N minutos de inatividade (0 = nunca)")

	serverStopCmd.Flags().Int("port", 8080, "Porta do servidor a encerrar")

	serverStatusCmd.Flags().Int("port", 8080, "Porta do servidor a verificar")

	serverCmd.AddCommand(serverStartCmd, serverStopCmd, serverStatusCmd)
	rootCmd.AddCommand(serverCmd)
}
