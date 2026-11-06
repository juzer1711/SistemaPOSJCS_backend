package com.sistemaposjcs.sistemaposjcs.modules.modulos.repository;

import com.sistemaposjcs.sistemaposjcs.modules.modulos.model.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ModuloRepository extends JpaRepository<Modulo, Long>, JpaSpecificationExecutor<Modulo> {

    @Query("select (count(m) > 0) from Modulo m where lower(m.nombreModulo) = lower(:nombreModulo)")
    boolean existsByNombreModuloIgnoreCase(@Param("nombreModulo") String nombreModulo);

    @Query("select (count(m) > 0) from Modulo m where lower(m.nombreModulo) = lower(:nombreModulo) and m.idModulo <> :idModulo")
    boolean existsByNombreModuloIgnoreCaseAndIdModuloNot(
            @Param("nombreModulo") String nombreModulo, @Param("idModulo") Long idModulo);

    boolean existsByCodigoModulo(String codigoModulo);
}
