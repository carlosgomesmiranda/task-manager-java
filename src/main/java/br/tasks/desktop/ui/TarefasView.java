package br.tasks.desktop.ui;

import br.tasks.desktop.api.ApiClient;
import br.tasks.desktop.api.ApiException;
import br.tasks.desktop.model.TarefaRequest;
import br.tasks.desktop.model.TarefaResponse;
import br.tasks.desktop.model.UsuarioResponse;
import br.tasks.desktop.session.Session;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.List;

/**
 * Tela principal de tarefas do usuário autenticado.
 */
public final class TarefasView extends BorderPane {

    private final ApiClient apiClient;
    private final AsyncExecutor executor;
    private final Session session;
    private final Runnable aoSair;

    private final ObservableList<TarefaResponse> tarefas = FXCollections.observableArrayList();
    private final TableView<TarefaResponse> tabela = new TableView<>();
    private final ComboBox<Filtro> filtro = new ComboBox<>();
    private final Label status = new Label("Carregando tarefas...");

    private final Button nova = new Button("+ Nova tarefa");
    private final Button editar = new Button("Editar");
    private final Button alternar = new Button("Concluir");
    private final Button excluir = new Button("Excluir");
    private final Button atualizar = new Button("Atualizar");
    private final Button sair = new Button("Sair");

    private boolean ocupado;

    /**
     * Cria a tela de tarefas.
     *
     * @param apiClientParam cliente da API
     * @param executorParam  executor assíncrono
     * @param sessionParam   sessão autenticada
     * @param aoSairParam    callback de logout
     */
    public TarefasView(final ApiClient apiClientParam, final AsyncExecutor executorParam,
                       final Session sessionParam, final Runnable aoSairParam) {
        this.apiClient = apiClientParam;
        this.executor = executorParam;
        this.session = sessionParam;
        this.aoSair = aoSairParam;

        getStyleClass().add("tasks-root");
        setTop(montarCabecalho());
        setCenter(montarCentro());
        setBottom(montarRodape());

        configurarTabela();
        atualizarBotoes();
        carregar();
    }

    private HBox montarCabecalho() {
        Label titulo = new Label("Minhas tarefas");
        titulo.getStyleClass().add("page-title");

        Label subtitulo = new Label("Você visualiza e altera somente as tarefas da sua conta.");
        subtitulo.getStyleClass().add("page-subtitle");

        VBox textos = new VBox(2, titulo, subtitulo);

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        UsuarioResponse usuario = session.getUsuario();
        Label identificacao = new Label(usuario == null
                ? "Usuário autenticado"
                : usuario.nome() + "  ·  " + usuario.email());
        identificacao.getStyleClass().add("user-label");

        sair.getStyleClass().add("secondary-button");
        sair.setOnAction(evento -> sair());

        HBox cabecalho = new HBox(12, textos, espaco, identificacao, sair);
        cabecalho.setAlignment(Pos.CENTER_LEFT);
        cabecalho.getStyleClass().add("header");
        return cabecalho;
    }

    private VBox montarCentro() {
        Label rotuloFiltro = new Label("Filtrar:");
        rotuloFiltro.getStyleClass().add("field-label");

        filtro.getItems().setAll(Filtro.values());
        filtro.getSelectionModel().select(Filtro.TODAS);
        filtro.setPrefWidth(150);
        filtro.valueProperty().addListener((observavel, antigo, novo) -> {
            if (novo != null && !ocupado) {
                carregar();
            }
        });

        atualizar.getStyleClass().add("secondary-button");
        atualizar.setOnAction(evento -> carregar());

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        HBox barra = new HBox(8, rotuloFiltro, filtro, espaco, atualizar);
        barra.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(12, barra, tabela);
        card.getStyleClass().add("table-card");
        VBox.setVgrow(tabela, Priority.ALWAYS);
        return card;
    }

    private HBox montarRodape() {
        nova.getStyleClass().add("primary-button");
        editar.getStyleClass().add("secondary-button");
        alternar.getStyleClass().add("secondary-button");
        excluir.getStyleClass().add("danger-button");

        nova.setOnAction(evento -> novaTarefa());
        editar.setOnAction(evento -> abrirEdicao(tabela.getSelectionModel().getSelectedItem()));
        alternar.setOnAction(evento -> alternarConclusao());
        excluir.setOnAction(evento -> excluirSelecionada());

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        status.getStyleClass().add("status-label");

        HBox rodape = new HBox(8, nova, editar, alternar, excluir, espaco, status);
        rodape.setAlignment(Pos.CENTER_LEFT);
        rodape.getStyleClass().add("footer");
        return rodape;
    }

