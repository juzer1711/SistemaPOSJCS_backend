package com.sistemaposjcs.sistemaposjcs.modules.empresa.dto;

import com.sistemaposjcs.sistemaposjcs.modules.empresa.model.Empresa;

public record EmpresaResponse(
        Long idEmpresa,
        String nombreComercial,
        String razonSocial,
        String nit,
        String digitoVerificacion,
        String direccion,
        String municipio,
        String departamento,
        String telefono,
        String correo,
        String logo,
        Boolean estado
) {
    public static EmpresaResponse from(Empresa empresa) {
        return new EmpresaResponse(
                empresa.getIdEmpresa(),
                empresa.getNombreComercial(),
                empresa.getRazonSocial(),
                empresa.getNit(),
                empresa.getDigitoVerificacion(),
                empresa.getDireccion(),
                empresa.getMunicipio(),
                empresa.getDepartamento(),
                empresa.getTelefono(),
                empresa.getCorreo(),
                empresa.getLogo(),
                empresa.getEstado()
        );
    }
}
