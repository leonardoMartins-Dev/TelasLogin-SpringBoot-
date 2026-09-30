package com.example.TelaLogin.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * TRATAMENTO CENTRAL DE ERROS DOS CONTROLLERS.
 *
 * Com @ControllerAdvice, qualquer exceção que "escapar" de um método de
 * controller cai aqui, em vez de mostrar a página de erro padrão do Spring.
 * O método escolhido é o do tipo de exceção mais específico:
 *
 *   - SendEmailException -> falha ao mandar o e-mail de recuperação.
 *     Devolve HTTP 500 com um JSON contendo data/hora e a mensagem do erro.
 *   - Exception (qualquer outra) -> por exemplo, erro de conexão ou de SQL
 *     no Supabase. Devolve HTTP 500 com uma mensagem genérica, para não
 *     expor detalhes internos ao usuário.
 *
 * Os dois métodos registram o erro completo (com stack trace) no console.
 * Sem isso, um erro de banco apareceria para o usuário só como
 * "Ocorreu um erro inesperado" e não haveria como descobrir a causa.
 *
 * Obs.: as respostas são JSON, não uma página HTML. Para uma aplicação com
 * telas (Thymeleaf), o ideal seria redirecionar para a página /error.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(SendEmailException.class)
    public ResponseEntity<Object> handleEmailSendException(SendEmailException ex) {
        log.error("Erro ao enviar e-mail", ex);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        log.error("Erro inesperado", ex);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "Ocorreu um erro inesperado");

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
