package com.example.TelaLogin.models;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * ENTIDADE JPA QUE REPRESENTA UM USUÁRIO SALVO NO SUPABASE.
 *
 * Cada objeto desta classe corresponde a UMA LINHA da tabela "public.users"
 * do PostgreSQL do Supabase. O Hibernate (implementação do JPA) faz a
 * tradução automática entre o objeto Java e a linha da tabela:
 *
 *   Campo Java     ->  Coluna no banco
 *   id             ->  id            (bigint, gerado pelo banco)
 *   email          ->  email         (text, único, usado como login)
 *   passwordHash   ->  password_hash (text, hash BCrypt da senha)
 *   name           ->  name          (text, nome exibido no e-mail)
 *   role           ->  role          ("USER" ou "ADMIN")
 *   cpf            ->  cpf           (text, só números)
 *   rg             ->  rg            (text)
 *   address        ->  address       (text, campo "endereco" do cadastro)
 *   institution    ->  institution   (text, campo "instituicao" do cadastro)
 *
 * POR QUE "users" E NÃO "user"?
 * "user" é uma palavra reservada do PostgreSQL. Com @Table(name = "user") o
 * Hibernate gera "select ... from user", que dá erro de sintaxe no banco.
 *
 * SENHA: NUNCA é guardada em texto puro. O UserService passa a senha pelo
 * BCrypt antes de criar o objeto, e aqui fica só o hash.
 *
 * ROLE: guardado sem o prefixo "ROLE_" (ex.: "ADMIN"). O Spring Security
 * adiciona o prefixo sozinho e o SecurityConfig compara com "ROLE_ADMIN".
 *
 * CPF, RG, ENDEREÇO E INSTITUIÇÃO podem ser NULL no banco: o admin (criado
 * pelo AdminSeeder) não tem esses dados. Para usuários comuns eles são
 * obrigatórios no formulário /register.
 *
 * IMPORTANTE: como spring.jpa.hibernate.ddl-auto=none, o Hibernate NÃO cria
 * nem altera a tabela. Ela precisa ser criada no Supabase com o script
 * supabase/schema.sql. Se mudar algum campo aqui, mude também no banco.
 */
@Entity //RECONHECE COMO ENTIDADE
@Table (name="users") // "user" é palavra reservada no PostgreSQL
public class User {

    @Id  //atribui ID
    @GeneratedValue(strategy = GenerationType.IDENTITY) //O BANCO GERA O VALOR
    private Long id;

    @Column (nullable = false, unique = true) //N pode ser NULL nem repetir
    private String email;

    @Column (name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    // "USER" ou "ADMIN". Precisa de valor no Java: o Hibernate envia a coluna
    // no INSERT, então o default do banco não seria usado.
    @Column(nullable = false)
    private String role = "USER";

    // DADOS DO CADASTRO (podem ser NULL: o admin não tem)
    @Column
    private String cpf;

    @Column
    private String rg;

    @Column
    private String address;

    @Column
    private String institution;

    // created_at não precisa estar aqui: o banco preenche sozinho (default now())

    //CONSTRUTORES
     public User() {
    }

    public User(String email, String passwordHash, String name, String role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.role = role;
    }

    //GETTERS SETTERS

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }

}
