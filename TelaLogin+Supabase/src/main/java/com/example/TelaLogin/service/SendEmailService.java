package com.example.TelaLogin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.example.TelaLogin.exception.SendEmailException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * ENVIO DE E-MAILS PELO GMAIL (SMTP).
 *
 * Usa o JavaMailSender, que o Spring Boot configura sozinho a partir das
 * chaves spring.mail.* do application.properties (servidor smtp.gmail.com,
 * porta 587, TLS). O usuário e a senha de app do Gmail vêm do .env.
 *
 * sendEmail(para, assunto, corpo):
 *   - Monta uma MimeMessage (formato que aceita HTML e acentos em UTF-8).
 *   - O "true" em setText(body, true) indica que o corpo é HTML, por isso o
 *     e-mail de recuperação chega com layout, botão e cores.
 *   - O remetente (from) é o próprio e-mail configurado em spring.mail.username.
 *   - Se o Gmail recusar (senha de app errada, sem internet...), a exceção
 *     técnica é trocada por uma SendEmailException, tratada no
 *     GlobalExceptionHandler.
 *
 * Hoje é usado só no fluxo "esqueci minha senha" (SecureLoginController).
 */
@Service
public class SendEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public SendEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);

        } catch (MailException | MessagingException e) {
            throw new SendEmailException(
                    "Falha ao enviar e-mail: " + e.getMessage());
        }
    }
}