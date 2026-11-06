package com.sistemaposjcs.sistemaposjcs.modules.autorizacion.service;

import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.model.PermisoAccion;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.model.Regla;
import com.sistemaposjcs.sistemaposjcs.modules.reglas.repository.ReglaRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.dto.PermisoModuloResponse;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutorizacionService {

    private final ReglaRepository reglaRepository;

    public AutorizacionService(ReglaRepository reglaRepository) {
        this.reglaRepository = reglaRepository;
    }

    @Transactional(readOnly = true)
    public boolean tienePermiso(String username, String codigoModulo, PermisoAccion accion) {
        if (username == null || username.isBlank()) {
            return false;
        }

        return reglaRepository.findByUsuario_UsernameAndModulo_CodigoModulo(username, codigoModulo)
                .map(regla -> permisoConcedido(regla, accion))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<PermisoModuloResponse> permisosDeUsuario(String username) {
        return reglaRepository.permisosDeUsuario(username);
    }

    private boolean permisoConcedido(Regla regla, PermisoAccion accion) {
        return switch (accion) {
            case CONSULTA -> regla.isReglaConsulta();
            case INSERCION -> regla.isReglaInsercion();
            case ACTUALIZACION -> regla.isReglaActualizacion();
            case REPORTE -> regla.isReglaReporte();
            case DELETE -> regla.isReglaDelete();
        };
    }
}
