package com.sistemaposjcs.sistemaposjcs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sistemaposjcs.sistemaposjcs.model.Categoria;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    Optional<Categoria> findByNombre(String nombre);
}