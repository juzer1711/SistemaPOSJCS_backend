package com.sistemaposjcs.sistemaposjcs.service;

import java.text.Normalizer;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.sistemaposjcs.sistemaposjcs.model.Enum.TipoConsulta;

@Service
public class RuleBasedIntentClassifierService {

    public TipoConsulta clasificar(String pregunta) {

        String texto = normalizar(pregunta);

        if (esConsultaProductosStockBajo(texto)) {
            return TipoConsulta.PRODUCTOS_STOCK_BAJO;
        }

        if (esConsultaProductosAgotados(texto)) {
            return TipoConsulta.PRODUCTOS_AGOTADOS;
        }

        if (esConsultaMovimientosInventarioHoy(texto)) {
            return TipoConsulta.MOVIMIENTOS_INVENTARIO_HOY;
        }

        if (esConsultaProductoMayorStock(texto)) {
            return TipoConsulta.PRODUCTO_MAYOR_STOCK;
        }

        if (esConsultaStockProducto(texto)) {
            return TipoConsulta.STOCK_PRODUCTO;
        }

        if (esConsultaTotalCajasAbiertas(texto)) {
            return TipoConsulta.TOTAL_CAJAS_ABIERTAS;
        }

        if (esConsultaListarCajasAbiertas(texto)) {
            return TipoConsulta.LISTAR_CAJAS_ABIERTAS;
        }

        if (esConsultaUsuarioCajaPrincipal(texto)) {
            return TipoConsulta.USUARIO_CAJA_PRINCIPAL;
        }

        if (esConsultaUltimoCierreCaja(texto)) {
            return TipoConsulta.ULTIMO_CIERRE_CAJA;
        }

        if (esConsultaDineroActualCaja(texto)) {
            return TipoConsulta.DINERO_ACTUAL_CAJA;
        }

        if (esConsultaCajeroMasVentasMes(texto)) {
            return TipoConsulta.CAJERO_MAS_VENTAS_MES;
        }

        if (esConsultaCajeroMasVentasHoy(texto)) {
            return TipoConsulta.CAJERO_MAS_VENTAS_HOY;
        }

        if (esConsultaRankingCajeros(texto)) {
            return TipoConsulta.RANKING_CAJEROS;
        }

        if (esConsultaIngresosEfectivo(texto)) {
            return TipoConsulta.INGRESOS_EFECTIVO;
        }

        if (esConsultaIngresosTransferencia(texto)) {
            return TipoConsulta.INGRESOS_TRANSFERENCIA;
        }

        if (esConsultaMetodoPagoMasUsado(texto)) {
            return TipoConsulta.METODO_PAGO_MAS_USADO;
        }

        if (esConsultaNombreEmpresa(texto)) {
            return TipoConsulta.NOMBRE_EMPRESA;
        }

        if (esConsultaNitEmpresa(texto)) {
            return TipoConsulta.NIT_EMPRESA;
        }

        if (esConsultaTelefonoEmpresa(texto)) {
            return TipoConsulta.TELEFONO_EMPRESA;
        }

        if (esConsultaVentasPorCajero(texto)) {
            return TipoConsulta.VENTAS_POR_CAJERO;
        }

        if (esConsultaUltimosClientes(texto)) {
            return TipoConsulta.ULTIMOS_CLIENTES;
        }

        if (esConsultaClientesActivos(texto)) {
            return TipoConsulta.CLIENTES_ACTIVOS;
        }

        if (esConsultaTotalClientes(texto)) {
            return TipoConsulta.TOTAL_CLIENTES;
        }

        if (esConsultaTopProductosMasVendidos(texto)) {
            return TipoConsulta.TOP_PRODUCTOS_MAS_VENDIDOS;
        }

        if (esConsultaProductoMenosVendido(texto)) {
            return TipoConsulta.PRODUCTO_MENOS_VENDIDO;
        }

        if (esConsultaProductoMasVendido(texto)) {
            return TipoConsulta.PRODUCTO_MAS_VENDIDO;
        }

        if (esConsultaProductosInactivos(texto)) {
            return TipoConsulta.PRODUCTOS_INACTIVOS;
        }

        if (esConsultaProductosActivos(texto)) {
            return TipoConsulta.PRODUCTOS_ACTIVOS;
        }

        if (esConsultaTotalProductos(texto)) {
            return TipoConsulta.TOTAL_PRODUCTOS;
        }

        if (esConsultaUltimasVentas(texto)) {
            return TipoConsulta.ULTIMAS_VENTAS;
        }

        if (esConsultaVentaMasAlta(texto)
                && contieneAlguna(texto, "mes", "mensual")) {
            return TipoConsulta.VENTA_MAS_ALTA_MES;
        }

        if (esConsultaVentaMasAlta(texto)
                && contieneAlguna(texto, "hoy", "dia", "diaria")) {
            return TipoConsulta.VENTA_MAS_ALTA_DIA;
        }

        if (esConsultaCantidadVentasHoy(texto)) {
            return TipoConsulta.CANTIDAD_VENTAS_HOY;
        }

        if (esConsultaVentasSemana(texto)) {
            return TipoConsulta.VENTAS_SEMANA;
        }

        if (esConsultaVentasMes(texto)) {
            return TipoConsulta.VENTAS_MES;
        }

        if (esConsultaVentasHoy(texto)) {
            return TipoConsulta.VENTAS_HOY;
        }

        return TipoConsulta.DESCONOCIDA;
    }

