package com.sistemaposjcs.sistemaposjcs.modules.reglas.repository;

import com.sistemaposjcs.sistemaposjcs.modules.reglas.model.Regla;
import java.util.Optional;
import java.util.List;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.dto.PermisoModuloResponse;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.dto.MatrizPermisoResponse;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.dto.ReglaUsuarioOpcion;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.dto.ReglaModuloOpcion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReglaRepository extends JpaRepository<Regla, Long>, JpaSpecificationExecutor<Regla> {

    Optional<Regla> findByUsuario_UsernameAndModulo_CodigoModulo(String username, String codigoModulo);

    boolean existsByUsuario_IdUsuarioAndModulo_IdModulo(Long usuarioId, Long moduloId);

    boolean existsByUsuario_IdUsuarioAndModulo_IdModuloAndIdReglaNot(
            Long usuarioId,
            Long moduloId,
            Long idRegla
    );

    void deleteByModulo_IdModulo(Long moduloId);

    @Query("""
            select new com.sistemaposjcs.sistemaposjcs.modules.autorizacion.dto.PermisoModuloResponse(
                m.codigoModulo, r.reglaConsulta, r.reglaInsercion, r.reglaActualizacion, r.reglaReporte, r.reglaDelete)
            from Regla r join r.modulo m where r.usuario.username = :username
            """)
    List<PermisoModuloResponse> permisosDeUsuario(@Param("username") String username);

    @Query(value = """
            select new com.sistemaposjcs.sistemaposjcs.modules.reglas.dto.MatrizPermisoResponse(
                r.idRegla, m.idModulo, m.codigoModulo, m.nombreModulo,
                coalesce(r.reglaConsulta, false), coalesce(r.reglaInsercion, false),
                coalesce(r.reglaActualizacion, false), coalesce(r.reglaReporte, false), coalesce(r.reglaDelete, false))
            from Modulo m left join Regla r on r.modulo = m and r.usuario.idUsuario = :usuarioId
            where (:moduloId is null or m.idModulo = :moduloId)
            order by lower(m.nombreModulo), m.idModulo
            """, countQuery = "select count(m) from Modulo m where (:moduloId is null or m.idModulo = :moduloId)")
    Page<MatrizPermisoResponse> matriz(@Param("usuarioId") Long usuarioId, @Param("moduloId") Long moduloId, Pageable pageable);

    @Query("""
            select new com.sistemaposjcs.sistemaposjcs.modules.reglas.dto.ReglaUsuarioOpcion(u.idUsuario, u.username)
            from Usuario u where lower(u.username) like :patron escape '\\'
            order by lower(u.username), u.idUsuario
            """)
    Page<ReglaUsuarioOpcion> usuarios(@Param("patron") String patron, Pageable pageable);

    @Query("""
            select new com.sistemaposjcs.sistemaposjcs.modules.reglas.dto.ReglaModuloOpcion(m.idModulo, m.codigoModulo, m.nombreModulo)
            from Modulo m where lower(m.nombreModulo) like :patron escape '\\'
                or lower(m.codigoModulo) like :patron escape '\\'
            order by lower(m.nombreModulo), m.idModulo
            """)
    Page<ReglaModuloOpcion> modulos(@Param("patron") String patron, Pageable pageable);

    // PostgreSQL: completa relaciones legacy faltantes sin carreras entre solicitudes.
    @Modifying
    @Query(value = """
            insert into reglas (usuario_id, modulo_id, regla_consulta, regla_insercion,
                regla_actualizacion, regla_reporte, regla_delete)
            values (:usuarioId, :moduloId, false, false, false, false, false)
            on conflict (usuario_id, modulo_id) do nothing
            """, nativeQuery = true)
    int asegurarRegla(@Param("usuarioId") Long usuarioId, @Param("moduloId") Long moduloId);

    // CASE conserva el resto de valores actuales en la base, incluso ante cambios concurrentes.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Regla r set
                r.reglaConsulta = case when :accion = 'CONSULTA' then :valor else r.reglaConsulta end,
                r.reglaInsercion = case when :accion = 'INSERCION' then :valor else r.reglaInsercion end,
                r.reglaActualizacion = case when :accion = 'ACTUALIZACION' then :valor else r.reglaActualizacion end,
                r.reglaReporte = case when :accion = 'REPORTE' then :valor else r.reglaReporte end,
                r.reglaDelete = case when :accion = 'DELETE' then :valor else r.reglaDelete end
            where r.usuario.idUsuario = :usuarioId and r.modulo.idModulo = :moduloId
            """)
    int cambiarPermiso(@Param("usuarioId") Long usuarioId, @Param("moduloId") Long moduloId,
            @Param("accion") String accion, @Param("valor") boolean valor);
    @Modifying(flushAutomatically = true)
    @Query(value = """
            insert into reglas (usuario_id, modulo_id, regla_consulta, regla_insercion,
                regla_actualizacion, regla_reporte, regla_delete)
            select u.id_usuario, :moduloId, false, false, false, false, false from usuarios u
            on conflict (usuario_id, modulo_id) do nothing
            """, nativeQuery = true)
    int crearFalseParaModulo(@Param("moduloId") Long moduloId);

    @Modifying(flushAutomatically = true)
    @Query(value = """
            insert into reglas (usuario_id, modulo_id, regla_consulta, regla_insercion,
                regla_actualizacion, regla_reporte, regla_delete)
            select :usuarioId, m.id_modulo, false, false, false, false, false from modulos m
            on conflict (usuario_id, modulo_id) do nothing
            """, nativeQuery = true)
    int crearFalseParaUsuario(@Param("usuarioId") Long usuarioId);
}

