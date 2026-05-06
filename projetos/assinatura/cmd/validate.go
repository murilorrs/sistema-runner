package cmd

import (
	"fmt"
	"os"

	"github.com/kyriosdata/assinatura/internal/runner"
	"github.com/spf13/cobra"
)

var validateCmd = &cobra.Command{
	Use:   "validate",
	Short: "Valida uma assinatura digital simulada",
	Long: `Valida uma assinatura digital para o conteúdo e assinatura fornecidos.

Por padrão, usa o modo servidor (HTTP) se houver instância do assinador rodando.
Use --local para forçar invocação direta via java -jar.

Exemplos:
  assinatura validate --content dGVzdGU= --signature MOCKED_SIGNATURE_BASE64_==
  assinatura validate --content dGVzdGU= --signature MOCKED_SIGNATURE_BASE64_== --local`,
	RunE: func(cmd *cobra.Command, args []string) error {
		content, _ := cmd.Flags().GetString("content")
		signature, _ := cmd.Flags().GetString("signature")
		local, _ := cmd.Flags().GetBool("local")
		port, _ := cmd.Flags().GetInt("port")

		if content == "" {
			return fmt.Errorf("--content é obrigatório")
		}
		if signature == "" {
			return fmt.Errorf("--signature é obrigatório")
		}

		var resp *runner.SignatureResponse
		var err error

		if !local && runner.IsServerRunning(port) {
			resp, err = runner.ServerValidate(port, content, signature)
			if err != nil {
				fmt.Fprintf(os.Stderr, "Aviso: falha no modo servidor (%v). Usando modo local.\n", err)
				resp, err = runner.LocalValidate(content, signature)
			}
		} else {
			resp, err = runner.LocalValidate(content, signature)
		}

		if err != nil {
			fmt.Fprintf(os.Stderr, "Erro: %v\n", err)
			os.Exit(1)
		}

		printValidateResponse(resp)
		return nil
	},
}

func printValidateResponse(r *runner.SignatureResponse) {
	if r.Valid {
		fmt.Println("✓ Assinatura VÁLIDA")
		fmt.Printf("  Mensagem: %s\n", r.Message)
	} else {
		fmt.Printf("✗ Assinatura INVÁLIDA\n")
		fmt.Printf("  Mensagem: %s\n", r.Message)
	}
}

func init() {
	validateCmd.Flags().String("content", "", "Conteúdo original (em Base64) [obrigatório]")
	validateCmd.Flags().String("signature", "", "Assinatura digital a validar [obrigatório]")
	validateCmd.Flags().Bool("local", false, "Forçar invocação direta via java -jar (modo local)")
	validateCmd.Flags().Int("port", 8080, "Porta do servidor assinador (modo servidor)")
	validateCmd.MarkFlagRequired("content")    //nolint:errcheck
	validateCmd.MarkFlagRequired("signature")  //nolint:errcheck
	rootCmd.AddCommand(validateCmd)
}
