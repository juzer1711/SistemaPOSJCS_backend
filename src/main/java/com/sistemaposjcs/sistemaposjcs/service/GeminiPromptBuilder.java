package com.sistemaposjcs.sistemaposjcs.service;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoConsulta;

@Service
public class GeminiPromptBuilder {

    public String construirPrompt(String pregunta) {
        return """
                Eres un clasificador de intenciones para un sistema POS.

                Tu unica tarea es leer la pregunta del usuario y responder exactamente con uno de los valores permitidos del enum TipoConsulta.

                Reglas obligatorias:
                - Responde solo texto plano.
                - No respondas JSON.
                - No expliques tu respuesta.
                - No generes SQL.
                - No inventes consultas.
                - No intentes acceder a bases de datos.
                - Si la pregunta no corresponde claramente a una intencion permitida, responde DESCONOCIDA.
                - La respuesta debe coincidir exactamente con uno de los valores permitidos.

                Valores permitidos:
                %s

                Ejemplos:
                Pregunta: Cuanto vendimos hoy?
                Respuesta: VENTAS_HOY

                Pregunta: Cuantas ventas van en el dia?
                Respuesta: CANTIDAD_VENTAS_HOY

                Pregunta: Muestrame las ultimas ventas
                Respuesta: ULTIMAS_VENTAS

                Pregunta: Cual fue la venta mas alta de este mes?
                Respuesta: VENTA_MAS_ALTA_MES

                Pregunta: Cual es el producto mas vendido?
                Respuesta: PRODUCTO_MAS_VENDIDO

                Pregunta: Que productos tienen stock bajo?
                Respuesta: PRODUCTOS_STOCK_BAJO

                Pregunta: Cuanto stock tiene el producto arroz?
                Respuesta: STOCK_PRODUCTO

                Pregunta: Cuantos clientes hay registrados?
                Respuesta: TOTAL_CLIENTES

                Pregunta: Que cajas estan abiertas?
                Respuesta: LISTAR_CAJAS_ABIERTAS

                Pregunta: Que cajero vendio mas hoy?
                Respuesta: CAJERO_MAS_VENTAS_HOY

                Pregunta: Cuanto dinero ingreso por efectivo?
                Respuesta: INGRESOS_EFECTIVO

                Pregunta: Cuanto ingreso por transferencia?
                Respuesta: INGRESOS_TRANSFERENCIA

                Pregunta: Cual es el metodo de pago mas usado?
                Respuesta: METODO_PAGO_MAS_USADO

                Pregunta: Cual es el NIT de la empresa?
                Respuesta: NIT_EMPRESA

                Pregunta: Cuentame un chiste
                Respuesta: DESCONOCIDA

                Pregunta del usuario:
                %s

                Respuesta:
                """.formatted(
                obtenerValoresPermitidos(),
                pregunta == null ? "" : pregunta
        );
    }

    private String obtenerValoresPermitidos() {
        return Arrays.stream(TipoConsulta.values())
                .map(Enum::name)
                .collect(Collectors.joining("\n"));
    }
}
