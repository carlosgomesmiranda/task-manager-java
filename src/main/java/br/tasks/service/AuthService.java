package br.tasks.service;

import br.tasks.dto.auth.LoginRequest;
import br.tasks.dto.auth.LoginResponse;
import br.tasks.dto.auth.RegisterRequest;
import br.tasks.dto.usuario.UsuarioResponse;

/**
 * Serviço de cadastro e autenticação de usuários.
 */
public interface AuthService {

    /**
     * Cadastra um novo usuário com a senha codificada em BCrypt.
     *
     * @param request dados do cadastro
     * @return dados públicos do usuário criado
     */
    UsuarioResponse registrar(RegisterRequest request);

    /**
     * Autentica o usuário e gera um token JWT.
     *
     * @param request credenciais
     * @return token e dados do usuário
     */
    LoginResponse autenticar(LoginRequest request);
}
