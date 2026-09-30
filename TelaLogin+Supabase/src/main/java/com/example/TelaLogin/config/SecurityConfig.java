package com.example.TelaLogin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.TelaLogin.service.RecaptchaService;

//SEMPRE ADICIONAR UM LINHA NOVA NO SECURITY CONFIG PARA CADA PAGINA CRIADA

/**
 * CONFIGURAÇÃO DE SEGURANÇA (Spring Security).
 *
 * Define QUEM pode acessar O QUÊ e COMO o login funciona.
 *
 * 1) securityFilterChain(): regras de acesso por URL
 *    - Públicas (sem login): /login, /register, /recoverpassword,
 *      /resetpassword, /error e os arquivos estáticos em /css e /images.
 *    - /admin/** só para quem tem o papel ADMIN (ROLE_ADMIN).
 *    - Qualquer outra URL (ex.: /home) exige estar logado.
 *    - Coloca o RecaptchaFilter ANTES do filtro de login do Spring, para
 *      barrar o POST /login quando o captcha não foi resolvido.
 *    - formLogin: usa a nossa página /login (campos "username" e "password").
 *      Sucesso: ADMIN vai para /admin, os demais para /home.
 *      Falha (senha errada / usuário inexistente): vai para /error.
 *    - logout: POST /logout encerra a sessão e volta para /login?logout=true.
 *
 * 2) passwordEncoder(): BCrypt. É usado em dois momentos:
 *    - no cadastro/troca de senha (UserService), para gerar o hash salvo;
 *    - no login, para comparar a senha digitada com o hash do banco.
 *
 * DE ONDE VÊM OS USUÁRIOS DO LOGIN?
 * Antes: InMemoryUserDetailsManager, com usuários fixos lidos do
 * application.properties (sumiam ao reiniciar e o cadastro não servia para
 * logar). Agora: do Supabase. O Spring Security encontra sozinho o bean
 * DatabaseUserDetailsService (único UserDetailsService) e o PasswordEncoder
 * abaixo, e monta a autenticação com os dois. Por isso não há mais nenhum
 * bean de usuários aqui.
 *
 * A proteção CSRF continua ativa (padrão). Os formulários usam th:action, que
 * adiciona o token CSRF automaticamente.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final RecaptchaFilter recaptchaFilter;

    public SecurityConfig(RecaptchaService recaptchaService) {
        this.recaptchaFilter = new RecaptchaFilter(recaptchaService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/login/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/login/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/css/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/images/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/recoverpassword").permitAll()
                .requestMatchers(HttpMethod.POST, "/recoverpassword").permitAll()
                .requestMatchers(HttpMethod.GET, "/resetpassword").permitAll()
                .requestMatchers(HttpMethod.POST, "/resetpassword").permitAll()
                .requestMatchers(HttpMethod.GET, "/error").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(recaptchaFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
                .successHandler((request, response, authentication) -> {
                    if (authentication.getAuthorities().stream()
                            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
                        response.sendRedirect("/admin");
                    } else {
                        response.sendRedirect("/home");
                    }
                })
                .failureHandler((request, response, authentication) -> {
                    response.sendRedirect("/error");
                })
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
