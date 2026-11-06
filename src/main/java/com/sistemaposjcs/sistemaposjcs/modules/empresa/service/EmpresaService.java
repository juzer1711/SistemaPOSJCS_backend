package com.sistemaposjcs.sistemaposjcs.modules.empresa.service;

import com.sistemaposjcs.sistemaposjcs.modules.autenticacion.service.UsuarioAuthService;
import com.sistemaposjcs.sistemaposjcs.modules.empresa.dto.EmpresaRequest;
import com.sistemaposjcs.sistemaposjcs.modules.empresa.dto.EmpresaResponse;
import com.sistemaposjcs.sistemaposjcs.modules.empresa.model.Empresa;
import com.sistemaposjcs.sistemaposjcs.modules.empresa.repository.EmpresaRepository;
import com.sistemaposjcs.sistemaposjcs.modules.usuarios.model.Usuario;
import com.sistemaposjcs.sistemaposjcs.service.AuditoriaService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmpresaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmpresaService.class);

    private final EmpresaRepository empresaRepository;
    private final EmpresaLogoStorage logoStorage;
    private final AuditoriaService auditoriaService;
    private final UsuarioAuthService usuarioAuthService;
    private final String bootstrapToken;

    public EmpresaService(
            EmpresaRepository empresaRepository,
            EmpresaLogoStorage logoStorage,
            AuditoriaService auditoriaService,
            UsuarioAuthService usuarioAuthService,
            @Value("${app.empresa.bootstrap-token:}") String bootstrapToken
    ) {
        this.empresaRepository = empresaRepository;
        this.logoStorage = logoStorage;
        this.auditoriaService = auditoriaService;
        this.usuarioAuthService = usuarioAuthService;
        this.bootstrapToken = bootstrapToken;
    }

    @Transactional(readOnly = true)
    public EmpresaResponse obtenerEmpresa() {
        return EmpresaResponse.from(obtenerEmpresaExistente());
    }

    @Transactional
    public EmpresaResponse crearEmpresa(EmpresaRequest request, String tokenPresentado) {
        validarTokenInicial(tokenPresentado);
        if (empresaRepository.count() > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una empresa registrada en esta instalación"
            );
        }

        Empresa empresa = new Empresa();
        empresa.setInstalacionId(1);
        aplicarDatos(empresa, request);
        empresa.setEstado(true);
        return EmpresaResponse.from(empresaRepository.saveAndFlush(empresa));
    }

    @Transactional
    public EmpresaResponse actualizarEmpresa(Long idEmpresa, EmpresaRequest request) {
        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Empresa no encontrada"
                ));

        List<String> camposModificados = camposModificados(empresa, request);
        aplicarDatos(empresa, request);
        Empresa actualizada = empresaRepository.saveAndFlush(empresa);

        if (!camposModificados.isEmpty()) {
            Usuario administrador = usuarioAuthService.getUsuarioLogueado();
            auditoriaService.registrar(
                    administrador,
                    "EMPRESA",
                    "ACTUALIZAR",
                    "Se actualizaron los campos: " + String.join(", ", camposModificados)
            );
        }

        return EmpresaResponse.from(actualizada);
    }

    @Transactional
    public EmpresaResponse actualizarLogo(MultipartFile file) throws IOException {
        Empresa empresa = obtenerEmpresaExistente();
        String logoAnterior = empresa.getLogoPublicId();
        EmpresaLogoStorage.LogoAsset nuevoLogo = logoStorage.upload(file);
        empresa.setLogo(nuevoLogo.url());
        empresa.setLogoPublicId(nuevoLogo.publicId());

        try {
            empresaRepository.saveAndFlush(empresa);
        } catch (DataAccessException exception) {
            eliminarLogoSinEnmascararError(nuevoLogo.publicId());
            throw exception;
        }

        if (logoAnterior != null && !logoAnterior.equals(nuevoLogo.publicId())) {
            eliminarLogoDespuesDeConfirmar(logoAnterior);
        }

        Usuario administrador = usuarioAuthService.getUsuarioLogueado();
        auditoriaService.registrar(
                administrador,
                "EMPRESA",
                "ACTUALIZAR LOGO",
                "Se actualizó el logo de la empresa."
        );
        return EmpresaResponse.from(empresa);
    }

    private Empresa obtenerEmpresaExistente() {
        return empresaRepository.findFirstByOrderByIdEmpresaAsc()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe una empresa registrada"
                ));
    }

    private void validarTokenInicial(String tokenPresentado) {
        if (bootstrapToken == null || bootstrapToken.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "El registro inicial no está configurado; define EMPRESA_BOOTSTRAP_TOKEN con al menos 32 bytes"
            );
        }

        byte[] esperado = bootstrapToken.getBytes(StandardCharsets.UTF_8);
        byte[] recibido = tokenPresentado == null
                ? new byte[0]
                : tokenPresentado.getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(esperado, recibido)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de configuración inicial inválido");
        }
    }

    private void aplicarDatos(Empresa empresa, EmpresaRequest request) {
        empresa.setNombreComercial(request.nombreComercial().trim());
        empresa.setRazonSocial(request.razonSocial().trim());
        empresa.setNit(request.nit().trim());
        empresa.setDigitoVerificacion(request.digitoVerificacion().trim());
        empresa.setDireccion(request.direccion().trim());
        empresa.setMunicipio(request.municipio().trim());
        empresa.setDepartamento(request.departamento().trim());
        empresa.setTelefono(request.telefono().trim());
        empresa.setCorreo(normalizarOpcional(request.correo()));
    }

    private List<String> camposModificados(Empresa empresa, EmpresaRequest request) {
        List<String> campos = new ArrayList<>();
        agregarSiCambio(campos, "nombreComercial", empresa.getNombreComercial(), request.nombreComercial().trim());
        agregarSiCambio(campos, "razonSocial", empresa.getRazonSocial(), request.razonSocial().trim());
        agregarSiCambio(campos, "nit", empresa.getNit(), request.nit().trim());
        agregarSiCambio(
                campos,
                "digitoVerificacion",
                empresa.getDigitoVerificacion(),
                request.digitoVerificacion().trim()
        );
        agregarSiCambio(campos, "direccion", empresa.getDireccion(), request.direccion().trim());
        agregarSiCambio(campos, "municipio", empresa.getMunicipio(), request.municipio().trim());
        agregarSiCambio(campos, "departamento", empresa.getDepartamento(), request.departamento().trim());
        agregarSiCambio(campos, "telefono", empresa.getTelefono(), request.telefono().trim());
        agregarSiCambio(campos, "correo", empresa.getCorreo(), normalizarOpcional(request.correo()));
        return campos;
    }

    private void agregarSiCambio(List<String> campos, String nombreCampo, Object anterior, Object actual) {
        if (!Objects.equals(anterior, actual)) {
            campos.add(nombreCampo);
        }
    }

    private String normalizarOpcional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void eliminarLogoDespuesDeConfirmar(String publicId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    logoStorage.delete(publicId);
                } catch (IOException exception) {
                    LOGGER.error("No se pudo eliminar el logo anterior de Cloudinary: {}", publicId, exception);
                }
            }
        });
    }

    private void eliminarLogoSinEnmascararError(String publicId) {
        try {
            logoStorage.delete(publicId);
        } catch (IOException cleanupException) {
            LOGGER.error("No se pudo limpiar el logo nuevo tras fallar el guardado: {}", publicId, cleanupException);
        }
    }
}
