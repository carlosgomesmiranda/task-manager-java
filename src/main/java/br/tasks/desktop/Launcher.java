package br.tasks.desktop;

import javafx.application.Application;

/**
 * Ponto de entrada do cliente desktop.
 *
 * <p>Esta classe não estende {@link Application} de propósito: assim o cliente
 * pode ser iniciado tanto pelo {@code javafx-maven-plugin} quanto diretamente
 * pela IDE, sem o erro "JavaFX runtime components are missing".</p>
 */
public final class Launcher {

    private Launcher() {
    }

    /**
     * Inicia a aplicação JavaFX.
     *
     * @param args argumentos de linha de comando
     */
    public static void main(final String[] args) {
        Application.launch(TaskManagerApp.class, args);
    }
}
