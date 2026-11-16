package br.tasks.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Tarefa pertencente a um único usuário.
 */
@Entity
@Table(name = "tarefas",
        indexes = @Index(name = "idx_tarefas_usuario", columnList = "usuario_id"))
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "descricao", nullable = false, length = 255)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridade", nullable = false, length = 20)
    private Prioridade prioridade;

    @Column(name = "concluida", nullable = false)
    private boolean concluida;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_tarefas_usuario"))
    private Usuario usuario;

    /**
     * Construtor exigido pelo JPA.
     */
    protected Tarefa() {
        // JPA
    }

    /**
     * Cria uma nova tarefa.
     *
     * @param descricaoParam  descrição da tarefa
     * @param prioridadeParam prioridade da tarefa
     * @param concluidaParam  indica se a tarefa já está concluída
     * @param usuarioParam    dono da tarefa
     */
    public Tarefa(final String descricaoParam, final Prioridade prioridadeParam,
                  final boolean concluidaParam, final Usuario usuarioParam) {
        this.descricao = descricaoParam;
        this.prioridade = prioridadeParam == null ? Prioridade.MEDIA : prioridadeParam;
        this.concluida = concluidaParam;
        this.usuario = usuarioParam;
    }

    public Long getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(final String descricaoParam) {
        this.descricao = descricaoParam;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(final Prioridade prioridadeParam) {
        this.prioridade = prioridadeParam == null ? Prioridade.MEDIA : prioridadeParam;
    }

    public boolean isConcluida() {
        return concluida;
    }

    public void setConcluida(final boolean concluidaParam) {
        this.concluida = concluidaParam;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(final Usuario usuarioParam) {
        this.usuario = usuarioParam;
    }
}
