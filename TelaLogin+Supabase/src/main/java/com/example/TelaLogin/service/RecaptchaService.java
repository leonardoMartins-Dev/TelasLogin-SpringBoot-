package com.example.TelaLogin.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;

import com.example.TelaLogin.config.UserConfig;

/**
 * VALIDAÇÃO DO GOOGLE reCAPTCHA NO SERVIDOR.
 *
 * O widget "Não sou um robô" na página de login gera um código temporário
 * (g-recaptcha-response). Esse código sozinho não prova nada: o servidor
 * precisa perguntar ao Google se ele é verdadeiro. É isso que validate() faz:
 *
 *   1. Se o código veio vazio (usuário não marcou o captcha) -> false.
 *   2. Faz um POST para https://www.google.com/recaptcha/api/siteverify
 *      enviando a SECRET KEY (do .env, via UserConfig) e o código recebido.
 *   3. Lê a resposta JSON do Google e devolve true se tiver "success": true.
 *   4. Qualquer falha (sem internet, timeout, etc.) -> false, ou seja, na
 *      dúvida o login é bloqueado.
 *
 * Usado pelo RecaptchaFilter antes de cada POST /login. Usa o HttpClient
 * nativo do Java (java.net.http), sem bibliotecas extras.
 */
@Service
public class RecaptchaService {

    private final UserConfig userConfig;

    private final HttpClient httpClient =
            HttpClient.newHttpClient();


    public RecaptchaService(UserConfig userConfig) {
        this.userConfig = userConfig;
    }


    public boolean validate(String captchaResponse) {

        if (captchaResponse == null
                || captchaResponse.isBlank()) {

            return false;
        }

        try {

            String body =
                    "secret="
                            + URLEncoder.encode(
                                    userConfig.getRecaptchaSecretKey(),
                                    StandardCharsets.UTF_8)
                            + "&response="
                            + URLEncoder.encode(
                                    captchaResponse,
                                    StandardCharsets.UTF_8);

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(
                                    "https://www.google.com/recaptcha/api/siteverify"))
                            .header(
                                    "Content-Type",
                                    "application/x-www-form-urlencoded")
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(body))
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString());

            return response.body()
                    .contains("\"success\": true")
                    || response.body()
                    .contains("\"success\":true");

        } catch (Exception e) {

            System.out.println(
                    "Erro ao validar reCAPTCHA: "
                            + e.getMessage());

            return false;
        }
    }
}