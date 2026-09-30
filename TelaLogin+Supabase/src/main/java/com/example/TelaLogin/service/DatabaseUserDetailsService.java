package com.example.TelaLogin.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.TelaLogin.models.User;

/**
 * PONTE ENTRE O SPRING SECURITY E A TABELA "users" DO SUPABASE.
 *
 * Substitui o antigo InMemoryUserDetailsManager (usuários fixos na memória).
 * O Spring Security não sabe nada sobre o nosso banco; ele só sabe chamar
 * um UserDetailsService. Por isso esta classe implementa essa interface.
 *
 * COMO O LOGIN FUNCIONA AGORA:
 *   1. O usuário envia o formulário de /login (campos "username" e "password").
 *   2. O RecaptchaFilter valida o captcha.
 *   3. O Spring Security chama loadUserByUsername(username) desta classe.
 *   4. Buscamos o usuário no Supabase pelo e-mail (via UserService).
 *      - Não existe? Lançamos UsernameNotFoundException -> login falha.
 *   5. Devolvemos um UserDetails com e-mail, hash da senha e role.
 *   6. O próprio Spring compara a senha digitada com o hash usando o
 *      PasswordEncoder (BCrypt) do SecurityConfig. Nós nunca comparamos
 *      senhas manualmente.
 *   7. Se bater, o successHandler do SecurityConfig redireciona para
 *      /admin (ROLE_ADMIN) ou /home (ROLE_USER).
 *
 * Não precisa registrar esta classe em lugar nenhum: por ser o único bean do
 * tipo UserDetailsService, o Spring Security a encontra sozinho.
 *
 * Atenção ao nome "User": existe o nosso models.User (entidade do banco) e o
 * User do Spring Security (objeto de login). Aqui o do Spring é escrito com
 * o nome completo para não confundir.
 */
@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public DatabaseUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userService.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário não encontrado: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole())
                .build();
    }
}