    private boolean esConsultaUltimasVentas(String texto) {
        return esTemaVentas(texto)
                && contieneAlguna(texto, "ultimas", "ultimos", "recientes", "reciente",
                        "nuevas", "historial", "registradas", "ver ventas", "mostrar ventas");
    }

    private boolean esConsultaVentaMasAlta(String texto) {
        return esTemaVentas(texto)
                && (
                        texto.contains("mas alta")
                        || texto.contains("mas grande")
                        || texto.contains("mas costosa")
                        || texto.contains("mayor valor")
                        || texto.contains("mayor monto")
                        || texto.contains("mayor venta")
                        || texto.contains("venta mayor")
                        || texto.contains("mejor venta")
                        || texto.contains("venta mas cara")
                        || texto.contains("factura mas alta")
                        || texto.contains("ticket mas alto")
                );
    }

    private boolean esConsultaCantidadVentasHoy(String texto) {
        return esTemaVentas(texto)
                && esPeriodoHoy(texto)
                && contieneAlguna(texto, "cantidad", "cuantas", "cuantos", "numero",
                        "conteo", "total de ventas", "ventas realizadas", "ventas registradas");
    }

    private boolean esConsultaVentasSemana(String texto) {
        return esTemaVentas(texto)
                && contieneAlguna(texto, "semana", "semanal", "esta semana", "semana actual");
    }

    private boolean esConsultaVentasMes(String texto) {
        return esTemaVentas(texto)
                && contieneAlguna(texto, "mes", "mensual", "este mes", "mes actual");
    }

    private boolean esConsultaVentasHoy(String texto) {
        return esTemaVentas(texto)
                && esPeriodoHoy(texto);
    }

    private boolean esConsultaTopProductosMasVendidos(String texto) {
        return esTemaProductos(texto)
                && contieneAlguna(texto, "top", "ranking", "lista", "listado", "mas vendidos",
                        "mayores ventas", "principales", "favoritos", "mas comprados");
    }

    private boolean esConsultaProductoMenosVendido(String texto) {
        return esTemaProductos(texto)
                && (
                        texto.contains("menos vendido")
                        || texto.contains("menos vendidos")
                        || texto.contains("menor venta")
                        || texto.contains("vende menos")
                        || texto.contains("se vende menos")
                        || texto.contains("peor vendido")
                        || texto.contains("menor rotacion")
                );
    }

    private boolean esConsultaProductoMasVendido(String texto) {
        return esTemaProductos(texto)
                && (
                        texto.contains("mas vendido")
                        || texto.contains("mas vendidos")
                        || texto.contains("mayor venta")
                        || texto.contains("vende mas")
                        || texto.contains("se vende mas")
                        || texto.contains("mas comprado")
                        || texto.contains("producto estrella")
                );
    }

    private boolean esConsultaTotalProductos(String texto) {
        return esTemaProductos(texto)
                && contieneAlguna(texto, "total", "cuantos", "cuantas", "cantidad",
                        "numero", "conteo", "registrados", "hay");
    }

    private boolean esConsultaProductosActivos(String texto) {
        return esTemaProductos(texto)
                && contieneAlguna(texto, "activo", "activos", "habilitado", "habilitados",
                        "disponible", "disponibles", "vigente", "vigentes");
    }

    private boolean esConsultaProductosInactivos(String texto) {
        return esTemaProductos(texto)
                && contieneAlguna(texto, "inactivo", "inactivos", "desactivado", "desactivados",
                        "deshabilitado", "deshabilitados", "no disponibles");
    }