    private void configurarTabela() {
        TableColumn<TarefaResponse, Long> colunaId = new TableColumn<>("ID");
        colunaId.setCellValueFactory(dados ->
                new ReadOnlyObjectWrapper<>(dados.getValue().id()));
        colunaId.setPrefWidth(70);
        colunaId.setMaxWidth(90);

        TableColumn<TarefaResponse, String> colunaDescricao = new TableColumn<>("Descrição");
        colunaDescricao.setCellValueFactory(dados ->
                new ReadOnlyStringWrapper(dados.getValue().descricao()));
        colunaDescricao.setPrefWidth(560);

        TableColumn<TarefaResponse, String> colunaPrioridade = new TableColumn<>("Prioridade");
        colunaPrioridade.setCellValueFactory(dados -> new ReadOnlyStringWrapper(
                dados.getValue().prioridade() == null
                        ? ""
                        : dados.getValue().prioridade().getRotulo()));
        colunaPrioridade.setPrefWidth(120);

        TableColumn<TarefaResponse, String> colunaConcluida = new TableColumn<>("Concluída");
        colunaConcluida.setCellValueFactory(dados -> new ReadOnlyStringWrapper(
                dados.getValue().concluida() ? "Sim" : "Não"));
        colunaConcluida.setPrefWidth(110);
        colunaConcluida.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(final String item, final boolean vazio) {
                super.updateItem(item, vazio);
                getStyleClass().removeAll("status-done", "status-pending");
                if (vazio || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    getStyleClass().add("Sim".equals(item) ? "status-done" : "status-pending");
                }
            }
        });

        tabela.getColumns().add(colunaId);
        tabela.getColumns().add(colunaDescricao);
        tabela.getColumns().add(colunaPrioridade);
        tabela.getColumns().add(colunaConcluida);

        tabela.setItems(tarefas);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabela.setPlaceholder(new Label("Nenhuma tarefa encontrada para este filtro."));
        tabela.getSelectionModel().selectedItemProperty()
                .addListener((observavel, antigo, novo) -> atualizarBotoes());
        tabela.setRowFactory(vista -> {
            TableRow<TarefaResponse> linha = new TableRow<>();
            linha.setOnMouseClicked(evento -> {
                if (evento.getClickCount() == 2 && !linha.isEmpty() && !ocupado) {
                    abrirEdicao(linha.getItem());
                }
            });
            return linha;
        });
    }

    private void carregar() {
        if (!session.isAutenticado()) {
            aoSair.run();
            return;
        }

        setOcupado(true);
        status.setText("Carregando tarefas...");

        executor.run(
                () -> apiClient.listar(filtroSelecionado().concluida),
                this::exibirTarefas,
                erro -> {
                    setOcupado(false);
                    status.setText("Não foi possível carregar as tarefas.");
                    tratarErro(erro);
                }
        );
    }

    private void exibirTarefas(final List<TarefaResponse> lista) {
        tarefas.setAll(lista);
        setOcupado(false);
        status.setText(lista.size() == 1
                ? "1 tarefa encontrada."
                : lista.size() + " tarefas encontradas.");
    }

    private void novaTarefa() {
        if (ocupado) {
            return;
        }
        new TarefaDialog(null).showAndWait()
                .ifPresent(request -> salvar(null, request));
    }

    private void abrirEdicao(final TarefaResponse tarefa) {
        if (tarefa == null || ocupado) {
            return;
        }
        new TarefaDialog(tarefa).showAndWait()
                .ifPresent(request -> salvar(tarefa, request));
    }

    private void salvar(final TarefaResponse existente, final TarefaRequest request) {
        setOcupado(true);
        status.setText(existente == null ? "Criando tarefa..." : "Atualizando tarefa...");

        executor.run(
                () -> existente == null
                        ? apiClient.criar(request)
                        : apiClient.atualizar(existente.id(), request),
                tarefa -> {
                    setOcupado(false);
                    carregar();
                },
                erro -> {
                    setOcupado(false);
                    status.setText("Operação não concluída.");
                    tratarErro(erro);
                }
        );
    }

    private void alternarConclusao() {
        TarefaResponse selecionada = tabela.getSelectionModel().getSelectedItem();
        if (selecionada == null || ocupado) {
            return;
        }

        TarefaRequest request = new TarefaRequest(
                selecionada.descricao(),
                selecionada.prioridade(),
                !selecionada.concluida()
        );

        setOcupado(true);
        status.setText(selecionada.concluida() ? "Reabrindo tarefa..." : "Concluindo tarefa...");

        executor.run(
                () -> apiClient.atualizar(selecionada.id(), request),
                tarefa -> {
                    setOcupado(false);
                    carregar();
                },
                erro -> {
                    setOcupado(false);
                    status.setText("Operação não concluída.");
                    tratarErro(erro);
                }
        );
    }

    private void excluirSelecionada() {
        TarefaResponse selecionada = tabela.getSelectionModel().getSelectedItem();
        if (selecionada == null || ocupado) {
            return;
        }

        String descricao = selecionada.descricao();
        if (descricao.length() > 90) {
            descricao = descricao.substring(0, 90) + "...";
        }

        boolean confirmado = Dialogs.confirmar(janela(), "Excluir tarefa",
                "Deseja realmente excluir a tarefa \"" + descricao + "\"?");
        if (!confirmado) {
            return;
        }

        Long id = selecionada.id();
        setOcupado(true);
        status.setText("Excluindo tarefa...");

        executor.run(
                () -> {
                    apiClient.excluir(id);
                    return null;
                },
                ignorado -> {
                    setOcupado(false);
                    carregar();
                },
                erro -> {
                    setOcupado(false);
                    status.setText("Operação não concluída.");
                    tratarErro(erro);
                }
        );
    }

    private void sair() {
        if (ocupado) {
            return;
        }
        if (Dialogs.confirmar(janela(), "Sair", "Deseja encerrar a sessão?")) {
            session.encerrar();
            aoSair.run();
        }
    }

    private void setOcupado(final boolean valor) {
        this.ocupado = valor;
        tabela.setDisable(valor);
        filtro.setDisable(valor);
        nova.setDisable(valor);
        atualizar.setDisable(valor);
        sair.setDisable(valor);
        atualizarBotoes();
    }

    private void atualizarBotoes() {
        boolean semSelecao = tabela.getSelectionModel().getSelectedItem() == null;
        editar.setDisable(ocupado || semSelecao);
        excluir.setDisable(ocupado || semSelecao);
        alternar.setDisable(ocupado || semSelecao);

        TarefaResponse selecionada = tabela.getSelectionModel().getSelectedItem();
        alternar.setText(selecionada != null && selecionada.concluida() ? "Reabrir" : "Concluir");
    }

    private void tratarErro(final Throwable erro) {
        if (erro instanceof ApiException apiException && apiException.getStatus() == 401) {
            session.encerrar();
            Dialogs.erro(janela(), "Sessão expirada", apiException.getMensagemCompleta());
            aoSair.run();
            return;
        }

        String mensagem = erro instanceof ApiException apiException
                ? apiException.getMensagemCompleta()
                : erro.getMessage();
        if (mensagem == null || mensagem.isBlank()) {
            mensagem = "Não foi possível concluir a operação.";
        }
        Dialogs.erro(janela(), "Erro", mensagem);
    }

    private Filtro filtroSelecionado() {
        Filtro selecionado = filtro.getValue();
        return selecionado == null ? Filtro.TODAS : selecionado;
    }

    private Window janela() {
        return getScene() == null ? null : getScene().getWindow();
    }

    /**
     * Opções de filtro da listagem.
     */
    private enum Filtro {
        TODAS("Todas", null),
        PENDENTES("Pendentes", Boolean.FALSE),
        CONCLUIDAS("Concluídas", Boolean.TRUE);

        private final String rotulo;
        private final Boolean concluida;

        Filtro(final String rotuloParam, final Boolean concluidaParam) {
            this.rotulo = rotuloParam;
            this.concluida = concluidaParam;
        }

        @Override
        public String toString() {
            return rotulo;
        }
    }
}
