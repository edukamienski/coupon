# Coupon API

API REST para criação, consulta e exclusão lógica de cupons.

As regras de negócio ficam no domínio, separado da entidade JPA. A aplicação utiliza arquitetura Hexagonal, com casos de uso orientados às intenções do usuário.

## Stack

* Java 21
* Spring Boot 4.1.1
* Maven
* Spring Web
* Spring Data JPA
* Bean Validation
* H2
* OpenAPI / Swagger

Pacote raiz: `com.eduardo.coupon`

## Arquitetura

```text
com.eduardo.coupon
├── domain
├── application
│   └── port
└── infrastructure
    ├── persistence
    └── web
```

Fluxo principal:

```text
HTTP → Controller → Use Case → Domain → Repository Port
                                      ↓
                              Persistence Adapter
                                      ↓
                                  JPA / H2
```

O domínio não depende de Spring ou JPA. A entidade de domínio é diferente da entidade de persistência.

## Regras de negócio

### Criação

* `code`, `description`, `discountValue` e `expirationDate` são obrigatórios.
* `published` é opcional e assume `false` quando ausente.
* O código mantém apenas letras e números e deve resultar em exatamente 6 caracteres.
* `ABC-123` resulta em `ABC123`.
* O desconto mínimo é `0.5`, sem limite máximo.
* A data de expiração não pode estar no passado.
* O cupom é criado com status `ACTIVE` e `redeemed = false`.

### Exclusão

* A exclusão é lógica.
* O status passa para `DELETED`.
* Os demais dados são preservados.
* Um cupom já deletado não pode ser excluído novamente.
* Cupons com status `DELETED` não são retornados pelo GET.

## API

| Método | Endpoint       | Sucesso |
| ------ | -------------- | ------- |
| POST   | `/coupon`      | 201     |
| GET    | `/coupon/{id}` | 200     |
| DELETE | `/coupon/{id}` | 204     |

Erros principais:

| Situação               | HTTP |
| ---------------------- | ---: |
| Dados inválidos        |  400 |
| Recurso não encontrado |  404 |
| Segunda exclusão       |  409 |
| Erro inesperado        |  500 |

O `id` é um UUID gerado pela aplicação.

## Swagger

Local:

`http://localhost:8080/swagger-ui.html`

Docker:

`http://localhost:8081/swagger-ui.html`

## Como executar

### Local

```powershell
.\mvnw.cmd spring-boot:run
```

A aplicação estará disponível em:

`http://localhost:8080`

### Docker

```powershell
docker compose up --build
```

A aplicação estará disponível em:

`http://localhost:8081`

A aplicação utiliza H2 em memória, portanto os dados existem somente enquanto o processo estiver em execução.

## Testes

```powershell
.\mvnw.cmd test
```

A suíte inclui:

* testes unitários do domínio;
* testes dos casos de uso;
* testes HTTP;
* testes de integração da persistência com H2 real.

A cobertura mínima configurada é de 80%.

Para executar também a verificação de cobertura:

```powershell
.\mvnw.cmd verify
```

## H2 Console

Local:

`http://localhost:8080/h2-console`

Docker:

`http://localhost:8081/h2-console`

```text
JDBC URL: jdbc:h2:mem:coupon
Usuário: sa
Senha: em branco
```

## Decisões técnicas

### Unicidade do código

A especificação não determina que `code` seja único. Por isso, não foi criada restrição de unicidade. O identificador do cupom é o UUID.

### Exclusão lógica

O registro permanece no banco com status `DELETED`, permitindo distinguir um cupom inexistente de um cupom que já foi excluído.

Para o GET, ambos são tratados como `404`. Para o DELETE, um cupom já excluído é identificado e resulta em `409`, conforme a especificação do desafio.