    private boolean esConsultaProductosStockBajo(String texto) {
        return contieneAlguna(texto, "producto", "productos", "articulo", "articulos",
                "inventario", "stock", "existencias")
                && (
                        texto.contains("stock bajo")
                        || texto.contains("bajo stock")
                        || texto.contains("poco stock")
                        || texto.contains("alerta de stock")
                        || texto.contains("alertas de stock")
                        || texto.contains("pocas unidades")
                        || texto.contains("pocas existencias")
                        || texto.contains("inventario bajo")
                        || texto.contains("por agotarse")
                );
    }

    private boolean esConsultaProductosAgotados(String texto) {
        return contieneAlguna(texto, "producto", "productos", "articulo", "articulos",
                "inventario", "stock", "existencias")
                && contieneAlguna(texto, "agotado", "agotados", "sin stock", "stock cero",
                        "sin existencias", "sin unidades", "en cero");
    }

    private boolean esConsultaMovimientosInventarioHoy(String texto) {
        return contieneAlguna(texto, "movimiento", "movimientos", "entradas", "salidas",
                "ajustes", "cambios")
                && contieneAlguna(texto, "inventario", "stock", "existencias")
                && esPeriodoHoy(texto);
    }

    private boolean esConsultaProductoMayorStock(String texto) {
        return esTemaProductos(texto)
                && (
                        texto.contains("mayor stock")
                        || texto.contains("mas stock")
                        || texto.contains("stock mas alto")
                        || texto.contains("mayor inventario")
                        || texto.contains("mas unidades")
                        || texto.contains("mayores existencias")
                        || texto.contains("mas existencias")
                );
    }

    private boolean esConsultaStockProducto(String texto) {
        return contieneAlguna(texto, "stock", "inventario", "existencias", "unidades",
                "disponible", "disponibles", "hay")
                && (esTemaProductos(texto)
                || contieneAlguna(texto, "de ", "del ", "para ", "tiene "));
    }

    private boolean esConsultaTotalClientes(String texto) {
        return esTemaClientes(texto)
                && contieneAlguna(texto, "total", "cuantos", "cuantas", "cantidad",
                        "numero", "conteo", "registrados", "hay");
    }

    private boolean esConsultaClientesActivos(String texto) {
        return esTemaClientes(texto)
                && contieneAlguna(texto, "activo", "activos", "habilitado", "habilitados",
                        "vigente", "vigentes");
    }

    private boolean esConsultaUltimosClientes(String texto) {
        return esTemaClientes(texto)
                && contieneAlguna(texto, "ultimos", "ultimas", "recientes", "reciente",
                        "nuevos", "nuevas", "registrados", "creados");
    }

    private boolean esConsultaTotalCajasAbiertas(String texto) {
        return esTemaCajas(texto)
                && contieneAlguna(texto, "abierta", "abiertas")
                && contieneAlguna(texto, "total", "cuantas", "cuantos", "cantidad",
                        "numero", "conteo", "hay");
    }

    private boolean esConsultaListarCajasAbiertas(String texto) {
        return esTemaCajas(texto)
                && contieneAlguna(texto, "abierta", "abiertas")
                && contieneAlguna(texto, "listar", "lista", "muestra", "mostrar",
                        "cuales", "ver", "detalle", "detalles");
    }

    private boolean esConsultaUsuarioCajaPrincipal(String texto) {
        return esTemaCajas(texto)
                && contieneAlguna(texto, "principal", "mayor", "mas ventas", "mejor")
                && contieneAlguna(texto, "usuario", "cajero", "responsable", "quien", "atiende");
    }

    private boolean esConsultaUltimoCierreCaja(String texto) {
        return esTemaCajas(texto)
                && contieneAlguna(texto, "cierre", "cerrada", "cerradas", "cerrar", "cerro")
                && contieneAlguna(texto, "ultimo", "ultima", "reciente", "recientes",
                        "mas reciente");
    }

    private boolean esConsultaDineroActualCaja(String texto) {
        return esTemaCajas(texto)
                && contieneAlguna(texto, "dinero", "monto", "actual", "saldo", "total",
                        "efectivo", "recaudo")
                && contieneAlguna(texto, "actual", "abierta", "abiertas", "hay",
                        "tiene", "disponible");
    }

    private boolean esConsultaCajeroMasVentasMes(String texto) {
        return esTemaCajeros(texto)
                && esTemaVentas(texto)
                && contieneAlguna(texto, "mas", "mayor", "mejor", "lider")
                && contieneAlguna(texto, "mes", "mensual");
    }

