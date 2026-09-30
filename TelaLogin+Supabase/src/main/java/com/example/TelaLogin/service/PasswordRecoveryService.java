package com.example.TelaLogin.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

/**
 * CONTROLE DOS TOKENS DE RECUPERAÇÃO DE SENHA ("esqueci minha senha").
 *
 * Fluxo completo:
 *   1. POST /recoverpassword -> generateToken(email) cria um token aleatório
 *      (UUID) válido por 15 minutos e ele vai no link enviado por e-mail.
 *   2. GET /resetpassword?token=... -> getEmailFromToken(token) confere se o
 *      token existe e não expirou antes de mostrar a página de nova senha.
 *   3. POST /resetpassword -> confere o token de novo, o UserService grava a
 *      nova senha no Supabase e invalidateToken(token) apaga o token para ele
 *      não poder ser usado duas vezes.
 *
 * ONDE OS TOKENS FICAM:
 * Ainda em MEMÓRIA (um Map dentro desta classe), não no Supabase. Isso
 * significa que, se a aplicação reiniciar, os links já enviados param de
 * funcionar. Para um projeto de estudo é aceitável; o próximo passo seria
 * criar uma tabela "password_reset_tokens" no Supabase.
 *
 * O Map é um ConcurrentHashMap porque o Spring atende várias requisições ao
 * mesmo tempo (uma thread por requisição) e existe um único objeto deste
 * service para todas elas. Um HashMap comum pode se corromper nesse cenário.
 */
@Service
public class PasswordRecoveryService {

    private final Map<String, RecoveryToken> tokens = new ConcurrentHashMap<>();

    /**
     * Gera um token de recuperação para o e-mail informado.
     */
    public String generateToken(String email) {

        String token = UUID.randomUUID().toString();

        // Token válido por 15 minutos
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(15);

        tokens.put(token, new RecoveryToken(email, expiration));

        return token;
    }

    /**
     * Retorna o e-mail associado ao token.
     * Retorna null caso o token seja inválido ou expirado.
     */
    public String getEmailFromToken(String token) {

        RecoveryToken recoveryToken = tokens.get(token);

        if (recoveryToken == null) {
            return null;
        }

        // Verifica se o token expirou
        if (LocalDateTime.now().isAfter(recoveryToken.expiration())) {
            tokens.remove(token);
            return null;
        }

        return recoveryToken.email();
    }

    /**
     * Remove o token depois que ele for utilizado.
     */
    public void invalidateToken(String token) {
        tokens.remove(token);
    }

    /**
     * Guarda as informações do token.
     */
    private record RecoveryToken(
            String email,
            LocalDateTime expiration) {
    }
}
