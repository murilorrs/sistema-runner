# Sistema Runner

Sistema Runner é uma solução multiplataforma que facilita o acesso à funcionalidade de execução de aplicações Java via linha de comandos, desenvolvida para a Plataforma HubSaúde.

## Estrutura do repositório

```
sistema-runner/
├── projetos/
│   ├── assinatura/        ← CLI em Go para assinatura digital
│   ├── assinador-java/    ← Backend Java (assinador.jar)
│   └── simulador/         ← CLI em Go para gerenciar o simulador HubSaúde
├── docs/                  ← Documentação e planos de sprint
├── especificacao.md       ← Requisitos e user stories
└── design.md              ← Diagramas C4 de arquitetura
```

## Binários disponíveis

Os binários pré-compilados para Windows, Linux e macOS estão disponíveis nas [GitHub Releases](../../releases).

Cada artefato é acompanhado de:
- `.sha256` — checksum de integridade
- `.sig` + `.pem` — assinatura via Cosign (Sigstore)

### Verificar autenticidade

```bash
cosign verify-blob \
  --certificate assinatura-v0.1.0-linux-amd64.pem \
  --signature   assinatura-v0.1.0-linux-amd64.sig \
  assinatura-v0.1.0-linux-amd64
```

---

## CLI `assinatura`

### Criar uma assinatura

```bash
# Modo servidor (padrão — usa instância HTTP se disponível)
assinatura sign --content dGVzdGU=

# Com token PKCS#11
assinatura sign --content dGVzdGU= --token /caminho/token.p11

# Modo local (java -jar direto)
assinatura sign --content dGVzdGU= --local
```

### Validar uma assinatura

```bash
assinatura validate --content dGVzdGU= --signature MOCKED_SIGNATURE_BASE64_==
```

### Modo servidor HTTP (menor latência)

```bash
# Iniciar o servidor assinador
assinatura server start

# Iniciar com timeout automático de 30 minutos
assinatura server start --timeout 30

# Verificar status
assinatura server status

# Encerrar
assinatura server stop
```

### Outros comandos

```bash
assinatura version
assinatura --help
```

---

## CLI `simulador`

```bash
# Iniciar o Simulador do HubSaúde (baixa automaticamente se necessário)
simulador start

# Usar URL alternativa para download
simulador start --source https://meu-servidor/simulador.jar

# Verificar status
simulador status

# Encerrar
simulador stop

simulador version
```

---

## Como publicar uma nova release

1. Edite a versão em `projetos/assinatura/cmd/version.go` e `projetos/simulador/cmd/version.go`
2. Faça commit e push para `main`
3. O pipeline detecta automaticamente a nova versão, gera os binários, assina com Cosign e publica na release

---

## Desenvolvimento

### assinador.jar (Java 21 + Maven)

```bash
cd projetos/assinador-java

# Rodar testes
mvn test

# Gerar JAR executável
mvn package

# Testar manualmente
java -jar target/assinador.jar sign --content dGVzdGU=
java -jar target/assinador.jar validate --content dGVzdGU= --signature MOCKED_SIGNATURE_BASE64_==
java -jar target/assinador.jar --server --port 8080
```

### assinatura CLI (Go)

```bash
cd projetos/assinatura
go build ./...
go vet ./...
./assinatura version
./assinatura sign --content dGVzdGU= --local
```

### simulador CLI (Go)

```bash
cd projetos/simulador
go build ./...
go vet ./...
./simulador version
./simulador start
```
