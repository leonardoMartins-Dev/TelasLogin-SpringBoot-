package com.example.TelaLogin.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.TelaLogin.config.UserConfig;
import com.example.TelaLogin.service.PasswordRecoveryService;
import com.example.TelaLogin.service.SendEmailService;
import com.example.TelaLogin.service.UserService;

/**
 * CONTROLLER DE TODAS AS PÁGINAS DA APLICAÇÃO (rotas / URLs).
 *
 * Recebe as requisições do navegador e decide qual template Thymeleaf mostrar
 * (o "return" é o caminho do HTML dentro de src/main/resources/templates) ou
 * para onde redirecionar ("redirect:/..."). A regra de negócio fica nos
 * services; o controller só recebe os dados do formulário e repassa.
 *
 * ROTAS:
 *   GET  /home            -> página do usuário logado (user/home.html)
 *   GET  /admin           -> página do administrador (admin/admin.html)
 *   GET  /login           -> página de login; envia a site key do reCAPTCHA
 *                            para o HTML. O POST /login NÃO está aqui: quem
 *                            processa é o próprio Spring Security.
 *   GET  /error           -> página genérica de erro (error.html)
 *   GET  /register        -> formulário de cadastro
 *   POST /register        -> cadastra o usuário no Supabase (via UserService)
 *                            com nome, e-mail, senha, CPF, RG, endereço e
 *                            instituição, se o e-mail ainda não existir
 *   GET  /recoverpassword -> formulário "esqueci minha senha"
 *   POST /recoverpassword -> gera o token (PasswordRecoveryService) e envia o
 *                            link de redefinição por e-mail (SendEmailService)
 *   GET  /resetpassword   -> valida o token do link e mostra o formulário de
 *                            nova senha
 *   POST /resetpassword   -> confere as senhas e o token, grava a nova senha
 *                            no Supabase e invalida o token
 *
 * As mensagens de sucesso/erro chegam às páginas por parâmetros na URL
 * (ex.: /login?cadastro=sucesso), lidos no HTML com ${param.xxx}.
 *
 * Toda rota pública nova precisa ser liberada também no SecurityConfig.
 */
@Controller
public class SecureLoginController {

        private final UserConfig userConfig;
        private final SendEmailService sendEmailService;
        private final UserService userService;
        private final PasswordRecoveryService passwordRecoveryService;

        public SecureLoginController(
                        UserConfig userConfig,
                        SendEmailService sendEmailService,
                        UserService userService,
                        PasswordRecoveryService passwordRecoveryService) {

                this.userConfig = userConfig;
                this.sendEmailService = sendEmailService;
                this.userService = userService;
                this.passwordRecoveryService = passwordRecoveryService;
        }

        // =========================================================
        // HOME
        // =========================================================

        @GetMapping("/home")
        public String home(
                        Authentication authentication,
                        Model model) {

                System.out.println(
                                "Usuário logado: " + authentication.getName());

                model.addAttribute(
                                "usuario",
                                authentication.getName());

                return "user/home";
        }

        // =========================================================
        // LOGIN
        // =========================================================

        @GetMapping("/login")
        public String login(Model model) {

                model.addAttribute(
                                "recaptchaSiteKey",
                                userConfig.getRecaptchaSiteKey());

                return "login/login";
        }

        // =========================================================
        // ERROR
        // =========================================================

        @GetMapping("/error")
        public String error() {
                return "error";
        }

        // =========================================================
        // ADMIN
        // =========================================================

        @GetMapping("/admin")
        public String admin(
                        Authentication authentication,
                        Model model) {

                System.out.println(
                                "Administrador logado: "
                                                + authentication.getName());

                model.addAttribute(
                                "usuario",
                                authentication.getName());

                return "admin/admin";
        }

        // =========================================================
        // CADASTRO
        // =========================================================

        @GetMapping("/register")
        public String register() {
                return "login/register";
        }

        @PostMapping("/register")
        public String handleRegister(
                        @RequestParam("nome") String nome,
                        @RequestParam("email") String email,
                        @RequestParam("cpf") String cpf,
                        @RequestParam("rg") String rg,
                        @RequestParam("endereco") String endereco,
                        @RequestParam("instituicao") String instituicao,
                        @RequestParam("senha") String senha) {

                if (userService.exists(email)) {

                        System.out.println(
                                        "Usuário já cadastrado: " + email);

                        return "redirect:/register?erro=email";
                }

                userService.createUser(
                                email,
                                senha,
                                nome,
                                cpf,
                                rg,
                                endereco,
                                instituicao);

                System.out.println(
                                "Usuário cadastrado: " + email);

                System.out.println(
                                "Nome cadastrado: " + nome);

                return "redirect:/login?cadastro=sucesso";
        }

