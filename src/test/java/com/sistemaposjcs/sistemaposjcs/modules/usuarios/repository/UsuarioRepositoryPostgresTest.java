package com.sistemaposjcs.sistemaposjcs.modules.usuarios.repository;

import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.service.UsuarioService;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.dto.UsuarioResponse;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.service.ReglaService;
import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.service.UsuarioAuthService;
import com.sistemaposjcs.sistemaposjcs.service.AuditoriaService;
import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoDocumento;
import java.sql.DriverManager;
import java.util.*;
import org.hibernate.cfg.Configuration;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Prueba optativa en PostgreSQL real; crea y elimina exclusivamente un esquema temporal propio. */
@EnabledIfEnvironmentVariable(named = "POS_TEST_DB_URL", matches = ".+")
class UsuarioRepositoryPostgresTest {
    @Test
    void proyeccionesFiltrosOrdenYExportacionPorClaveFuncionanSinSeleccionarPassword() throws Exception {
        var hibernateLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("org.hibernate");
        hibernateLogger.setLevel(ch.qos.logback.classic.Level.ERROR);
        String url = System.getenv("POS_TEST_DB_URL"), user = System.getenv("POS_TEST_DB_USER"), password = System.getenv("POS_TEST_DB_PASSWORD");
        String schema = "usuarios_test_" + UUID.randomUUID().toString().replace("-", "");
        if (!schema.matches("usuarios_test_[a-f0-9]{32}")) throw new IllegalStateException("Esquema inválido");
        try (var connection = DriverManager.getConnection(url, user, password); var statement = connection.createStatement()) {
            statement.execute("create schema " + schema);
            try {
                List<String> sql = new ArrayList<>();
                Configuration cfg = new Configuration().addAnnotatedClass(Usuario.class)
                        .setProperty("hibernate.connection.url", url).setProperty("hibernate.connection.username", user)
                        .setProperty("hibernate.connection.password", password).setProperty("hibernate.default_schema", schema)
                        .setProperty("hibernate.hbm2ddl.auto", "create-drop");
                cfg.getProperties().put("hibernate.session_factory.statement_inspector", (StatementInspector) query -> { sql.add(query); return query; });
                try (var factory = cfg.buildSessionFactory(); var em = factory.createEntityManager()) {
                    em.getTransaction().begin();
                    for (int i = 0; i < 520; i++) {
                        Usuario value = new Usuario(); value.setUsername(i == 0 ? "persona_50%" : "persona" + i);
                        value.setPassword("hash-sintetico"); value.setPrimerNombre("Ana"); value.setPrimerApellido("Pérez");
                        value.setDocumento(String.format("%08d", 10000000 + i)); value.setEmail("persona" + i + "@example.test");
                        value.setTelefono("3001234567"); value.setTipoDocumento(TipoDocumento.NIT); value.setEstado(i % 2 == 0);
                        em.persist(value);
                    }
                    em.flush(); em.clear(); sql.clear();
                    UsuarioRepository repository = new JpaRepositoryFactory(em).getRepository(UsuarioRepository.class);
                    UsuarioService service = new UsuarioService(repository, mock(AuditoriaService.class), mock(UsuarioAuthService.class),
                            mock(PasswordEncoder.class), mock(ReglaService.class));
                    assertEquals(1, service.searchUsuarios(PageRequest.of(0, 10), "_50%", null, null).getTotalElements());
                    assertEquals(520, service.searchUsuarios(PageRequest.of(0, 5, Sort.by("nombreCompleto")), "Ana Pérez", null, null).getTotalElements());
                    assertEquals(1, service.searchUsuarios(PageRequest.of(0, 10), "persona11@example", false, TipoDocumento.NIT).getTotalElements());
                    assertEquals(1, service.searchUsuarios(PageRequest.of(0, 10), "10000005", null, null).getTotalElements());
                    assertTrue(sql.stream().filter(query -> query.toLowerCase().startsWith("select")).noneMatch(query -> query.toLowerCase().contains("password")));
                    sql.clear();
                    List<UsuarioResponse> exported = new ArrayList<>();
                    service.recorrerUsuarios(null, null, null, exported::add);
                    assertTrue(sql.stream().filter(query -> query.toLowerCase().startsWith("select")).count() >= 2);
                    assertTrue(sql.stream().noneMatch(query -> query.toLowerCase().contains("count(")));
                    assertEquals(520, exported.size()); assertEquals("Ana Pérez", exported.getFirst().nombreCompleto());
                    assertTrue(sql.stream().filter(query -> query.toLowerCase().startsWith("select")).noneMatch(query -> query.toLowerCase().contains("password")));
                    em.getTransaction().rollback();
                }
            } finally { statement.execute("drop schema " + schema + " cascade"); }
        }
    }
}