package com.sistemaposjcs.sistemaposjcs.modules.reglas.repository;

import com.sistemaposjcs.sistemaposjcs.modules.modulos.model.Modulo;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.model.Regla;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

/** Valida el HQL real, DTOs y propiedades sin simular el parser ni requerir una base. */
class ReglaQueryValidationTest {
    @Test
    void consultasDeclaradasSonValidasParaLasEntidadesYProyecciones() {
        Configuration configuration = new Configuration()
                .addAnnotatedClass(Regla.class).addAnnotatedClass(Modulo.class).addAnnotatedClass(Usuario.class)
                .setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
                .setProperty("hibernate.boot.allow_jdbc_metadata_access", "false")
                .setProperty("hibernate.connection.provider_class",
                        "org.hibernate.engine.jdbc.connections.internal.UserSuppliedConnectionProviderImpl")
                .setProperty("hibernate.hbm2ddl.auto", "none");
        try (var factory = configuration.buildSessionFactory(); var session = factory.openSession()) {
            for (var method : ReglaRepository.class.getMethods()) {
                Query query = method.getAnnotation(Query.class);
                if (query == null || query.nativeQuery()) continue;
                if (query.value().stripLeading().startsWith("update")) session.createMutationQuery(query.value());
                else session.createQuery(query.value(), Object.class);
                if (!query.countQuery().isBlank()) session.createQuery(query.countQuery(), Long.class);
            }
        }
    }
}