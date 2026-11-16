package br.tasks.desktop.ui;

import br.tasks.desktop.model.RegisterRequest;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Diálogo de criação de conta.
 */
public final class RegistrationDialog extends Dialog<RegisterRequest> {

    private final TextField nome = new TextField();
    private final TextField email = new TextField();
    private final PasswordField senha = new PasswordField();
    private final PasswordField confirmacao = new PasswordField();
    private final Label mensagemErro = new Label();

    /**
     * Cria o diálogo.
     */
    public RegistrationDialog() {
        setTitle("Criar conta");
        setHeaderText("Cadastre-se para começar a usar o TaskManager");

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        getDialogPane().setContent(montarFormulario());
        getDialogPane().setPrefWidth(460);
        Dialogs.aplicarEstilo(getDialogPane());

        Button confirmar = (Button) getDialogPane().lookupButton(ButtonType.OK);
        confirmar.setText("Cadastrar");
        confirmar.getStyleClass().add("primary-button");
        confirmar.addEventFilter(ActionEvent.ACTION, evento -> {
            String erro = validar();
            if (erro != null) {
                mensagemErro.setText(erro);
                evento.consume();
            }
        });

        Button cancelar = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelar.setText("Cancelar");
        cancelar.getStyleClass().add("secondary-button");

        setResultConverter(botao -> botao == ButtonType.OK
                ? new RegisterRequest(nome.getText().trim(), email.getText().trim(),
                        senha.getText())
                : null);

        setOnShown(evento -> nome.requestFocus());
    }

    private GridPane montarFormulario() {
        nome.setPromptText("Seu nome completo");
        email.setPromptText("voce@exemplo.com");
        senha.setPromptText("Mínimo de 6 caracteres");
        confirmacao.setPromptText("Repita a senha");

        mensagemErro.getStyleClass().add("error-label");
        mensagemErro.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 4, 4, 4));

        grid.add(criarRotulo("Nome"), 0, 0);
        grid.add(nome, 1, 0);
        grid.add(criarRotulo("E-mail"), 0, 1);
        grid.add(email, 1, 1);
        grid.add(criarRotulo("Senha"), 0, 2);
        grid.add(senha, 1, 2);
        grid.add(criarRotulo("Confirmação"), 0, 3);
        grid.add(confirmacao, 1, 3);
        grid.add(mensagemErro, 0, 4, 2, 1);

        GridPane.setHgrow(nome, Priority.ALWAYS);
        GridPane.setHgrow(email, Priority.ALWAYS);
        GridPane.setHgrow(senha, Priority.ALWAYS);
        GridPane.setHgrow(confirmacao, Priority.ALWAYS);

        ColumnConstraints colunaRotulos = new ColumnConstraints();
        ColumnConstraints colunaCampos = new ColumnConstraints();
        colunaCampos.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(colunaRotulos, colunaCampos);

        return grid;
    }

    private Label criarRotulo(final String texto) {
        Label rotulo = new Label(texto);
        rotulo.getStyleClass().add("field-label");
        return rotulo;
    }

    private String validar() {
        String nomeValor = nome.getText() == null ? "" : nome.getText().trim();
        String emailValor = email.getText() == null ? "" : email.getText().trim();
        String senhaValor = senha.getText() == null ? "" : senha.getText();

        if (nomeValor.length() < 3) {
            return "Informe um nome com pelo menos 3 caracteres.";
        }
        if (!emailValor.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return "Informe um e-mail válido.";
        }
        if (senhaValor.length() < 6 || senhaValor.length() > 72) {
            return "A senha deve ter entre 6 e 72 caracteres.";
        }
        if (!senhaValor.equals(confirmacao.getText())) {
            return "As senhas não conferem.";
        }
        return null;
    }
}
