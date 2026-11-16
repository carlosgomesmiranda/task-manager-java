package br.tasks.desktop.ui;

import javafx.application.Platform;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Executa chamadas HTTP fora da thread de interface e devolve o resultado no
 * JavaFX Application Thread.
 */
public final class AsyncExecutor {

    private final ExecutorService executor;

    /**
     * Cria um pool de threads daemon exclusivo para chamadas à API.
     */
    public AsyncExecutor() {
        this.executor = Executors.newFixedThreadPool(4, tarefa -> {
            Thread thread = new Thread(tarefa, "taskmanager-api");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Executa uma ação assíncrona.
     *
     * @param acao      chamada à API
     * @param aoSucesso callback executado na thread JavaFX
     * @param aoErro    callback de erro executado na thread JavaFX
     * @param <T>       tipo do resultado
     */
    public <T> void run(final Callable<T> acao, final Consumer<T> aoSucesso,
                        final Consumer<Throwable> aoErro) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return acao.call();
            } catch (Exception ex) {
                throw new CompletionException(ex);
            }
        }, executor).whenComplete((resultado, erro) -> Platform.runLater(() -> {
            if (erro != null) {
                aoErro.accept(desembrulhar(erro));
            } else {
                aoSucesso.accept(resultado);
            }
        }));
    }

    /**
     * Encerra o pool de threads.
     */
    public void shutdown() {
        executor.shutdownNow();
    }

    private Throwable desembrulhar(final Throwable erro) {
        Throwable atual = erro;
        while ((atual instanceof CompletionException || atual instanceof ExecutionException)
                && atual.getCause() != null) {
            atual = atual.getCause();
        }
        return atual;
    }
}
