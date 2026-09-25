package dbp.projectbackend.ai;

import dbp.projectbackend.dtos.reportes.*;
import dbp.projectbackend.exceptions.InvalidOperationException;
import dbp.projectbackend.services.ReporteService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class HerramientasAdmin {

    private static final int MAX_FILAS = 20;

    private final ReporteService reporteService;
    private final Long empresaId;

    public HerramientasAdmin(ReporteService reporteService, Long empresaId) {
        this.reporteService = reporteService;
        this.empresaId = empresaId;
    }

    @Tool(description = "Resumen de las ventas de un periodo: numero de ventas, unidades, total vendido en soles, "
            + "costo de la mercaderia vendida, ganancia, margen en porcentaje, descuentos dados por rebajas y ticket promedio.")
    public ResumenVentasDTO resumenVentas(
            @ToolParam(description = "Fecha inicial, formato AAAA-MM-DD") String desde,
            @ToolParam(description = "Fecha final (inclusive), formato AAAA-MM-DD") String hasta) {
        return reporteService.resumen(empresaId, fecha(desde, "desde"), fecha(hasta, "hasta"));
    }

    @Tool(description = "Ranking de productos (modelos de prenda) de un periodo, por unidades vendidas o por ganancia.")
    public List<ProductoRankingDTO> topProductos(
            @ToolParam(description = "Fecha inicial, formato AAAA-MM-DD") String desde,
            @ToolParam(description = "Fecha final (inclusive), formato AAAA-MM-DD") String hasta,
            @ToolParam(description = "UNIDADES = lo que mas se vende; GANANCIA = lo que mas ganancia deja") CriterioRanking criterio,
            @ToolParam(description = "Cuantos productos devolver, de 1 a 20. Por defecto 5", required = false) Integer limite) {
        int n = limite == null ? 5 : Math.min(Math.max(limite, 1), MAX_FILAS);
        return reporteService.topProductos(empresaId, fecha(desde, "desde"), fecha(hasta, "hasta"), criterio, n);
    }

    @Tool(description = "Ventas de un periodo agrupadas por medio de pago (EFECTIVO, YAPE, PLIN...), canal de venta "
            + "(TIENDA, WHATSAPP, INSTAGRAM, TIKTOK...), categoria de producto, dia de la semana u hora del dia. "
            + "Incluye numero de ventas, unidades, total en soles y porcentaje del total.")
    public List<VentasAgrupadasDTO> ventasAgrupadas(
            @ToolParam(description = "Fecha inicial, formato AAAA-MM-DD") String desde,
            @ToolParam(description = "Fecha final (inclusive), formato AAAA-MM-DD") String hasta,
            @ToolParam(description = "Como agrupar las ventas") AgrupacionVentas por) {
        return reporteService.ventasAgrupadas(empresaId, fecha(desde, "desde"), fecha(hasta, "hasta"), por);
    }

    @Tool(description = "Tallas y colores con stock que no se venden hace al menos N dias (mercaderia parada), "
            + "ordenados por capital inmovilizado (stock por costo). Sirve para decidir que rematar o dejar de comprar.")
    public List<ProductoParadoDTO> stockParado(
            @ToolParam(description = "Minimo de dias sin vender, por ejemplo 30 o 60. Por defecto 60", required = false) Integer dias) {
        return reporteService.stockParado(empresaId, dias == null ? 60 : dias).stream().limit(MAX_FILAS).toList();
    }

    private LocalDate fecha(String valor, String nombre) {
        try {
            return LocalDate.parse(valor.strip());
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new InvalidOperationException("Fecha '" + nombre + "' invalida (" + valor + "): usa el formato AAAA-MM-DD");
        }
    }
}
