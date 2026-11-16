package br.tasks.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.ArrayList;
import java.util.List;

/**
 * Usuário da aplicação. Cada usuário possui as suas próprias tarefas.
 */
@Entity
@Table(name = "usuarios",
        uniqueConstraints = @UniqueConstraint(name = "uk_usuarios_email", columnNames = "email"))
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "email", nullable = false, length = 160)
    private String email;

    /**
     * Senha armazenada exclusivamente como hash BCrypt.
     */
    @Column(name = "senha", nullable = false, length = 100)
    private String senha;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Tarefa> tarefas = new ArrayList<>();

    /**
     * Construtor exigido pelo JPA.
     */
    protected Usuario() {
        // JPA
    }

    /**
     * Cria um novo usuário.
     *
     * @param nomeParam  nome completo
     * @param emailParam e-mail já normalizado
     * @param senhaParam hash da senha
     */
    public Usuario(final String nomeParam, final String emailParam, final String senhaParam) {
        this.nome = nomeParam;
        this.email = emailParam;
        this.senha = senhaParam;
    }

    /**
     * Adiciona uma tarefa ao usuário mantendo os dois lados do relacionamento.
     *
     * @param tarefa tarefa a ser adicionada
     */
    public void adicionarTarefa(final Tarefa tarefa) {
        tarefas.add(tarefa);
        tarefa.setUsuario(this);
    }

    /**
     * Remove uma tarefa do usuário mantendo os dois lados do relacionamento.
     *
     * @param tarefa tarefa a ser removida
     */
    public void removerTarefa(final Tarefa tarefa) {
        tarefas.remove(tarefa);
        tarefa.setUsuario(null);
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(final String nomeParam) {
        this.nome = nomeParam;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String emailParam) {
        this.email = emailParam;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(final String senhaParam) {
        this.senha = senhaParam;
    }

    public List<Tarefa> getTarefas() {
        return tarefas;
    }
}
