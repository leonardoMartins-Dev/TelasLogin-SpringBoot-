# Projeto TelaLogin

## Descrição
O TelaLogin é um módulo de autenticação pronto para ser reaproveitado em outros projetos. É uma aplicação web feita com Spring Boot e Spring Security, com os usuários salvos em um banco **PostgreSQL no Supabase**.

O que já vem pronto:
- **Login** com e-mail e senha (senha guardada com hash BCrypt) e **Google reCAPTCHA**
- **Cadastro** de novos usuários (nome, e-mail, senha, CPF, RG, endereço e instituição), salvos na tabela `users` do Supabase
- **Recuperação de senha** por e-mail (Gmail), com link válido por 15 minutos
- **Dois perfis de acesso**: `USER` (vai para `/home`) e `ADMIN` (vai para `/admin`)
- **Administrador criado automaticamente** no banco na primeira vez que a aplicação sobe

> Para usar em outro projeto, veja o passo a passo em [Como usar este módulo em um projeto novo](#como-usar-este-módulo-em-um-projeto-novo), no final deste README.

---

## Estrutura do Projeto

```text
📁 TelaLogin
│
├── 📁 src
│   └── 📁 main
│       │
│       ├── ☕ java
│       │   └── 📦 com.example.TelaLogin
│       │       │
│       │       ├── 🚀 application
│       │       │   └── TelaLoginApplication.java
│       │       │       └── Classe principal; define onde o Spring procura beans, entidades e repositórios
│       │       │
│       │       ├── 🔐 config
│       │       │   ├── SecurityConfig.java
│       │       │   │   └── Regras de acesso, login, logout e BCrypt
│       │       │   │
│       │       │   ├── UserConfig.java
│       │       │   │   └── Leitura dos dados do admin e das chaves do reCAPTCHA
│       │       │   │
│       │       │   ├── RecaptchaFilter.java
│       │       │   │   └── Filtro que exige o reCAPTCHA no POST /login
│       │       │   │
│       │       │   └── AdminSeeder.java
│       │       │       └── Cria o administrador no Supabase ao subir (se ainda não existir)
│       │       │
│       │       ├── 🎮 controller
│       │       │   └── SecureLoginController.java
│       │       │       └── Rotas e páginas da aplicação
│       │       │
│       │       ├── ⚠️ exception
│       │       │   ├── GlobalExceptionHandler.java
│       │       │   │   └── Tratamento global de exceções (registra o erro no console)
│       │       │   │
│       │       │   └── SendEmailException.java
│       │       │       └── Exceção relacionada ao envio de e-mails
│       │       │
│       │       ├── 🧩 models
│       │       │   └── User.java
│       │       │       └── Entidade JPA: uma linha da tabela "users"
│       │       │
│       │       ├── 🗄️ repository
│       │       │   └── UserRepository.java
│       │       │       └── Acesso à tabela "users" (Spring Data JPA)
│       │       │
│       │       └── ⚙️ service
│       │           ├── UserService.java
│       │           │   └── Cadastro, busca e troca de senha dos usuários no banco
│       │           │
│       │           ├── DatabaseUserDetailsService.java
│       │           │   └── Entrega ao Spring Security o usuário do banco na hora do login
│       │           │
│       │           ├── PasswordRecoveryService.java
│       │           │   └── Tokens de recuperação de senha (em memória, 15 min)
│       │           │
│       │           ├── SendEmailService.java
│       │           │   └── Envio de e-mails pelo Gmail
│       │           │
│       │           └── RecaptchaService.java
│       │               └── Validação do Google reCAPTCHA
│       │
│       └── 📁 resources
│           │
│           ├── ⚙️ application.properties
│           │   └── Configurações de admin, e-mail, reCAPTCHA e Supabase
│           │
│           ├── 🎨 static
│           │   │
│           │   ├── 🎨 css
│           │   │   ├── admin.css
│           │   │   ├── error.css
│           │   │   ├── home.css
│           │   │   ├── login.css
│           │   │   ├── recoverpassword.css
│           │   │   ├── register.css
│           │   │   └── resetpassword.css
│           │   │       └── Arquivos de estilização das páginas
│           │   │
│           │   └── 🖼️ images
│           │       └── teste.png
│           │           └── Imagens utilizadas pela aplicação
│           │
│           └── 🌐 templates
│               │
│               ├── error.html
│               │   └── Página apresentada quando ocorre um erro
│               │
│               ├── 📁 login
│               │   ├── login.html
│               │   │   └── Página de login com Google reCAPTCHA
│               │   │
│               │   ├── register.html
│               │   │   └── Página de cadastro de usuários
│               │   │
│               │   ├── recoverpassword.html
│               │   │   └── Página de recuperação de senha
│               │   │
│               │   └── resetpassword.html
│               │       └── Página para redefinição da senha
│               │
│               ├── 📁 admin
│               │   └── admin.html
│               │       └── Página da área administrativa
│               │
│               └── 📁 user
│                   └── home.html
│                       └── Página inicial após autenticação
│
├── 🗄️ supabase
│   └── schema.sql
│       └── Script que cria a tabela "users" no Supabase
│
├── 📚 GuiaParaUsosFuturos
│   └── Guias em HTML para consulta
│
├── 🔒 .env
│   └── Credenciais reais (Supabase, Gmail e reCAPTCHA), não versionado
│
├── 📄 .env.example
│   └── Modelo do .env, sem valores sensíveis
│
├── 🚫 .gitignore
│   └── Arquivos e pastas ignorados pelo Git (.env, target/, etc.)
│
└── 📄 pom.xml
    └── Dependências e configurações do Maven
```

---

## Banco de dados (Supabase)

A aplicação conecta direto no PostgreSQL do Supabase via JDBC (Spring Data JPA + Hibernate). Não usa a biblioteca JavaScript nem a API REST do Supabase.

### Tabela `users`

| Coluna          | Tipo          | Descrição                                            |
|-----------------|---------------|------------------------------------------------------|
| `id`            | `bigint`      | Gerado pelo banco                                    |
| `email`         | `text`        | Único; usado como login. Sempre salvo em minúsculo   |
| `password_hash` | `text`        | Hash BCrypt da senha (a senha nunca é salva)         |
| `name`          | `text`        | Nome do usuário (aparece no e-mail de recuperação)   |
| `role`          | `text`        | `USER` ou `ADMIN` (sem o prefixo `ROLE_`)            |
| `cpf`           | `text`        | Só números (`123.456.789-00` vira `12345678900`)     |
| `rg`            | `text`        | Como foi digitado                                    |
| `address`       | `text`        | Campo "Endereço" do cadastro                         |
| `institution`   | `text`        | Campo "Instituição" do cadastro                      |
| `created_at`    | `timestamptz` | Preenchido pelo banco (`default now()`)              |

`cpf`, `rg`, `address` e `institution` aceitam `NULL` no banco porque o admin não tem esses dados. Para usuários comuns, a obrigatoriedade fica no formulário `/register` (atributo `required`).

Pontos importantes:
- **A tabela se chama `users`, não `user`.** `user` é palavra reservada do PostgreSQL e quebra as consultas do Hibernate.
- **O Hibernate não cria nem altera tabelas** (`spring.jpa.hibernate.ddl-auto=none`). Toda mudança de estrutura é feita por SQL no Supabase.
- **O RLS fica ligado sem nenhuma policy.** O Supabase expõe o schema `public` numa API REST acessível com a *anon key*, que é pública. Com o RLS ligado essa API fica bloqueada, e o Spring continua funcionando porque conecta como `postgres`, o dono da tabela.
- **Não crie usuários pelo painel do Supabase**: a senha precisa estar em hash BCrypt. Use a página `/register` (ou o `AdminSeeder`, no caso do admin).

### Adicionando campos ao usuário

Siga o mesmo caminho usado para CPF, RG, endereço e instituição. Exemplo com um campo de telefone:

1. **Supabase** (SQL Editor). Deixe aceitar `NULL`, senão a criação do admin quebra:
   ```sql
   alter table public.users add column phone text;
   ```
2. **`templates/login/register.html`**: adicione o `<input name="telefone" ...>` no formulário.
3. **`models/User.java`**: adicione o campo com `@Column`, o getter e o setter.
4. **`UserService.createUser`**: receba o valor e chame `user.setPhone(...)`.
5. **`SecureLoginController.handleRegister`**: adicione `@RequestParam("telefone") String telefone` e repasse para o `createUser`.
6. Adicione a coluna no `supabase/schema.sql` para os próximos projetos já nascerem com ela.

> Se o passo 1 for esquecido, **todo login e cadastro quebra** (`column ... does not exist`), porque o Hibernate passa a buscar a coluna nova em todas as consultas.

---

## Administrador

O admin fica na **mesma tabela `users`** dos usuários comuns. O que o diferencia é `role = 'ADMIN'`: com isso o login leva para `/admin`, e só ele acessa as rotas `/admin/**`.

**Como ele é criado:** o `AdminSeeder` roda toda vez que a aplicação sobe. Se o e-mail de `app.admin.username` ainda não existe no banco, ele cria o admin com a senha de `app.admin.password` (em hash BCrypt) e o nome de `app.admin.name`. Se já existe, não faz nada.

**Como entrar:** em `/login`, use o valor de `app.admin.username` no campo de usuário e o de `app.admin.password` na senha (no padrão, `admin` / `1234`).

Pontos importantes:
- O admin **não passa pelo `/register`**, então CPF, RG, endereço e instituição ficam `NULL` para ele.
- **Use um e-mail real** em `app.admin.username`. Com `admin`, o "Esqueceu sua senha?" não tem para onde mandar o link.
- **Trocar a senha no `application.properties` não altera o admin que já existe.** Use o "Esqueceu sua senha?" ou apague a linha do admin no Supabase e reinicie a aplicação.
- **Trocar o `app.admin.username` cria um admin novo**, e o antigo continua no banco. Apague o antigo no *Table Editor* se não for mais usar.
- **Para ter mais de um admin**, cadastre a pessoa pelo `/register` e rode no SQL Editor:
  ```sql
  update public.users set role = 'ADMIN' where email = 'email@da.pessoa';
  ```
  Para tirar o acesso, volte para `'USER'`. A mudança vale a partir do próximo login da pessoa.

---

## Como o login funciona

1. O usuário envia o formulário de `/login` (campos `username` e `password`).
2. O `RecaptchaFilter` valida o captcha no Google. Se falhar, volta para `/login?captcha=true`.
3. O Spring Security chama o `DatabaseUserDetailsService`, que busca o usuário no Supabase pelo e-mail.
4. O Spring compara a senha digitada com o `password_hash` usando o BCrypt.
5. Deu certo: `ADMIN` vai para `/admin` e `USER` vai para `/home`. Deu errado: vai para `/error`.

**Recuperação de senha:** `/recoverpassword` gera um token (válido por 15 minutos) e envia o link por e-mail; `/resetpassword?token=...` grava a nova senha no banco e invalida o token.

> Os tokens de recuperação ficam **em memória**. Se a aplicação reiniciar, os links já enviados deixam de funcionar.

---

## Organização dos templates

Os templates HTML foram agrupados em subpastas de acordo com sua área na aplicação, e os nomes das views retornadas pelo `SecureLoginController` foram atualizados de acordo:

| View                     | Caminho do template                  |
|--------------------------|---------------------------------------|
| `login/login`            | `templates/login/login.html`          |
| `login/register`         | `templates/login/register.html`       |
| `login/recoverpassword`  | `templates/login/recoverpassword.html`|
| `login/resetpassword`    | `templates/login/resetpassword.html`  |
| `error`                  | `templates/error.html`                |
| `admin/admin`            | `templates/admin/admin.html`          |
| `user/home`              | `templates/user/home.html`            |

As rotas (URLs) da aplicação **não foram alteradas**, apenas os nomes internos das views/templates.

---

## Configuração do application.properties

As credenciais sensíveis (Supabase, Gmail e Google reCAPTCHA) **não ficam hardcoded** no `application.properties`. Elas são lidas a partir de variáveis de ambiente, carregadas automaticamente de um arquivo `.env` na raiz do projeto (via a dependência [`spring-dotenv`](https://github.com/paulschwarz/spring-dotenv)).

```properties
spring.application.name=TelaLogin

# Administrador (criado no banco pelo AdminSeeder na primeira subida)
app.admin.username=admin
app.admin.password=1234
app.admin.name=Administrador

# Gmail
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

# Google reCAPTCHA
recaptcha.site-key=${RECAPTCHA_SITE_KEY}
recaptcha.secret-key=${RECAPTCHA_SECRET_KEY}

# Supabase (PostgreSQL)
spring.datasource.url=${SUPABASE_DB_URL:...}
spring.datasource.username=${SUPABASE_DB_USER:...}
spring.datasource.password=${SUPABASE_DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.hikari.maximum-pool-size=5
spring.jpa.hibernate.ddl-auto=none
spring.jpa.open-in-view=false
spring.jpa.show-sql=false
```

- `hikari.maximum-pool-size=5`: o plano gratuito do Supabase limita o número de conexões.
- `ddl-auto=none`: o Hibernate não mexe na estrutura do banco.
- `show-sql=true` mostra no console os SQLs executados, o que ajuda a depurar.
- Os usuários **não** ficam mais no `application.properties`. O antigo `app.user.*` foi removido; usuários comuns são criados pelo `/register`.

### Arquivo .env

Crie um arquivo `.env` na raiz do projeto (um modelo está disponível em `.env.example`) com o seguinte conteúdo:

```env
# https://myaccount.google.com/apppasswords
MAIL_USERNAME=seu-email@gmail.com
MAIL_PASSWORD=sua-senha-de-app

# https://www.google.com/recaptcha/admin
RECAPTCHA_SITE_KEY=sua-site-key
RECAPTCHA_SECRET_KEY=sua-secret-key

# Supabase > Connect > Direct > Session pooler (veja "Como usar este módulo em um projeto novo")
SUPABASE_DB_URL=jdbc:postgresql://aws-0-<regiao>.pooler.supabase.com:5432/postgres
SUPABASE_DB_USER=postgres.<project-ref>
SUPABASE_DB_PASSWORD=sua-senha-do-banco
```

O arquivo `.env` está listado no `.gitignore` e **não deve ser versionado**, já que contém credenciais reais.

---

## Dependências
```xml
<!-- Dependência do Spring Boot Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Dependência do Spring Boot Test -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- Dependência do Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Dependência do Thymeleaf para o Spring Boot -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>

<!-- Dependência do Spring Mail para o envio de email -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- Dependência para carregar variáveis do arquivo .env -->
<dependency>
    <groupId>me.paulschwarz</groupId>
    <artifactId>spring-dotenv</artifactId>
    <version>4.0.0</version>
</dependency>

<!-- Spring Data JPA (acesso ao banco com repositórios e entidades) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Driver do PostgreSQL (banco do Supabase) -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## Problemas comuns

| Erro / sintoma | Causa e solução |
|----------------|-----------------|
| `cannot read distributionUrl property in ./.mvn/wrapper/maven-wrapper.properties` | Falta a pasta `.mvn/`. Rode com `mvn spring-boot:run` ou gere o wrapper com `mvn wrapper:wrapper`. |
| `relation "users" does not exist` | O `supabase/schema.sql` não foi rodado nesse projeto do Supabase. |
| `column "..." does not exist` (ex.: `u1_0.address`) | A entidade `User` tem um campo que a tabela não tem. Crie a coluna com `alter table public.users add column ... text;`. |
| `syntax error at or near "user"` | A tabela ou o `@Table` está como `user`. Use `users`. |
| `password authentication failed` | Senha do banco errada, ou o usuário não está no formato `postgres.<project-ref>`. |
| Timeout / `UnknownHostException` ao conectar | Está usando a "Direct connection" (só IPv6). Use a string do **Session pooler**. |
| `No qualifying bean of type UserRepository` / `Not a managed type` | Pacote renomeado sem atualizar `@EntityScan` / `@EnableJpaRepositories` no `TelaLoginApplication`. |
| `Max client connections reached` | Muitas conexões abertas no plano gratuito. Mantenha o pool pequeno e não rode várias instâncias ao mesmo tempo. |
| Login sempre vai para `/error` | Usuário não existe no banco ou senha errada. Confira a tabela no *Table Editor*. |
| Login volta com "confirme que você não é um robô" | Chaves do reCAPTCHA erradas ou o domínio (`localhost`) não está cadastrado no painel do reCAPTCHA. |
| Mudei `app.admin.password` e a senha do admin não mudou | O `AdminSeeder` só **cria** o admin, não atualiza. Use "Esqueceu sua senha?" ou apague a linha do admin no Supabase e reinicie. |
| A página mostra `{"message":"Ocorreu um erro inesperado"}` | Veja o console: o `GlobalExceptionHandler` registra o erro completo lá. |
| O teste `contextLoads` falha | Ele sobe a aplicação inteira e conecta no Supabase real. Precisa do `.env` preenchido e de internet. |

---

# Thymeleaf

Thymeleaf é um motor de templates para Java que permite a criação de páginas HTML dinâmicas de forma simples e eficiente. Ele é frequentemente utilizado em aplicações Spring, proporcionando uma maneira intuitiva de gerar conteúdo HTML e manipular dados diretamente nas páginas.

## Principais Características

- **Natural Templating**: Os templates Thymeleaf são válidos como documentos HTML, permitindo que sejam visualizados em navegadores sem processamento.
- **Integração com Spring**: Thymeleaf se integra perfeitamente com o Spring Framework, facilitando a injeção de dependências e o acesso a beans do Spring.
- **Expressões de Template**: Utiliza uma sintaxe simples e expressiva para manipular dados, permitindo a criação de lógicas condicionais e loops diretamente nas páginas.

## Exemplo de Uso

Aqui está um exemplo simples de um template Thymeleaf:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Exemplo Thymeleaf</title>
</head>
<body>
    <h1 th:text="${titulo}">Título do Documento</h1>
    <ul>
        <li th:each="item : ${itens}" th:text="${item}"></li>
    </ul>
</body>
</html>
```

Neste exemplo, o título e a lista de itens são preenchidos dinamicamente com dados fornecidos pelo controlador Spring.

Thymeleaf é uma escolha poderosa para desenvolvedores que desejam criar interfaces web dinâmicas e interativas em aplicações Java. Com sua sintaxe intuitiva e forte integração com o Spring, ele se tornou uma ferramenta popular no ecossistema de desenvolvimento Java.

> Nos formulários, use sempre `th:action="@{/rota}"` em vez de `action="/rota"`: o `th:action` adiciona automaticamente o token CSRF exigido pelo Spring Security. Sem ele, o POST retorna erro 403.

## Interface Gráfica

A interface gráfica permite ao usuário inserir seus dados de login e, após a autenticação, ser redirecionado para a página correspondente, onde terá acesso às funcionalidades e informações de acordo com suas credenciais.

### Captura de Tela

- **Login**: A página de login possui campos para inserir o e-mail e a senha. Ela também exibe o logo da aplicação, proporcionando uma identificação visual clara. Abaixo do formulário de login, existem links para os usuários que ainda não possuem cadastro, direcionando-os para a página de registro, e para aqueles que esqueceram a senha, levando-os à página de recuperação de senha.

- **Register**: A página de registro permite que novos usuários criem uma conta na plataforma. Ela inclui campos para inserir **nome completo, e-mail, CPF, RG, endereço, instituição e senha**, e todos são salvos na tabela `users` do Supabase. A lateral exibe o **logo da aplicação**, mantendo a identidade visual do sistema. Abaixo do formulário, há um link para os usuários que já possuem conta, direcionando-os de volta para a página de login.

| <img src="imgs/Login.png" alt="Login" width="1000"/> |
|:----------------------------------------------------:|
|                        Login                         |

| <img src="imgs/Register.png" alt="Register" width="1000"/> |
|:-------------------------------------------------------:|
|                        Register                         |

| <img src="imgs/Email.png" alt="Recover" width="1000"/> |
|:---------------------------------------------------------------:|
|                        Recover Password                         |

## Métodos da Classe SecurityConfig

### @Configuration
Indica que a classe contém métodos de configuração que geram beans para o contexto da aplicação.

### @EnableWebSecurity
Ativa a segurança da web, permitindo a configuração de regras de segurança para as URLs da aplicação.

### public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
Configura as regras de segurança das requisições HTTP: libera as páginas públicas (login, cadastro, recuperação de senha, erro, CSS e imagens), restringe `/admin/**` ao papel ADMIN, registra o filtro do reCAPTCHA e define para onde cada perfil vai após o login.

### public PasswordEncoder passwordEncoder()
Define o codificador de senhas a ser utilizado na aplicação, utilizando o BCryptPasswordEncoder. É usado para gerar o hash no cadastro e para conferir a senha no login.

### De onde vêm os usuários?
Antes existia aqui um `userDetailsService()` com usuários em memória (`InMemoryUserDetailsManager`). Ele foi removido. Agora quem entrega os usuários ao Spring Security é a classe `DatabaseUserDetailsService`, que busca no Supabase. O Spring a encontra sozinho por ser o único `UserDetailsService` da aplicação.

## Urls do projeto:
http://localhost:8080/login

http://localhost:8080/login?logout=true

http://localhost:8080/home

http://localhost:8080/admin

http://localhost:8080/error

http://localhost:8080/register

http://localhost:8080/recoverpassword

http://localhost:8080/resetpassword?token=... (link enviado por e-mail)

---

## Como usar este módulo em um projeto novo

Siga as etapas na ordem: **criar o banco → pegar a conexão → preencher o `.env` → ajustar o admin → rodar**.

### 1. Criar o banco no Supabase

1. Crie um projeto em [supabase.com](https://supabase.com). Na criação ele pede uma **Database Password**: guarde, ela vai para o `.env`.
   - Esqueceu? *Project Settings > Database > Reset database password*.
2. Abra o **SQL Editor > New query**, cole todo o conteúdo de [`supabase/schema.sql`](supabase/schema.sql) e clique em **Run**.
3. Confira no **Table Editor** se a tabela `users` apareceu.

### 2. Pegar os dados de conexão

1. Clique em **Connect**, no topo do painel do Supabase.
2. Escolha **Direct** (*Connection string*) e selecione o **Session pooler**.
3. Copie a string que aparece. Ela tem este formato:

```text
postgresql://postgres.abcdefghijklmnop:[YOUR-PASSWORD]@aws-0-sa-east-1.pooler.supabase.com:5432/postgres
             └─────── usuário ───────┘ └─── senha ───┘ └────────────── endereço do banco ──────────────┘
```

Separe em três partes e coloque no `.env`:

| Variável               | O que colocar                                        | Exemplo                                                               |
|------------------------|------------------------------------------------------|-----------------------------------------------------------------------|
| `SUPABASE_DB_URL`      | `jdbc:postgresql://` + tudo o que vem depois do `@`  | `jdbc:postgresql://aws-0-sa-east-1.pooler.supabase.com:5432/postgres` |
| `SUPABASE_DB_USER`     | o que vem entre `postgresql://` e o `:`              | `postgres.abcdefghijklmnop`                                           |
| `SUPABASE_DB_PASSWORD` | a senha do banco da etapa 1 (sem os colchetes)       | `minhaSenha123`                                                       |

> Use o **Session pooler**, não a "Direct connection": a conexão direta só funciona em redes IPv6 e costuma dar timeout.

### 3. Preencher o `.env`

Copie o `.env.example` para `.env`, na raiz do projeto, e preencha:

- **Supabase**: os três valores da etapa 2.
- **Gmail**: seu e-mail e uma [senha de app](https://myaccount.google.com/apppasswords). Precisa ter a verificação em duas etapas ativada.
- **reCAPTCHA**: crie as chaves em [google.com/recaptcha/admin](https://www.google.com/recaptcha/admin), tipo **reCAPTCHA v2 > "Não sou um robô"**, e adicione `localhost` em *Domínios* (depois, o domínio real também).

### 4. Ajustar o administrador

No `application.properties`, troque os dados de `app.admin.*`. Use um **e-mail real** em `app.admin.username` para conseguir recuperar a senha do admin. Mais detalhes em [Administrador](#administrador).

### 5. Rodar

```bash
mvn spring-boot:run
```

> O `./mvnw` **não funciona** neste projeto porque a pasta `.mvn/` não existe. Use o `mvn` instalado, gere o wrapper com `mvn wrapper:wrapper` ou rode pelo *Spring Boot Dashboard* do VS Code.

Na primeira vez, o console mostra `Administrador criado no banco: ...`. Depois disso:

- **Admin**: entre em http://localhost:8080/login com o usuário e a senha de `app.admin.*`.
- **Usuário comum**: cadastre-se em http://localhost:8080/register.

### O que personalizar em cada projeto

| O quê | Onde |
|-------|------|
| Dados do administrador | `app.admin.*` no `application.properties` |
| Credenciais (banco, Gmail, reCAPTCHA) | `.env` |
| Valores padrão do Supabase (o que vem depois do `:` em `spring.datasource.url` e `username`) | `application.properties`. Apontam para o projeto original; troque ou apague |
| Link do e-mail de recuperação (`http://localhost:8080/resetpassword?token=`) | `SecureLoginController`, método `handleRecoverPassword`. Troque pelo domínio real em produção |
| Texto/visual do e-mail ("TELA LOGIN", cores) | HTML dentro de `handleRecoverPassword` no `SecureLoginController` |
| Logo | `src/main/resources/static/images/teste.png` |
| Nome da aplicação | `spring.application.name` e `pom.xml` |
| Nome do pacote (`com.example.TelaLogin`) | Se renomear, atualize também `scanBasePackages`, `@EntityScan` e `@EnableJpaRepositories` no `TelaLoginApplication` |
| Novas páginas públicas | Adicione uma linha `permitAll()` no `SecurityConfig` |
| Novos campos do usuário (CPF, RG...) | Veja [Adicionando campos ao usuário](#adicionando-campos-ao-usuário) |

---

## Licença
Este projeto está licenciado sob a MIT License.
