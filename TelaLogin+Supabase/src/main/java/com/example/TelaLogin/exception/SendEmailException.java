package com.example.TelaLogin.exception;

/**
 * EXCEÇÃO PRÓPRIA PARA FALHAS NO ENVIO DE E-MAIL.
 *
 * O SendEmailService captura os erros técnicos do Java Mail (MailException,
 * MessagingException) e lança esta exceção no lugar, com uma mensagem mais
 * clara ("Falha ao enviar e-mail: ...").
 *
 * Vantagens:
 *   - Quem chama o service não precisa conhecer as exceções do Java Mail.
 *   - O GlobalExceptionHandler tem um método só para ela, podendo dar uma
 *     resposta específica para erro de e-mail.
 *
 * Estende RuntimeException (exceção "não verificada"), então não é preciso
 * declarar "throws" nem colocar try/catch em quem chama.
 */
public class SendEmailException extends RuntimeException {
    public SendEmailException(String message) {
        super(message);
    }
}
