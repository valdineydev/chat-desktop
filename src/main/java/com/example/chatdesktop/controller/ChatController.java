package com.example.chatdesktop.controller;

import com.example.chatdesktop.model.ChatMessage;
import com.example.chatdesktop.service.GroqService;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class ChatController {

    // =========================================
    // COMPONENTES DA TELA
    // =========================================

    @FXML
    private TextArea areaChat;

    @FXML
    private TextField campoMensagem;

    @FXML
    private Button botaoEnviar;

    @FXML
    private Button botaoCopiar;


    // =========================================
    // SERVICE
    // =========================================

    private GroqService groqService;


    // =========================================
    // HISTÓRICO DA CONVERSA
    // =========================================

    private List<ChatMessage> historico;


    // =========================================
    // ÚLTIMA RESPOSTA DA IA
    // =========================================

    private String ultimaResposta;


    // =========================================
    // INITIALIZE
    // =========================================

    @FXML
    public void initialize() {

        // Cria o serviço responsável
        // pela comunicação com a Groq
        groqService = new GroqService();


        // Cria a lista responsável
        // pelo histórico da conversa
        historico = new ArrayList<>();


        // Inicializa o histórico
        iniciarHistorico();


        // Botão copiar começa desabilitado
        botaoCopiar.setDisable(true);


        // Coloca o cursor no campo de mensagem
        Platform.runLater(() ->
                campoMensagem.requestFocus()
        );
    }


    // =========================================
    // INICIAR HISTÓRICO
    // =========================================

    private void iniciarHistorico() {

        historico.clear();


        // Mensagem de configuração da IA
        historico.add(
                new ChatMessage(
                        "system",
                        "Você é um assistente útil, educado e objetivo. " +
                                "Responda sempre em português do Brasil."
                )
        );
    }


    // =========================================
    // ENVIAR MENSAGEM
    // =========================================

    @FXML
    private void enviarMensagem() {

        // Recupera a mensagem digitada
        String mensagem =
                campoMensagem
                        .getText()
                        .trim();


        // Impede envio de mensagem vazia
        if (mensagem.isEmpty()) {
            return;
        }


        // Limpa o campo
        campoMensagem.clear();


        // Mostra a mensagem na tela
        areaChat.appendText(
                "Você:\n"
                        + mensagem
                        + "\n\n"
        );


        // Adiciona a mensagem ao histórico
        historico.add(
                new ChatMessage(
                        "user",
                        mensagem
                )
        );


        // Bloqueia enquanto aguarda
        bloquearInterface();


        // Mostra carregamento
        areaChat.appendText(
                "IA está respondendo...\n\n"
        );


        try {

            // Envia o histórico para a Groq
            groqService
                    .enviarMensagem(historico)
                    .thenAccept(
                            this::receberResposta
                    )
                    .exceptionally(
                            this::tratarErro
                    );

        } catch (Exception erro) {

            // Captura também erros que possam
            // acontecer antes da chamada assíncrona
            tratarErro(erro);
        }
    }


    // =========================================
    // RECEBER RESPOSTA DA IA
    // =========================================

    private void receberResposta(
            String resposta
    ) {

        Platform.runLater(() -> {

            // Remove "IA está respondendo..."
            removerMensagemCarregamento();


            // Mostra resposta
            areaChat.appendText(
                    "IA:\n"
                            + resposta
                            + "\n\n"
            );


            // Guarda resposta no histórico
            historico.add(
                    new ChatMessage(
                            "assistant",
                            resposta
                    )
            );


            // Guarda a última resposta
            // para o botão copiar
            ultimaResposta = resposta;


            // Agora existe uma resposta
            // que pode ser copiada
            botaoCopiar.setDisable(false);


            // Libera a interface
            liberarInterface();
        });
    }


    // =========================================
    // TRATAR ERROS
    // =========================================

    private Void tratarErro(
            Throwable erro
    ) {

        Platform.runLater(() -> {

            removerMensagemCarregamento();


            String mensagemErro =
                    erro.getMessage();


            // Algumas chamadas assíncronas
            // encapsulam o erro original
            if (erro.getCause() != null &&
                    erro.getCause().getMessage() != null) {

                mensagemErro =
                        erro.getCause().getMessage();
            }


            if (mensagemErro == null ||
                    mensagemErro.isBlank()) {

                mensagemErro =
                        "Ocorreu um erro inesperado.";
            }


            areaChat.appendText(
                    "Erro:\n"
                            + mensagemErro
                            + "\n\n"
            );


            liberarInterface();
        });


        return null;
    }


    // =========================================
    // NOVA CONVERSA
    // =========================================

    @FXML
    private void novaConversa() {

        // Reinicia o histórico
        iniciarHistorico();


        // Limpa a tela
        areaChat.clear();


        // Limpa o campo de mensagem
        campoMensagem.clear();


        // Remove a última resposta
        ultimaResposta = null;


        // Desabilita botão copiar
        botaoCopiar.setDisable(true);


        // Volta o texto original,
        // caso esteja mostrando "Copiado!"
        botaoCopiar.setText(
                "Copiar resposta"
        );


        // Libera interface
        liberarInterface();
    }


    // =========================================
    // COPIAR RESPOSTA
    // =========================================

    @FXML
    private void copiarResposta() {

        // Verifica se existe resposta
        if (ultimaResposta == null ||
                ultimaResposta.isBlank()) {

            return;
        }


        // Área de transferência do Windows
        Clipboard clipboard =
                Clipboard.getSystemClipboard();


        // Conteúdo que será copiado
        ClipboardContent content =
                new ClipboardContent();


        content.putString(
                ultimaResposta
        );


        // Copia
        clipboard.setContent(
                content
        );


        // Feedback visual
        botaoCopiar.setText(
                "Copiado!"
        );


        // Depois de 2 segundos
        // volta para o texto original
        PauseTransition pause =
                new PauseTransition(
                        Duration.seconds(2)
                );


        pause.setOnFinished(event ->

                botaoCopiar.setText(
                        "Copiar resposta"
                )

        );


        pause.play();
    }


    // =========================================
    // REMOVER MENSAGEM DE CARREGAMENTO
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


            // Coloca o cursor no final
            areaChat.positionCaret(
                    areaChat.getText().length()
            );
        }
    }


    // =========================================
    // BLOQUEAR INTERFACE
    // =========================================

    private void bloquearInterface() {

        campoMensagem.setDisable(true);

        botaoEnviar.setDisable(true);
    }


    // =========================================
    // LIBERAR INTERFACE
    // =========================================

    private void liberarInterface() {

        campoMensagem.setDisable(false);

        botaoEnviar.setDisable(false);

        campoMensagem.requestFocus();
    }
}