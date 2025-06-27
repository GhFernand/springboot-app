# Projeto: Middleware de Integração SeSuits

Este projeto é uma aplicação Java com Spring Boot que atua como middleware entre o sistema **SeSuits** e outros sistemas externos, centralizando a comunicação via APIs REST.

---

## 🚀 Tecnologias Utilizadas

- Java 17
- Spring Boot 3.2
- Maven
- SLF4J + Logback
- REST (Spring Web)
- Consumo de APIs externas via `RestTemplate`

---

## 📁 Estrutura de Pacotes

```
com.semiddleware.integration
├── controller   # Endpoints REST disponíveis ao SeSuits
├── service      # Regras de negócio e orquestrações
├── client       # Integração com APIs externas (ex: ViaCEP)
├── model        # DTOs de entrada e saída
└── IntegrationApplication.java  # Classe principal
```

---

## 🔧 Endpoints REST

### `POST /api/v1/integracao/enviar-dados`
- Envia dados para um sistema externo fictício.
- Espera um JSON com `campo1` e `campo2`.

### `POST /api/v1/integracao/mostrar-dados`
- Apenas retorna os dados enviados e gera log dedicado.
- Usado para debug e validação de comunicação.

### `GET /api/v1/integracao/cep?valor={CEP}`
- Consulta a API pública [ViaCEP](https://viacep.com.br) com o CEP informado.
- Retorna o endereço completo.

---

## 📝 Logs

- Logs padrão vão para o **console**.
- O endpoint `/mostrar-dados` grava também no arquivo `logs/mostrar-dados.log`.
- Logback configurado em `src/main/resources/logback-spring.xml`.

---

## ▶️ Como executar

```bash
mvn spring-boot:run
```

Ou gere o `.jar` com:

```bash
mvn clean package
java -jar target/integration-0.0.1-SNAPSHOT.jar
```

---

## 👶 Guia Rápido: Como criar uma nova integração com outra API

Criar uma nova integração entre o SeSuits e uma API externa usando este projeto, siga esses passos básicos:

### 📌 Exemplo: integrar com uma nova API externa

### 1. Criar um DTO
No pacote `model`, crie uma classe Java para representar os dados que você vai enviar ou receber da API externa.

```java
// ExemploModelDTO.java
public class ExemploModelDTO {
    private String nome;
    private int idade;
    // getters e setters
}
```

---

### 2. Criar um método de consumo da API externa
No pacote `client`, adicione um método que use `RestTemplate` para chamar a API.

```java
@Component
public class NovaApiClient {
    private final RestTemplate restTemplate = new RestTemplate();

    public String consumirApi(String parametro) {
        String url = "https://exemplo.com/api?valor=" + parametro;
        return restTemplate.getForObject(url, String.class);
    }
}
```

---

### 3. Criar um método no `service`
No pacote `service`, crie um método intermediário que chame o client:

```java
@Service
public class NovaApiService {
    private final NovaApiClient client;

    public NovaApiService(NovaApiClient client) {
        this.client = client;
    }

    public String buscarResultado(String valor) {
        return client.consumirApi(valor);
    }
}
```

---

### 4. Criar um endpoint no `controller`
No pacote `controller`, crie ou edite o controller existente:

```java
@RestController
@RequestMapping("/api/v1/integracao")
public class NovaApiController {
    private final NovaApiService service;

    public NovaApiController(NovaApiService service) {
        this.service = service;
    }

    @GetMapping("/nova-api")
    public ResponseEntity<String> integrar(@RequestParam String valor) {
        return ResponseEntity.ok(service.buscarResultado(valor));
    }
}
```

---

### ✅ Pronto!
Agora o SeSuits pode fazer uma requisição para:

```
GET http://localhost:8080/api/v1/integracao/nova-api?valor=teste
```

E o middleware irá consumir a nova API externa e retornar a resposta.

Se tiver dúvidas, comece copiando um dos exemplos existentes como `cep`, e vá testando com Postman ou Insomnia.


---

## 🧪 Exemplo de Consumo Interno (sem chamar APIs externas)

Querer criar um endpoint que **não faz chamadas externas**, apenas processa ou retorna dados diretamente no próprio Java.

### 🎯 Exemplo: gerar um "relatório" baseado no nome enviado

---

### 1. Criar um DTO para o retorno

📁 `model/RelatorioDTO.java`
```java
public class RelatorioDTO {
    private String nome;
    private String mensagem;

    public RelatorioDTO(String nome, String mensagem) {
        this.nome = nome;
        this.mensagem = mensagem;
    }

    public String getNome() { return nome; }
    public String getMensagem() { return mensagem; }
}
```

---

### 2. Criar um método no `service`

📁 `service/RelatorioService.java`
```java
@Service
public class RelatorioService {
    public RelatorioDTO gerarRelatorio(String nome) {
        String mensagem = "Olá " + nome + ", seu relatório foi gerado com sucesso!";
        return new RelatorioDTO(nome, mensagem);
    }
}
```

---

### 3. Criar um endpoint no `controller`

📁 `controller/RelatorioController.java`
```java
@RestController
@RequestMapping("/api/v1/integracao")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/relatorio")
    public ResponseEntity<RelatorioDTO> gerar(@RequestParam String nome) {
        return ResponseEntity.ok(relatorioService.gerarRelatorio(nome));
    }
}
```

---

### ✅ Testando

```http
GET http://localhost:8080/api/v1/integracao/relatorio?nome=Alex
```

**Resposta:**
```json
{
  "nome": "Alex",
  "mensagem": "Olá Alex, seu relatório foi gerado com sucesso!"
}
```

---

🧠 **Esse tipo de endpoint é ótimo para lógica de negócio interna, validações, testes e operações simples.**

---

## 🔄 Exemplo: Criar um consumo **interno** (sem acessar APIs externas)

Nem toda integração precisa acessar um sistema de fora. Às vezes, queremos apenas processar ou gerar dados **dentro do próprio middleware**.

Abaixo está um exemplo simples que recebe um nome e retorna um "relatório simulado", sem se comunicar com nenhum sistema externo.

---

### 🧱 1. Criar o DTO `RelatorioDTO`

```java
package com.semiddleware.integration.model;

public class RelatorioDTO {
    private String nome;
    private String status;
    private String mensagem;

    // Getters e setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
}
```

---

### 🧠 2. Criar um método de negócio no `service`

```java
@Service
public class RelatorioService {

    public RelatorioDTO gerarRelatorio(String nome) {
        RelatorioDTO dto = new RelatorioDTO();
        dto.setNome(nome);
        dto.setStatus("GERADO");
        dto.setMensagem("Relatório gerado com sucesso para " + nome);
        return dto;
    }
}
```

---

### 🌐 3. Criar o endpoint no `controller`

```java
@RestController
@RequestMapping("/api/v1/integracao")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/relatorio")
    public ResponseEntity<RelatorioDTO> gerarRelatorio(@RequestParam String nome) {
        return ResponseEntity.ok(relatorioService.gerarRelatorio(nome));
    }
}
```

---

### ✅ Testar

Você pode acessar com:

```
GET http://localhost:8080/api/v1/integracao/relatorio?nome=Alex
```

Resposta esperada:

```json
{
  "nome": "Alex",
  "status": "GERADO",
  "mensagem": "Relatório gerado com sucesso para Alex"
}
```

Esse exemplo mostra como você pode usar o middleware como uma central de regras e simulações internas, sem necessidade de consumir outra API.

