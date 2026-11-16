package br.tasks.desktop.session;

import br.tasks.desktop.model.UsuarioResponse;

/**
 * Sessão do cliente desktop. O token JWT é mantido apenas em memória e é
 * descartado ao sair da aplicação.
 */
public final class Session {

    private volatile String token;
    private volatile UsuarioResponse usuario;

    /**
     * Inicia a sessão após o login.
     *
     * @param tokenParam   token JWT
     * @param usuarioParam dados do usuário autenticado
     */
    public void iniciar(final String tokenParam, final UsuarioResponse usuarioParam) {
        if (tokenParam == null || tokenParam.isBlank()) {
            throw new IllegalArgumentException("O token JWT não pode ser vazio");
        }
        this.token = tokenParam;
        this.usuario = usuarioParam;
    }

    /**
     * Encerra a sessão e apaga o token da memória.
     */
    public void encerrar() {
        this.token = null;
        this.usuario = null;
    }

    public boolean isAutenticado() {
        return token != null && !token.isBlank();
    }

    public String getToken() {
        return token;
    }

    public UsuarioResponse getUsuario() {
        return usuario;
    }
}
