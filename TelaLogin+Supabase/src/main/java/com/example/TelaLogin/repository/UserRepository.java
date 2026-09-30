package com.example.TelaLogin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.TelaLogin.models.User;

/**
 * REPOSITÓRIO DE USUÁRIOS (acesso à tabela "users" no Supabase).
 *
 * É só uma interface: o Spring Data JPA cria a implementação sozinho quando a
 * aplicação sobe. Por herdar de JpaRepository<User, Long> (entidade User, ID
 * do tipo Long) ela já vem com os métodos básicos prontos:
 *   save(user)      -> INSERT (se o id for null) ou UPDATE (se já tiver id)
 *   findById(id)    -> SELECT pelo id
 *   findAll()       -> SELECT de todos
 *   deleteById(id)  -> DELETE
 *
 * Os métodos declarados abaixo são "query methods": o Spring lê o NOME do
 * método e gera o SQL automaticamente:
 *   findByEmail(email)   -> select * from users where email = ?
 *   existsByEmail(email) -> select count(*) > 0 from users where email = ?
 *
 * Quem usa: UserService (cadastro, busca, troca de senha). Os controllers não
 * falam direto com o repositório; passam sempre pelo service.
 *
 * Para ser encontrado, o pacote desta interface precisa estar coberto pelo
 * @EnableJpaRepositories da classe TelaLoginApplication.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional <User> findByEmail(String email);
    boolean existsByEmail(String email);

}
