package cmd

import (
	"fmt"
	"os"

	"github.com/kyriosdata/assinatura/internal/runner"
	"github.com/spf13/cobra"
)

var signCmd = &cobra.Command{
	Use:   "sign",
	Short: "Cria uma assinatura digital simulada",
	Long: `Cria uma assinatura digital simulada para o conteúdo fornecido.

Por padrão, usa o modo servidor (HTTP) se houver instância do assinador rodando.
Use --local para forçar invocação direta via java -jar.

Exemplos:
  assinatura sign --content dGVzdGU=
  assinatura sign --content dGVzdGU= --token /caminho/token.p11
  assinatura sign --content dGVzdGU= --local`,
	RunE: func(cmd *cobra.Command, args []string) error {
		content, _ := cmd.Flags().GetString("content")
		token, _ := cmd.Flags().GetString("token")
		local, _ := cmd.Flags().GetBool("local")
		port, _ := cmd.Flags().GetInt("port")

		if content == "" {
			return fmt.Errorf("--content é obrigatório")
		}

		var resp *runner.SignatureResponse
		var err error

		if !local && runner.IsServerRunning(port) {
			resp, err = runner.ServerSign(port, content, token)
			if err != nil {
				fmt.Fprintf(os.Stderr, "Aviso: falha no modo servidor (%v). Usando modo local.\n", err)
				resp, err = runner.LocalSign(content, token)
			}
		} else {
			resp, err = runner.LocalSign(content, token)
		}

		if err != nil {
			fmt.Fprintf(os.Stderr, "Erro: %v\n", err)
			os.Exit(1)
		}

		printSignResponse(resp)
		if !resp.Valid {
			os.Exit(1)
		}
		return nil
	},
}

func printSignResponse(r *runner.SignatureResponse) {
	if r.Valid {
		fmt.Println("✓ Assinatura criada com sucesso")
		fmt.Printf("  Assinatura: %s\n", r.Signature)
		fmt.Printf("  Mensagem:   %s\n", r.Message)
	} else {
		fmt.Fprintf(os.Stderr, "✗ Erro: %s\n", r.Message)
	}
}

func init() {
	signCmd.Flags().String("content", "", "Conteúdo a ser assinado (em Base64) [obrigatório]")
	signCmd.Flags().String("token", "", "Caminho para o token/smart card PKCS#11 (opcional)")
	signCmd.Flags().Bool("local", false, "Forçar invocação direta via java -jar (modo local)")
	signCmd.Flags().Int("port", 8080, "Porta do servidor assinador (modo servidor)")
	signCmd.MarkFlagRequired("content") //nolint:errcheck
	rootCmd.AddCommand(signCmd)
}
