package com.example.TelaLogin.config;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import com.example.TelaLogin.service.RecaptchaService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * FILTRO QUE EXIGE O GOOGLE reCAPTCHA NO LOGIN.
 *
 * Um "filtro" roda ANTES da requisição chegar ao controller ou ao login do
 * Spring Security. Este é colocado na corrente de filtros pelo SecurityConfig,
 * logo antes do UsernamePasswordAuthenticationFilter (o filtro que confere
 * usuário e senha).
 *
 * O que ele faz em cada requisição:
 *   - Se NÃO for "POST /login": deixa passar sem fazer nada.
 *   - Se for "POST /login": pega o campo "g-recaptcha-response" enviado pelo
 *     widget do captcha e pede para o RecaptchaService validar no Google.
 *       - Válido   -> segue para o login normal (usuário e senha no Supabase).
 *       - Inválido -> redireciona para /login?captcha=true e PARA aqui; a
 *         senha nem chega a ser conferida.
 *
 * OncePerRequestFilter garante que o filtro roda uma única vez por requisição.
 * Não tem @Component de propósito: é criado com "new" no SecurityConfig. Se
 * fosse @Component, o Spring o registraria de novo como filtro comum.
 */
public class RecaptchaFilter extends OncePerRequestFilter {

    private final RecaptchaService recaptchaService;


    public RecaptchaFilter(
            RecaptchaService recaptchaService) {

        this.recaptchaService = recaptchaService;
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getRequestURI().equals("/login")
                && request.getMethod().equalsIgnoreCase("POST")) {

            String captchaResponse =
                    request.getParameter(
                            "g-recaptcha-response");

            boolean captchaValido =
                    recaptchaService.validate(
                            captchaResponse);

            if (!captchaValido) {

                response.sendRedirect(
                        "/login?captcha=true");

                return;
            }
        }

        filterChain.doFilter(
                request,
                response);
    }
}
