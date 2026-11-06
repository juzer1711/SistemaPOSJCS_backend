package com.sistemaposjcs.sistemaposjcs.service;

import java.util.List;

import org.springframework.stereotype.Service;
import com.sistemaposjcs.sistemaposjcs.model.Categoria;
import com.sistemaposjcs.sistemaposjcs.repository.CategoriaRepository;
import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.service.UsuarioAuthService;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;

@Service
public class CategoriaService {
    private final  CategoriaRepository categoriaRepository;
    private final AuditoriaService auditoriaService;
private final UsuarioAuthService usuarioAuthService;

    public CategoriaService(
            CategoriaRepository categoriaRepository,
            AuditoriaService auditoriaService,
            UsuarioAuthService usuarioAuthService
    ) {
        this.categoriaRepository = categoriaRepository;
        this.auditoriaService = auditoriaService;
        this.usuarioAuthService = usuarioAuthService;
    }

// Obtener categoria por ID
    public Categoria getCategoriaById(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrado"));
    }

    public List<Categoria> getCategorias() {
        return categoriaRepository.findAll();
    }

// Crear categoria
    public Categoria createCategoria(Categoria categoria) {

        Categoria categoriaCreada = categoriaRepository.save(categoria);

        try {

            Usuario administrador = usuarioAuthService.getUsuarioLogueado();

            String descripcion =
                    "Categoría creada.\n" +
                    "Nombre: " + categoriaCreada.getNombre() + ".";

            auditoriaService.registrar(
                    administrador,
                    "CATEGORIA",
                    "CREAR",
                    descripcion
            );

        } catch (Exception e) {
            System.out.println("Error auditoría categoría: " + e.getMessage());
        }

        return categoriaCreada;
    }
// Actualizar categoria
    public Categoria updateCategoria(Long id, Categoria categoriaDetails) {

        Categoria categoria = getCategoriaById(id);

        String nombreAnterior = categoria.getNombre();

        categoria.setNombre(categoriaDetails.getNombre());

        Categoria categoriaActualizada = categoriaRepository.save(categoria);

        try {

            Usuario administrador = usuarioAuthService.getUsuarioLogueado();

            if (!nombreAnterior.equals(categoriaActualizada.getNombre())) {

                String descripcion =
                        "Categoría '" + categoriaActualizada.getNombre() + "'.\n" +
                        "Nombre: '" + nombreAnterior +
                        "' → '" + categoriaActualizada.getNombre() + "'";

                auditoriaService.registrar(
                        administrador,
                        "CATEGORIA",
                        "ACTUALIZAR",
                        descripcion
                );
            }

        } catch (Exception e) {
            System.out.println("Error auditoría categoría: " + e.getMessage());
        }

        return categoriaActualizada;
    }
}
