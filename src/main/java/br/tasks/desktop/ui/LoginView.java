package br.tasks.desktop.ui;

import br.tasks.desktop.api.ApiClient;
import br.tasks.desktop.api.ApiException;
import br.tasks.desktop.model.LoginRequest;
import br.tasks.desktop.model.RegisterRequest;
import br.tasks.desktop.model.UsuarioResponse;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Optional;

/**
 * Tela de login e cadastro. O token JWT é armazenado apenas em memória pelo
 * {@link ApiClient} através da sessão.
 */
public final class LoginView extends BorderPane {

    private final ApiClient apiClient;
    private final AsyncExecutor executor;
    private final Runnable aoAutenticar;

    private final TextField email = new TextField();
    private final PasswordField senha = new PasswordField();
    private final Button entrar = new Button("Entrar");
    private final Button criarConta = new Button("Criar conta");
    private final ProgressIndicator progresso = new ProgressIndicator();
    private final Label feedback = new Label();

    /**
     * Cria a tela de login.
     *
     * @param apiClientParam cliente da API
     * @param executorParam  executor assíncrono
     * @param aoAutenticarParam callback executado após o login
     */
    public LoginView(final ApiClient apiClientParam, final AsyncExecutor executorParam,
                     final Runnable aoAutenticarParam) {
        this.apiClient = apiClientParam;
        this.executor = executorParam;
        this.aoAutenticar = aoAutenticarParam;

        getStyleClass().add("login-root");
        setCenter(montarCard());
        setAlignment(getCenter(), Pos.CENTER);
    }

    private VBox montarCard() {
        Label marca = new Label("TaskManager");
        marca.getStyleClass().add("brand-title");

        Label subtitulo = new Label("Organize suas tarefas com prioridade, foco e segurança.");
        subtitulo.getStyleClass().add("brand-subtitle");
        subtitulo.setWrapText(true);

        Label rotuloEmail = new Label("E-mail");
        rotuloEmail.getStyleClass().add("field-label");
        email.setPromptText("voce@exemplo.com");

        Label rotuloSenha = new Label("Senha");
        rotuloSenha.getStyleClass().add("field-label");
        senha.setPromptText("Sua senha");

        entrar.setDefaultButton(true);
        entrar.getStyleClass().add("primary-button");
        entrar.setMaxWidth(Double.MAX_VALUE);
        entrar.setOnAction(evento -> autenticar());

        criarConta.getStyleClass().add("link-button");
        criarConta.setOnAction(evento -> abrirCadastro());

        progresso.setPrefSize(20, 20);
        progresso.setVisible(false);
        progresso.managedProperty().bind(progresso.visibleProperty());

        HBox acoes = new HBox(10, entrar, progresso);
        acoes.setAlignment(Pos.CENTER_LEFT);

        feedback.getStyleClass().add("error-label");
        feedback.setWrapText(true);

        Label dicaApi = new Label("API: " + apiClient.getBaseUrl());
        dicaApi.getStyleClass().add("hint-label");

        VBox card = new VBox(10,
                marca, subtitulo,
                rotuloEmail, email,
                rotuloSenha, senha,
                acoes, feedback,
                new HBox(criarConta),
                dicaApi
        );
        card.getStyleClass().add("login-card");
        card.setMaxWidth(430);
        card.setPrefWidth(430);

        return card;
    }

    private void autenticar() {
        String emailValor = email.getText() == null ? "" : email.getText().trim();
        String senhaValor = senha.getText() == null ? "" : senha.getText();

        if (emailValor.isEmpty() || senhaValor.isEmpty()) {
            mostrarFeedback("Informe e-mail e senha.", false);
            return;
        }

        setCarregando(true);
        executor.run(
                () -> apiClient.login(new LoginRequest(emailValor, senhaValor)),
                resposta -> {
                    setCarregando(false);
                    mostrarFeedback("", true);
                    aoAutenticar.run();
                },
                erro -> {
                    setCarregando(false);
                    senha.clear();
                    mostrarFeedback(mensagem(erro), false);
                    senha.requestFocus();
                }
        );
    }

    private void abrirCadastro() {
        RegistrationDialog dialogo = new RegistrationDialog();
        Optional<RegisterRequest> resultado = dialogo.showAndWait();

        resultado.ifPresent(request -> {
            setCarregando(true);
            executor.run(
                    () -> apiClient.registrar(request),
                    (UsuarioResponse usuario) -> {
                        setCarregando(false);
                        email.setText(usuario.email());
                        senha.clear();
                        mostrarFeedback("Conta criada com sucesso! Agora faça login.", true);
                        senha.requestFocus();
                    },
                    erro -> {
                        setCarregando(false);
                        mostrarFeedback(mensagem(erro), false);
                    }
            );
        });
    }

    private void setCarregando(final boolean carregando) {
        email.setDisable(carregando);
        senha.setDisable(carregando);
        entrar.setDisable(carregando);
        criarConta.setDisable(carregando);
        progresso.setVisible(carregando);
    }

    private void mostrarFeedback(final String mensagem, final boolean sucesso) {
        feedback.setText(mensagem);
        feedback.getStyleClass().setAll(sucesso ? "success-label" : "error-label");
    }

    private String mensagem(final Throwable erro) {
        if (erro instanceof ApiException apiException) {
            return apiException.getMensagemCompleta();
        }
        String detalhe = erro.getMessage();
        return detalhe == null || detalhe.isBlank()
                ? "Não foi possível concluir a operação."
                : detalhe;
    }
}
