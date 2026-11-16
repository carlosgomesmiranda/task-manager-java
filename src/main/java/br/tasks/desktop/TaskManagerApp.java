package br.tasks.desktop;

import br.tasks.desktop.api.ApiClient;
import br.tasks.desktop.session.Session;
import br.tasks.desktop.ui.AsyncExecutor;
import br.tasks.desktop.ui.LoginView;
import br.tasks.desktop.ui.TarefasView;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Aplicação JavaFX de gerenciamento de tarefas.
 *
 * <p>A aplicação não acessa o banco diretamente: todas as operações são feitas
 * pela API REST através do {@link ApiClient}.</p>
 */
public class TaskManagerApp extends Application {

    private static final String CSS = "/desktop/desktop.css";

    private final Session session = new Session();
    private ApiClient apiClient;
    private AsyncExecutor executor;
    private Stage stage;

    @Override
    public void start(final Stage palco) {
        this.stage = palco;
        this.apiClient = new ApiClient(session);
        this.executor = new AsyncExecutor();

        palco.setTitle("TaskManager");
        palco.setMinWidth(920);
        palco.setMinHeight(620);

        mostrarLogin();
        palco.show();
    }

    /**
     * Exibe a tela de login.
     */
    public void mostrarLogin() {
        LoginView login = new LoginView(apiClient, executor, this::mostrarTarefas);
        stage.setScene(criarCena(login, 1000, 680));
        stage.setTitle("TaskManager · Login");
        stage.centerOnScreen();
    }

    /**
     * Exibe a tela de tarefas do usuário autenticado.
     */
    public void mostrarTarefas() {
        if (!session.isAutenticado()) {
            mostrarLogin();
            return;
        }

        TarefasView tarefas = new TarefasView(apiClient, executor, session, this::mostrarLogin);
        stage.setScene(criarCena(tarefas, 1180, 760));
        stage.setTitle("TaskManager · Minhas tarefas");
    }

    private Scene criarCena(final Parent raiz, final double largura, final double altura) {
        Scene cena = new Scene(raiz, largura, altura);
        URL css = getClass().getResource(CSS);
        if (css != null) {
            cena.getStylesheets().add(css.toExternalForm());
        }
        return cena;
    }

    @Override
    public void stop() {
        if (executor != null) {
            executor.shutdown();
        }
    }
}
