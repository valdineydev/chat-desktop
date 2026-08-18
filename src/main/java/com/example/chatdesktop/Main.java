package com.example.chatdesktop;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class Main extends Application {

    // =========================================
    // CONFIGURAÇÕES DA GROQ
    // =========================================

    private static final String API_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    private static final String API_KEY =
            System.getenv("GROQ_API_KEY");


    private static final String MODEL =
            "openai/gpt-oss-20b";


    // =========================================
    // OBJETOS DA INTERFACE
    // =========================================

    private TextArea areaChat;
    private TextField campoMensagem;
    private Button botaoEnviar;


    // =========================================
    // HTTP
    // =========================================

    private final HttpClient httpClient =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(20))
                    .build();


    // =========================================
    // JSON
    // =========================================

    private final Gson gson = new Gson();


    // =========================================
    // HISTÓRICO DA CONVERSA
    // =========================================

    private final JsonArray historico = new JsonArray();


    @Override
    public void start(Stage stage) {

        // =====================================
        // MENSAGEM SYSTEM
        // =====================================

        adicionarMensagemHistorico(
                "system",
                "Você é um assistente útil, educado e objetivo. " +
                        "Responda sempre em português do Brasil."
        );


        // =====================================
        // TÍTULO
        // =====================================

        Label titulo = new Label("Chat com IA");

        titulo.setStyle(
                "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;"
        );


        Label modelo = new Label(
                "Groq • " + MODEL
        );

        modelo.setStyle(
                "-fx-text-fill: #666666;"
        );


        VBox topo = new VBox(
                5,
                titulo,
                modelo
        );

        topo.setPadding(
                new Insets(20)
        );


        // =====================================
        // ÁREA DO CHAT
        // =====================================

        areaChat = new TextArea();

        areaChat.setEditable(false);

        areaChat.setWrapText(true);

        areaChat.setPromptText(
                "A conversa aparecerá aqui..."
        );

        areaChat.setStyle(
                "-fx-font-size: 14px;"
        );


        // =====================================
        // CAMPO DE MENSAGEM
        // =====================================

        campoMensagem = new TextField();

        campoMensagem.setPromptText(
                "Digite sua mensagem..."
        );


        // =====================================
        // BOTÃO
        // =====================================

        botaoEnviar = new Button("Enviar");

        botaoEnviar.setPrefWidth(100);


        // =====================================
        // AÇÃO DO BOTÃO
        // =====================================

        botaoEnviar.setOnAction(event ->
                enviarMensagem()
        );


        // =====================================
        // ENTER TAMBÉM ENVIA
        // =====================================

        campoMensagem.setOnAction(event ->
                enviarMensagem()
        );


        // =====================================
        // PARTE INFERIOR
        // =====================================

        HBox inferior = new HBox(
                10,
                campoMensagem,
                botaoEnviar
        );

        inferior.setPadding(
                new Insets(15)
        );


        HBox.setHgrow(
                campoMensagem,
                javafx.scene.layout.Priority.ALWAYS
        );


        // =====================================
        // LAYOUT PRINCIPAL
        // =====================================

        BorderPane root = new BorderPane();

        root.setTop(topo);

        root.setCenter(areaChat);

        root.setBottom(inferior);


        // =====================================
        // CENA
        // =====================================

        Scene scene = new Scene(
                root,
                700,
                600
        );


        // =====================================
        // STAGE
        // =====================================

        stage.setTitle(
                "Chat JavaFX + Groq"
        );

        stage.setScene(scene);

        stage.show();


        campoMensagem.requestFocus();
    }


    // =========================================
    // ENVIAR MENSAGEM
    // =========================================

    private void enviarMensagem() {

        String mensagem =
                campoMensagem
                        .getText()
                        .trim();


        if (mensagem.isEmpty()) {
            return;
        }


        // Limpa o campo
        campoMensagem.clear();


        // Mostra mensagem do usuário
        areaChat.appendText(
                "Você:\n" +
                        mensagem +
                        "\n\n"
        );


        // Adiciona ao histórico
        adicionarMensagemHistorico(
                "user",
                mensagem
        );


        // Bloqueia botão enquanto aguarda
        botaoEnviar.setDisable(true);

        campoMensagem.setDisable(true);


        areaChat.appendText(
                "IA está respondendo...\n\n"
        );


        // Chama API
        chamarGroq();
    }


    // =========================================
    // CHAMADA PARA GROQ
    // =========================================

    private void chamarGroq() {

        try {

            // =================================
            // JSON DA REQUISIÇÃO
            // =================================

            JsonObject jsonBody =
                    new JsonObject();


            jsonBody.addProperty(
                    "model",
                    MODEL
            );


            jsonBody.add(
                    "messages",
                    historico.deepCopy()
            );


            String body =
                    gson.toJson(jsonBody);


            // =================================
            // REQUEST
            // =================================

            HttpRequest request =
                    HttpRequest
                            .newBuilder()
                            .uri(
                                    URI.create(API_URL)
                            )
                            .timeout(
                                    Duration.ofSeconds(60)
                            )
                            .header(
                                    "Authorization",
                                    "Bearer " + API_KEY
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest
                                            .BodyPublishers
                                            .ofString(body)
                            )
                            .build();


            // =================================
            // ENVIA ASSÍNCRONO
            // =================================

            httpClient
                    .sendAsync(
                            request,
                            HttpResponse
                                    .BodyHandlers
                                    .ofString()
                    )
                    .thenAccept(
                            this::processarResposta
                    )
                    .exceptionally(erro -> {

                        Platform.runLater(() -> {

                            removerMensagemCarregamento();

                            areaChat.appendText(
                                    "Erro ao conectar com a Groq:\n"
                                            + erro.getMessage()
                                            + "\n\n"
                            );


                            liberarInterface();

                        });


                        return null;
                    });


        } catch (Exception erro) {

            removerMensagemCarregamento();


            areaChat.appendText(
                    "Erro:\n" +
                            erro.getMessage() +
                            "\n\n"
            );


            liberarInterface();
        }
    }


    // =========================================
    // PROCESSAR RESPOSTA
    // =========================================

    private void processarResposta(
            HttpResponse<String> response
    ) {

        Platform.runLater(() -> {

            removerMensagemCarregamento();


            try {

                // =================================
                // VERIFICA STATUS HTTP
                // =================================

                if (response.statusCode() != 200) {

                    areaChat.appendText(
                            "Erro da API Groq\n"
                    );

                    areaChat.appendText(
                            "Status: "
                                    + response.statusCode()
                                    + "\n"
                    );

                    areaChat.appendText(
                            response.body()
                                    + "\n\n"
                    );


                    liberarInterface();

                    return;
                }


                // =================================
                // CONVERTE JSON
                // =================================

                JsonObject jsonResposta =
                        JsonParser
                                .parseString(
                                        response.body()
                                )
                                .getAsJsonObject();


                // =================================
                // PEGA CHOICES
                // =================================

                JsonArray choices =
                        jsonResposta
                                .getAsJsonArray(
                                        "choices"
                                );


                // =================================
                // PRIMEIRA RESPOSTA
                // =================================

                JsonObject primeiraResposta =
                        choices
                                .get(0)
                                .getAsJsonObject();


                // =================================
                // MESSAGE
                // =================================

                JsonObject message =
                        primeiraResposta
                                .getAsJsonObject(
                                        "message"
                                );


                // =================================
                // CONTENT
                // =================================

                String respostaIA =
                        message
                                .get(
                                        "content"
                                )
                                .getAsString();


                // =================================
                // MOSTRA NA TELA
                // =================================

                areaChat.appendText(
                        "IA:\n"
                                + respostaIA
                                + "\n\n"
                );


                // =================================
                // ADICIONA RESPOSTA AO HISTÓRICO
                // =================================

                adicionarMensagemHistorico(
                        "assistant",
                        respostaIA
                );


            } catch (Exception erro) {

                areaChat.appendText(
                        "Erro ao interpretar resposta:\n"
                                + erro.getMessage()
                                + "\n\n"
                );

            } finally {

                liberarInterface();
            }
        });
    }


    // =========================================
    // ADICIONAR AO HISTÓRICO
    // =========================================

    private void adicionarMensagemHistorico(
            String role,
            String content
    ) {

        JsonObject mensagem =
                new JsonObject();


        mensagem.addProperty(
                "role",
                role
        );


        mensagem.addProperty(
                "content",
                content
        );


        historico.add(
                mensagem
        );
    }


    // =========================================
    // REMOVE "IA ESTÁ RESPONDENDO"
    // =========================================

    private void removerMensagemCarregamento() {

        String texto =
                areaChat.getText();


        String carregando =
                "IA está respondendo...\n\n";


        if (texto.endsWith(carregando)) {

            areaChat.setText(
                    texto.substring(
                            0,
                            texto.length()
                                    - carregando.length()
                    )
            );
        }
    }


    // =========================================
    // LIBERAR INTERFACE
    // =========================================

    private void liberarInterface() {

        botaoEnviar.setDisable(false);

        campoMensagem.setDisable(false);

        campoMensagem.requestFocus();
    }


    // =========================================
    // MAIN
    // =========================================

    public static void main(String[] args) {

        launch(args);

    }

}