package br.tasks.desktop.model;

/**
 * Prioridades exibidas no cliente desktop.
 */
public enum Prioridade {
    BAIXA("Baixa"),
    MEDIA("Média"),
    ALTA("Alta");

    private final String rotulo;

    Prioridade(final String rotuloParam) {
        this.rotulo = rotuloParam;
    }

    public String getRotulo() {
        return rotulo;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
