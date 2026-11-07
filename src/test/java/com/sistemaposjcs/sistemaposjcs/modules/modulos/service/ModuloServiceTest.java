package com.sistemaposjcs.sistemaposjcs.modules.modulos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sistemaposjcs.sistemaposjcs.modules.modulos.dto.ModuloRequest;
import com.sistemaposjcs.sistemaposjcs.modules.modulos.model.Modulo;
import com.sistemaposjcs.sistemaposjcs.modules.modulos.repository.ModuloRepository;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.service.ReglaService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ModuloServiceTest {

    @Mock
    private ModuloRepository moduloRepository;

    @Mock
    private ReglaService reglaService;

    @InjectMocks
    private ModuloService moduloService;

    @Captor
    private ArgumentCaptor<Specification<Modulo>> specificationCaptor;

    @Mock
    private Root<Modulo> root;

    @Mock
    private Path<String> nombrePath;

    @Mock
    private Path<String> zonaPath;

    @Mock
    private Expression<String> nombreLower;

    @Mock
    private Expression<String> zonaLower;

    @Test
    void ordenarIdDescartaIgnoreCaseDeLaSolicitud() {
        when(moduloRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());
        moduloService.listar(PageRequest.of(0, 10, Sort.by(Sort.Order.desc("idModulo").ignoreCase())));
        verify(moduloRepository).findAll(PageRequest.of(0, 10, Sort.by(Sort.Order.desc("idModulo"))));
    }
    @Test
    void disponibilidadDistingueCodigosOcupadosYLibres() {
        when(moduloRepository.existsByCodigoModulo("MODULOS")).thenReturn(true);
        when(moduloRepository.existsByCodigoModulo("GESTION_PRODUCTOS")).thenReturn(false);
        assertEquals(false, moduloService.codigoDisponible("MODULOS"));
        assertEquals(true, moduloService.codigoDisponible("GESTION_PRODUCTOS"));
    }

    @Test
    void disponibilidadRechazaCodigosInvalidosSinConsultar() {
        for (String codigo : new String[]{"", "123", "minusculas", "CON ESPACIOS", "A".repeat(51)}) {
            assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                    () -> moduloService.codigoDisponible(codigo)).getStatusCode());
        }
        verify(moduloRepository, never()).existsByCodigoModulo(any());
    }

    @Test
    void ordenarTextoAgregaDesempateYLimitaTamano() {
        when(moduloRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());
        moduloService.listar(PageRequest.of(2, 200, Sort.by(Sort.Order.desc("zona"))));
        verify(moduloRepository).findAll(PageRequest.of(2, 100,
                Sort.by(Sort.Order.desc("zona").ignoreCase(), Sort.Order.asc("idModulo"))));
    }

    @Test
    void ordenarIdConservaOrdenNumericoSinDesempateDuplicado() {
        when(moduloRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("idModulo")));
        moduloService.listar(pageable);
        verify(moduloRepository).findAll(pageable);
    }

    @Test
    void rechazaOrdenarPorCampoNoPermitido() {
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> moduloService.listar(PageRequest.of(0, 10, Sort.by("reglas.usuario"))))
                .getStatusCode());
        verify(moduloRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void crearRechazaCodigoDuplicadoAunqueAntesEstuvieraDisponible() {
        when(moduloRepository.existsByCodigoModulo("MODULOS")).thenReturn(false, true);
        assertEquals(true, moduloService.codigoDisponible("MODULOS"));
        assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class,
                () -> moduloService.crear(new ModuloRequest("MODULOS", "archivo", "Módulos", "Sistema", null)))
                .getStatusCode());
        verify(moduloRepository, never()).save(any(Modulo.class));
    }
    @Test
    void crearRecortaCamposYConvierteComentariosVaciosANull() {
        when(moduloRepository.existsByCodigoModulo("PRODUCTOS")).thenReturn(false);
        when(moduloRepository.existsByNombreModuloIgnoreCase("Productos")).thenReturn(false);
        when(moduloRepository.save(any(Modulo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Modulo modulo = moduloService.crear(
                new ModuloRequest("PRODUCTOS", " ProductoController.java ", " Productos ", " Catálogo ", "  ")
        );

        assertEquals("PRODUCTOS", modulo.getCodigoModulo());
        assertEquals("ProductoController.java", modulo.getArchivo());
        assertEquals("Productos", modulo.getNombreModulo());
        assertEquals("Catálogo", modulo.getZona());
        assertNull(modulo.getComentarios());
    }

    @Test
    void crearRechazaNombresDuplicadosSinGuardar() {
        when(moduloRepository.existsByCodigoModulo("PRODUCTOS")).thenReturn(false);
        when(moduloRepository.existsByNombreModuloIgnoreCase("Productos")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> moduloService.crear(
                        new ModuloRequest("PRODUCTOS", "ProductoController.java", "Productos", "Catálogo", null)
                )
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(moduloRepository, never()).save(any(Modulo.class));
    }

    @Test
    void buscarEntregaLosFiltrosAlRepositorioConPaginacion() {
        Pageable pageable = PageRequest.of(1, 20);
        when(moduloRepository.findAll(ArgumentMatchers.<Specification<Modulo>>any(), any(Pageable.class)))
                .thenReturn(Page.empty(pageable));

        moduloService.buscar(pageable, "  prod  ", "Catálogo");

        verify(moduloRepository).findAll(specificationCaptor.capture(), eq(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Order.asc("nombreModulo").ignoreCase(), Sort.Order.asc("idModulo")))));
        assertNotNull(specificationCaptor.getValue());
    }

    @Test
    void buscarUsaPrefijosInsensiblesAEspaciosYEscapaComodines() {
        Pageable pageable = PageRequest.of(0, 20);
        when(moduloRepository.findAll(ArgumentMatchers.<Specification<Modulo>>any(), any(Pageable.class)))
                .thenReturn(Page.empty(pageable));

        moduloService.buscar(pageable, " ProD%_\\ ", " Catálogo ");

        verify(moduloRepository).findAll(specificationCaptor.capture(), eq(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Order.asc("nombreModulo").ignoreCase(), Sort.Order.asc("idModulo")))));

        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        Predicate nombrePredicate = mock(Predicate.class);
        Predicate zonaPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        when(root.<String>get("nombreModulo")).thenReturn(nombrePath);
        when(root.<String>get("zona")).thenReturn(zonaPath);
        when(criteriaBuilder.lower(nombrePath)).thenReturn(nombreLower);
        when(criteriaBuilder.lower(zonaPath)).thenReturn(zonaLower);
        when(criteriaBuilder.like(nombreLower, "prod\\%\\_\\\\%", '\\')).thenReturn(nombrePredicate);
        when(criteriaBuilder.like(zonaLower, "catálogo%", '\\')).thenReturn(zonaPredicate);
        doReturn(combinedPredicate).when(criteriaBuilder).and(any(Predicate[].class));

        assertEquals(
                combinedPredicate,
                specificationCaptor.getValue().toPredicate(root, null, criteriaBuilder)
        );
    }
}
