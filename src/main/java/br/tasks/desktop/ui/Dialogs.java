package br.tasks.desktop.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.stage.Window;

import java.net.URL;

/**
 * Diálogos padronizados do cliente desktop.
 */
public final class Dialogs {

    private static final String CSS = "/desktop/desktop.css";

    private Dialogs() {
    }

    /**
     * Exibe um alerta de erro.
     *
     * @param dono     janela proprietária (pode ser nula)
     * @param titulo   título
     * @param mensagem mensagem
     */
    public static void erro(final Window dono, final String titulo, final String mensagem) {
        mostrar(Alert.AlertType.ERROR, dono, titulo, mensagem);
    }

    /**
     * Exibe um alerta informativo.
     *
     * @param dono     janela proprietária (pode ser nula)
     * @param titulo   título
     * @param mensagem mensagem
     */
    public static void info(final Window dono, final String titulo, final String mensagem) {
        mostrar(Alert.AlertType.INFORMATION, dono, titulo, mensagem);
    }

    /**
     * Solicita confirmação do usuário.
     *
     * @param dono     janela proprietária (pode ser nula)
     * @param titulo   título
     * @param mensagem mensagem
     * @return {@code true} quando o usuário confirma
     */
    public static boolean confirmar(final Window dono, final String titulo, final String mensagem) {
        Alert alerta = criar(Alert.AlertType.CONFIRMATION, dono, titulo, mensagem);
        alerta.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        return alerta.showAndWait()
                .filter(botao -> botao == ButtonType.YES)
                .isPresent();
    }

    private static void mostrar(final Alert.AlertType tipo, final Window dono,
                                final String titulo, final String mensagem) {
        criar(tipo, dono, titulo, mensagem).showAndWait();
    }

    private static Alert criar(final Alert.AlertType tipo, final Window dono,
                               final String titulo, final String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);

        Label conteudo = new Label(mensagem);
        conteudo.setWrapText(true);
        conteudo.setMaxWidth(520);
        alerta.getDialogPane().setContent(conteudo);

        if (dono != null) {
            alerta.initOwner(dono);
        }

        aplicarEstilo(alerta.getDialogPane());
        return alerta;
    }

    /**
     * Aplica a folha de estilos em um painel de diálogo.
     *
     * @param painel painel do diálogo
     */
    public static void aplicarEstilo(final DialogPane painel) {
        URL css = Dialogs.class.getResource(CSS);
        if (css != null) {
            painel.getStylesheets().add(css.toExternalForm());
        }
        painel.getStyleClass().add("app-dialog");
    }
}