        // =========================================================
        // RECUPERAÇÃO DE SENHA
        // =========================================================

        @GetMapping("/recoverpassword")
        public String recoverpassword() {
                return "login/recoverpassword";
        }

        @PostMapping("/recoverpassword")
        public String handleRecoverPassword(
                        @RequestParam("email") String email) {

                if (!userService.exists(email)) {

                        System.out.println(
                                        "E-mail não encontrado: " + email);

                        return "redirect:/recoverpassword?erro=email";
                }

                String nome = userService.getName(email);

                if (nome == null || nome.isBlank()) {
                        nome = email;
                }

                String token = passwordRecoveryService.generateToken(email);

                String link = "http://localhost:8080/resetpassword?token="
                                + token;

                sendEmailService.sendEmail(
                                email,
                                "Recuperação de Senha - Tela Login",
                                "<!DOCTYPE html><html lang='pt-BR'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'></head><body style='margin:0;padding:0;background-color:#f8fafc;font-family:Arial,Helvetica,sans-serif;color:#171717;'><div style='width:100%;padding:40px 20px;box-sizing:border-box;'><div style='max-width:600px;margin:0 auto;background:#ffffff;border:1px solid rgba(0,51,79,0.09);border-radius:20px;overflow:hidden;box-shadow:0 2px 4px rgba(0,51,79,0.08),0 24px 56px -12px rgba(0,51,79,0.20);'><div style='padding:40px 30px;text-align:center;background:linear-gradient(145deg,#00334f,#005380);'><div style='color:#ffffff;font-size:13px;font-weight:500;letter-spacing:2px;margin-bottom:12px;'>TELA LOGIN</div><div style='color:#ffffff;font-size:28px;font-weight:500;line-height:1.2;'>Recuperação de Senha</div></div><div style='padding:40px 45px;text-align:center;'><p style='margin:0 0 18px 0;color:#005380;font-size:18px;font-weight:500;'>Olá, "
                                                + nome
                                                + "!</p><div style='width:60px;height:3px;margin:0 auto 25px auto;background-color:#005380;border-radius:999px;'></div><p style='margin:0 0 18px 0;color:#64748b;font-size:15px;line-height:1.7;'>Recebemos uma solicitação para redefinir a senha da sua conta no sistema.</p><p style='margin:0 0 30px 0;color:#64748b;font-size:15px;line-height:1.7;'>Clique no botão abaixo para criar uma nova senha.</p><a href='"
                                                + link
                                                + "' style='display:inline-block;padding:14px 28px;background-color:#005380;color:#ffffff;text-decoration:none;border-radius:8px;font-size:15px;font-weight:500;'>Redefinir minha senha</a><p style='margin:30px 0 0 0;padding:15px;background-color:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;color:#64748b;font-size:13px;line-height:1.6;'>Este link é válido por <strong style='color:#171717;'>15 minutos</strong>.</p><p style='margin:25px 0 0 0;color:#64748b;font-size:13px;line-height:1.6;'>Se você não solicitou a recuperação da senha, ignore este e-mail.</p></div><div style='padding:20px 30px;background-color:#f8fafc;border-top:1px solid #e2e8f0;text-align:center;'><p style='margin:0;color:#64748b;font-size:12px;line-height:1.5;'>Tela Login<br>Sistema de Autenticação</p></div></div></div></body></html>");

                System.out.println(
                                "Link de recuperação enviado para: " + email);

                return "redirect:/recoverpassword?sucesso=email";
        }

        // =========================================================
        // RESET DE SENHA
        // =========================================================

        @GetMapping("/resetpassword")
        public String resetPassword(
                        @RequestParam("token") String token,
                        Model model) {

                String email = passwordRecoveryService
                                .getEmailFromToken(token);

                if (email == null) {

                        model.addAttribute(
                                        "erro",
                                        "O link de recuperação é inválido ou expirou.");

                        return "error";
                }

                model.addAttribute(
                                "token",
                                token);

                return "login/resetpassword";
        }

        @PostMapping("/resetpassword")
        public String handleResetPassword(
                        @RequestParam("token") String token,
                        @RequestParam("senha") String senha,
                        @RequestParam("confirmarSenha") String confirmarSenha) {

                if (!senha.equals(confirmarSenha)) {

                        return "redirect:/resetpassword?token="
                                        + token
                                        + "&erro=senhas";
                }

                String email = passwordRecoveryService
                                .getEmailFromToken(token);

                if (email == null) {

                        return "redirect:/login?erro=token";
                }

                userService.updatePassword(
                                email,
                                senha);

                passwordRecoveryService.invalidateToken(token);

                System.out.println(
                                "Senha alterada com sucesso para: "
                                                + email);

                return "redirect:/login?senha=alterada";
        }
}