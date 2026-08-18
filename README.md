# Chat Desktop com JavaFX e Groq

Aplicação desktop simples desenvolvida em **Java 21** com **JavaFX**, integrada à API da **Groq** para criação de um chat com Inteligência Artificial.

Nesta primeira versão, toda a lógica da aplicação está concentrada na classe `Main.java`, com o objetivo de facilitar o aprendizado da integração entre Java, JavaFX e APIs de IA.

## Tecnologias utilizadas

* Java 21
* JavaFX 21
* Maven
* Java HttpClient
* Gson
* Groq API
* Modelo `openai/gpt-oss-20b`
* IntelliJ IDEA

## Funcionalidades

* Interface gráfica desenvolvida com JavaFX
* Campo para digitação de mensagens
* Envio de mensagens utilizando o botão **Enviar**
* Envio de mensagens pressionando **Enter**
* Comunicação com a API da Groq
* Exibição das respostas da Inteligência Artificial
* Histórico da conversa
* Manutenção do contexto entre as mensagens
* Requisição HTTP assíncrona para evitar travamento da interface

## Estrutura inicial do projeto

```text
chat-desktop
│
├── pom.xml
│
└── src
    └── main
        └── java
            │
            ├── module-info.java
            │
            └── com
                └── example
                    └── chatdesktop
                        └── Main.java
```

## Configuração necessária

Para utilizar o projeto é necessário possuir uma chave da API da Groq.

Na classe `Main.java`, localize:

```java
private static final String API_KEY =
        "COLE_SUA_CHAVE_GROQ_AQUI";
```

Substitua pelo seu token:

```java
private static final String API_KEY =
        "gsk_sua_chave_aqui";
```

> Esta configuração é utilizada apenas para simplificar a primeira versão do projeto. Em versões futuras, a chave deverá ser armazenada utilizando variável de ambiente ou outro mecanismo mais adequado.

## Modelo utilizado

O projeto utiliza o modelo:

```text
openai/gpt-oss-20b
```

A configuração está localizada na classe `Main.java`:

```java
private static final String MODEL =
        "openai/gpt-oss-20b";
```

## Endpoint da Groq

A aplicação utiliza o endpoint:

```text
https://api.groq.com/openai/v1/chat/completions
```

## Dependências principais

O projeto utiliza JavaFX para construção da interface gráfica:

```xml
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>21.0.6</version>
</dependency>
```

Também é utilizada a biblioteca Gson para trabalhar com JSON:

```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.14.0</version>
</dependency>
```

## Configuração do Java

O projeto foi desenvolvido utilizando:

```text
Java 21
```

O Maven deve estar configurado para utilizar a mesma versão:

```xml
<configuration>
    <release>21</release>
</configuration>
```

No IntelliJ IDEA também deve ser configurado:

```text
Project SDK: Java 21
Language Level: 21
```

## Executando o projeto

Após configurar a chave da Groq e atualizar as dependências Maven, execute a classe:

```text
Main.java
```

Ou utilize o Maven:

```bash
mvn clean javafx:run
```

## Funcionamento

O fluxo básico da aplicação é:

```text
Usuário
   ↓
JavaFX
   ↓
Main.java
   ↓
Java HttpClient
   ↓
Groq API
   ↓
Modelo de IA
   ↓
Resposta
   ↓
JavaFX
```

Quando o usuário envia uma mensagem, a aplicação:

1. Captura o texto digitado.
2. Adiciona a mensagem ao histórico.
3. Monta uma requisição JSON.
4. Envia a requisição para a Groq.
5. Aguarda a resposta de maneira assíncrona.
6. Processa o JSON retornado.
7. Exibe a resposta da IA.
8. Adiciona a resposta ao histórico da conversa.

## Histórico da conversa

A aplicação mantém um histórico contendo mensagens com os seguintes papéis:

```text
system
user
assistant
```

Exemplo:

```json
{
    "role": "user",
    "content": "O que é JavaFX?"
}
```

Dessa forma, a IA consegue considerar as mensagens anteriores durante a conversa.

## Objetivo educacional

Este projeto tem como objetivo demonstrar de maneira simples:

* criação de interfaces desktop utilizando JavaFX;
* consumo de APIs REST utilizando Java;
* requisições HTTP com `HttpClient`;
* manipulação de JSON;
* integração de aplicações Java com Inteligência Artificial;
* execução de tarefas assíncronas em aplicações JavaFX.

## Próximas melhorias

Em versões futuras o projeto poderá receber:

* interface com balões de conversa;
* CSS separado;
* FXML;
* padrão MVC;
* separação da comunicação com a Groq em uma classe `Service`;
* variável de ambiente para armazenar a API Key;
* botão para limpar conversa;
* histórico de conversas;
* seleção de modelos;
* indicador de carregamento;
* tratamento avançado de erros;
* Markdown nas respostas;
* streaming das respostas da IA;
* persistência das conversas em banco de dados.

## Autor

Projeto desenvolvido para fins de aprendizado de **JavaFX, Java 21 e integração com Inteligência Artificial**.
