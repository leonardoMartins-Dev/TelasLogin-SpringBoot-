package com.example.TelaLogin.service;


import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.TelaLogin.models.User;
import com.example.TelaLogin.repository.UserRepository;

/**
 * SERVIÇO DE USUÁRIOS (regras de negócio sobre os usuários do Supabase).
 *
 * É a única classe que conversa com o UserRepository. Controllers, o login
 * (DatabaseUserDetailsService) e o AdminSeeder sempre passam por aqui, assim
 * as regras abaixo valem em todo o sistema:
 *
 * 1) E-MAIL NORMALIZADO: todo e-mail passa por normalize() (tira espaços e
 *    deixa minúsculo) antes de ir para o banco. Assim "Leo@Gmail.com " e
 *    "leo@gmail.com" são o mesmo usuário, tanto no cadastro quanto no login.
 *
 * 2) SENHA SEMPRE COM HASH: a senha digitada nunca é salva. Ela passa pelo
 *    PasswordEncoder (BCrypt, definido no SecurityConfig) e só o hash vai
 *    para a coluna password_hash.
 *
 * 3) CPF SÓ COM NÚMEROS: "123.456.789-00" é salvo como "12345678900", para
 *    que buscas e comparações não dependam de como o usuário digitou.
 *
 * Métodos:
 *   createUser()     -> cadastro pela página /register (role USER), com
 *                       nome, e-mail, senha, CPF, RG, endereço e instituição
 *   createAdmin()    -> usado pelo AdminSeeder na subida da aplicação (só
 *                       e-mail, senha e nome; os demais dados ficam NULL)
 *   exists()         -> verifica se o e-mail já está cadastrado
 *   findByEmail()    -> busca o usuário (usado no login)
 *   getName()        -> nome do usuário (usado no e-mail de recuperação)
 *   updatePassword() -> grava a nova senha no fluxo "esqueci minha senha"
 */
@Service
public class UserService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

        public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
                this.userRepository = userRepository;
                this.passwordEncoder =  passwordEncoder;
        }

        //CRIA O USER COM OS DADOS DO CADASTRO E SALVA NO DB
        public void createUser(String email, String senha, String nome,
                        String cpf, String rg, String endereco, String instituicao){
                User user = new User(normalize(email), passwordEncoder.encode(senha), nome, "USER");
                user.setCpf(onlyDigits(cpf));
                user.setRg(rg);
                user.setAddress(endereco);
                user.setInstitution(instituicao);
                userRepository.save(user);
        }

        //CRIA O ADMIN E SALVA NO DB
        public void createAdmin(String email, String senha, String nome){
                userRepository.save(new User(normalize(email), passwordEncoder.encode(senha), nome, "ADMIN"));
        }

        //VERIFICA O USER PELO EMAIL
        public boolean exists(String email){
                return userRepository.existsByEmail(normalize(email));
        }

        //BUSCA O USER PELO EMAIL
        public Optional<User> findByEmail(String email){
                return userRepository.findByEmail(normalize(email));
        }

        //RETORNA O NOME DO USER (OU NULL SE NÃO EXISTIR)
        public String getName(String email){
                return findByEmail(email)
                                .map(User::getName)
                                .orElse(null);
        }

        //TROCA A SENHA E SALVA NO DB
        public void updatePassword(String email, String novaSenha){
                User user = findByEmail(email)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Usuário não encontrado: " + email));

                user.setPasswordHash(passwordEncoder.encode(novaSenha));

                // Como o user já tem id, o save() faz UPDATE em vez de INSERT
                userRepository.save(user);
        }

        //PADRONIZA O EMAIL: SEM ESPAÇOS E EM MINÚSCULO
        private String normalize(String email){
                return email == null ? null : email.trim().toLowerCase();
        }

        //DEIXA SÓ OS NÚMEROS DO CPF (123.456.789-00 -> 12345678900)
        private String onlyDigits(String value){
                return value == null ? null : value.replaceAll("\\D", "");
        }
}