    private boolean esConsultaCajeroMasVentasHoy(String texto) {
        return esTemaCajeros(texto)
                && esTemaVentas(texto)
                && contieneAlguna(texto, "mas", "mayor", "mejor", "lider")
                && esPeriodoHoy(texto);
    }

    private boolean esConsultaVentasPorCajero(String texto) {
        return esTemaCajeros(texto)
                && esTemaVentas(texto)
                && contieneAlguna(texto, "por", "cada", "todos", "total", "de cada",
                        "segun", "por usuario", "por cajero");
    }

    private boolean esConsultaRankingCajeros(String texto) {
        return esTemaCajeros(texto)
                && contieneAlguna(texto, "ranking", "top", "lista", "mejores",
                        "posiciones", "orden", "clasificacion");
    }

    private boolean esConsultaIngresosEfectivo(String texto) {
        return esConsultaIngresosPorMetodoPago(texto)
                && contieneAlguna(texto, "efectivo", "cash");
    }

    private boolean esConsultaIngresosTransferencia(String texto) {
        return esConsultaIngresosPorMetodoPago(texto)
                && contieneAlguna(texto, "transferencia", "transferencias", "transferido");
    }

    private boolean esConsultaIngresosPorMetodoPago(String texto) {
        return contieneAlguna(texto, "ingreso", "ingresos", "ingreso por", "ingreso en",
                "cuanto ingreso", "cuanto entro", "dinero recibido", "monto recibido",
                "recaudo", "recaudado", "recibido", "venta", "ventas", "vendido",
                "pagada", "pagadas", "pagado", "pagados", "total");
    }

    private boolean esConsultaMetodoPagoMasUsado(String texto) {
        return contieneAlguna(texto, "metodo", "pago", "pagos", "forma de pago", "medio de pago")
                && (
                        texto.contains("mas usado")
                        || texto.contains("mas utilizado")
                        || texto.contains("mas frecuente")
                        || texto.contains("mas comun")
                        || texto.contains("principal")
                        || texto.contains("popular")
                        || texto.contains("prefieren")
                        || texto.contains("usan mas")
                        || texto.contains("utilizan mas")
                );
    }

    private boolean esConsultaNombreEmpresa(String texto) {
        return esTemaEmpresa(texto)
                && contieneAlguna(texto, "nombre", "llama", "razon social",
                        "nombre comercial", "como se llama");
    }

    private boolean esConsultaNitEmpresa(String texto) {
        return contieneAlguna(texto, "empresa", "negocio", "compania", "tienda", "nit")
                && contieneAlguna(texto, "nit");
    }

    private boolean esConsultaTelefonoEmpresa(String texto) {
        return contieneAlguna(texto, "empresa", "negocio", "compania", "tienda",
                "telefono", "celular", "contacto")
                && contieneAlguna(texto, "telefono", "celular", "contacto", "numero");
    }

    private boolean esTemaVentas(String texto) {
        return contieneAlguna(texto, "venta", "ventas", "vendido", "vendidos",
                "vendida", "vendidas", "vendi", "vendimos", "facturado",
                "facturacion", "recaudo", "recaudado", "ingreso", "ingresos");
    }

    private boolean esTemaProductos(String texto) {
        return contieneAlguna(texto, "producto", "productos", "articulo",
                "articulos", "item", "items", "referencia", "referencias");
    }

    private boolean esTemaClientes(String texto) {
        return contieneAlguna(texto, "cliente", "clientes", "comprador",
                "compradores");
    }

    private boolean esTemaCajas(String texto) {
        return contieneAlguna(texto, "caja", "cajas");
    }

    private boolean esTemaCajeros(String texto) {
        return contieneAlguna(texto, "cajero", "cajeros", "usuario",
                "usuarios", "vendedor", "vendedores");
    }

    private boolean esTemaEmpresa(String texto) {
        return contieneAlguna(texto, "empresa", "negocio", "compania", "tienda");
    }

    private boolean esPeriodoHoy(String texto) {
        return contieneAlguna(texto, "hoy", "dia", "diaria", "diarias", "del dia",
                "este dia", "dia actual");
    }

    private String normalizar(String texto) {
        if (texto == null) {
            return "";
        }

        String sinAcentos = Normalizer
                .normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return sinAcentos.toLowerCase(Locale.ROOT).trim();
    }

    private boolean contieneAlguna(String texto, String... palabras) {
        for (String palabra : palabras) {
            if (texto.contains(palabra)) {
                return true;
            }
        }

        return false;
    }
}
