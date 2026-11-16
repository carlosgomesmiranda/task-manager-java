package br.tasks.desktop.ui;

import br.tasks.desktop.model.Prioridade;
import br.tasks.desktop.model.TarefaRequest;
import br.tasks.desktop.model.TarefaResponse;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Diálogo de criação/edição de tarefa.
 */
public final class TarefaDialog extends Dialog<TarefaRequest> {

    private static final int LIMITE_DESCRICAO = 255;

    private final TextArea descricao = new TextArea();
    private final ComboBox<Prioridade> prioridade = new ComboBox<>();
    private final CheckBox concluida = new CheckBox("Tarefa concluída");
    private final Label contador = new Label();
    private final Label mensagemErro = new Label();

    /**
     * Cria o diálogo. Quando {@code tarefa} é nula, o modo é de criação.
     *
     * @param tarefa tarefa a editar ou {@code null}
     */
    public TarefaDialog(final TarefaResponse tarefa) {
        boolean edicao = tarefa != null;
        setTitle(edicao ? "Editar tarefa" : "Nova tarefa");
        setHeaderText(edicao
                ? "Atualize as informações da tarefa"
                : "Descreva a tarefa que deseja acompanhar");

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        getDialogPane().setContent(montarFormulario());
        getDialogPane().setPrefWidth(540);
        Dialogs.aplicarEstilo(getDialogPane());

        prioridade.getItems().setAll(Prioridade.values());
        prioridade.getSelectionModel().select(Prioridade.MEDIA);

        if (edicao) {
            descricao.setText(tarefa.descricao());
            prioridade.getSelectionModel().select(
                    tarefa.prioridade() == null ? Prioridade.MEDIA : tarefa.prioridade());
            concluida.setSelected(tarefa.concluida());
            atualizarContador();
        }

        Button confirmar = (Button) getDialogPane().lookupButton(ButtonType.OK);
        confirmar.setText(edicao ? "Salvar" : "Criar");
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
                ? new TarefaRequest(descricao.getText().trim(), prioridade.getValue(),
                        concluida.isSelected())
                : null);

        setOnShown(evento -> {
            descricao.requestFocus();
            descricao.positionCaret(descricao.getText().length());
        });
    }

    private GridPane montarFormulario() {
        descricao.setPromptText("Ex.: Revisar o relatório mensal");
        descricao.setWrapText(true);
        descricao.setPrefRowCount(4);
        descricao.textProperty().addListener((observavel, antigo, novo) -> atualizarContador());

        contador.getStyleClass().add("hint-label");
        mensagemErro.getStyleClass().add("error-label");
        mensagemErro.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 4, 4, 4));

        grid.add(criarRotulo("Descrição"), 0, 0);
        grid.add(descricao, 1, 0);
        grid.add(contador, 1, 1);
        grid.add(criarRotulo("Prioridade"), 0, 2);
        grid.add(prioridade, 1, 2);
        grid.add(concluida, 1, 3);
        grid.add(mensagemErro, 0, 4, 2, 1);

        GridPane.setHgrow(descricao, Priority.ALWAYS);
        GridPane.setHgrow(prioridade, Priority.ALWAYS);
        prioridade.setMaxWidth(Double.MAX_VALUE);

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

    private void atualizarContador() {
        int tamanho = descricao.getText() == null ? 0 : descricao.getText().length();
        contador.setText(tamanho + "/" + LIMITE_DESCRICAO + " caracteres");
        contador.getStyleClass().setAll("hint-label");
        if (tamanho > LIMITE_DESCRICAO) {
            contador.getStyleClass().setAll("error-label");
        }
    }

    private String validar() {
        String texto = descricao.getText() == null ? "" : descricao.getText().trim();
        if (texto.isEmpty()) {
            return "A descrição é obrigatória.";
        }
        if (texto.length() > LIMITE_DESCRICAO) {
            return "A descrição deve ter no máximo " + LIMITE_DESCRICAO + " caracteres.";
        }
        if (prioridade.getValue() == null) {
            return "Selecione uma prioridade.";
        }
        return null;
    }
}
