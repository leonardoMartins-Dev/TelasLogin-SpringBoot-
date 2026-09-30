package com.example.TelaLogin;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * TESTE DE "FUMAÇA": VERIFICA SE A APLICAÇÃO CONSEGUE SUBIR.
 *
 * @SpringBootTest sobe o contexto inteiro do Spring (como no main), e o teste
 * contextLoads() passa se nada falhar durante a subida: beans criados,
 * propriedades encontradas, repositórios e entidades mapeados.
 *
 * ATENÇÃO: agora que a aplicação usa o Supabase, subir o contexto significa
 * CONECTAR NO BANCO DE VERDADE (com as credenciais do .env). Então este teste:
 *   - falha se não houver internet ou se o .env não estiver preenchido;
 *   - executa o AdminSeeder, que pode criar o admin no banco real.
 * Para testes isolados, o comum é usar um banco separado (ex.: H2 em memória
 * ou Testcontainers com PostgreSQL) em um application-test.properties.
 *
 * A classe principal é informada em "classes" porque ela está em outro pacote
 * (com.example.TelaLogin.application) e o teste não a encontraria sozinho.
 */
@SpringBootTest(classes={com.example.TelaLogin.application.TelaLoginApplication.class})
class TelaLoginApplicationTests {

	@Test
	void contextLoads() {
	}

}
