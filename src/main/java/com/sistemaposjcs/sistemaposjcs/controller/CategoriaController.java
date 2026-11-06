package com.sistemaposjcs.sistemaposjcs.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sistemaposjcs.sistemaposjcs.dto.CategoriaDTO;
import com.sistemaposjcs.sistemaposjcs.model.Categoria;
import com.sistemaposjcs.sistemaposjcs.service.CategoriaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }
    @GetMapping
public List<CategoriaDTO> getCategorias() {
    return categoriaService.getCategorias()
        .stream()
        .map(c -> new CategoriaDTO(
            c.getId(),
            c.getNombre()))
        .toList();
}
    @GetMapping("/{id}")
    public Categoria getCategoriaById(@PathVariable Long id) {
        return categoriaService.getCategoriaById(id);
    }

    @PostMapping
    public ResponseEntity<Categoria> createCategoria(@Valid @RequestBody Categoria categoria) {
        return ResponseEntity.ok(categoriaService.createCategoria(categoria));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Categoria> updateCategoria(@PathVariable Long id, @Valid @RequestBody Categoria categoria) {
        return ResponseEntity.ok(categoriaService.updateCategoria(id, categoria));
    }
}
