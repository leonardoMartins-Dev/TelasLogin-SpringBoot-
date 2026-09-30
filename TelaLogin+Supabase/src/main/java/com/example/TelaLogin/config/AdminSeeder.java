package com.example.TelaLogin.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.TelaLogin.service.UserService;

/**
 * GARANTE QUE EXISTE UM ADMINISTRADOR NO SUPABASE.
 *
 * Antes o admin existia só na memória (InMemoryUserDetailsManager). Agora o
 * login lê tudo do banco, então o admin também precisa estar na tabela
 * "users", com role = 'ADMIN' e a senha em hash BCrypt. Como não dá para
 * gerar esse hash na mão pelo painel do Supabase, esta classe faz isso.
 *
 * COMO FUNCIONA:
 * Por implementar CommandLineRunner, o Spring executa o método run() UMA vez,
 * logo depois que a aplicação termina de subir. Ele:
 *   1. Lê app.admin.username / password / name do application.properties
 *      (através do UserConfig).
 *   2. Se esse e-mail já existe na tabela, não faz nada.
 *   3. Se não existe, cria o admin (UserService.createAdmin), que salva com
 *      a senha criptografada e role ADMIN.
 *
 * O admin fica na MESMA tabela dos usuários comuns; o que o diferencia é
 * role = 'ADMIN'. Ele não passa pelo /register, então CPF, RG, endereço e
 * instituição ficam NULL (por isso essas colunas aceitam NULL no banco).
 *
 * OBS.: se você mudar app.admin.password depois que o admin já foi criado, a
 * senha no banco NÃO muda. Use o "Esqueceu sua senha?" ou apague a linha do
 * admin no Supabase para ele ser recriado na próxima subida.
 *
 * Para ter MAIS de um admin: cadastre a pessoa normalmente pelo /register e
 * rode no SQL Editor do Supabase:
 *   update public.users set role = 'ADMIN' where email = 'email@da.pessoa';
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserService userService;
    private final UserConfig userConfig;

    public AdminSeeder(UserService userService, UserConfig userConfig) {
        this.userService = userService;
        this.userConfig = userConfig;
    }

    @Override
    public void run(String... args) {

        String adminEmail = userConfig.getAdminUsername();

        if (userService.exists(adminEmail)) {
            return;
        }

        userService.createAdmin(
                adminEmail,
                userConfig.getAdminPassword(),
                userConfig.getAdminName());

        System.out.println(
                "Administrador criado no banco: " + adminEmail);
    }
}
