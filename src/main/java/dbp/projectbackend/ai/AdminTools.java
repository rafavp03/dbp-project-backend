package dbp.projectbackend.ai;

import dbp.projectbackend.dtos.response.report.*;
import dbp.projectbackend.enums.SalesGrouping;
import dbp.projectbackend.enums.RankingCriterion;
import dbp.projectbackend.exceptions.InvalidOperationException;
import dbp.projectbackend.services.ReportService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class AdminTools {

    private static final int MAX_ROWS = 20;

    private final ReportService reporteService;
    private final Long empresaId;

    public AdminTools(ReportService reporteService, Long empresaId) {
        this.reporteService = reporteService;
        this.empresaId = empresaId;
    }

    @Tool(description = "Resumen de las ventas de un periodo: numero de ventas, unidades, total vendido en soles, "
            + "costo de la mercaderia vendida, ganancia, margen en porcentaje, descuentos dados por rebajas y ticket promedio.")
    public SalesSummaryDTO salesSummary(
            @ToolParam(description = "Fecha inicial, formato AAAA-MM-DD") String desde,
            @ToolParam(description = "Fecha final (inclusive), formato AAAA-MM-DD") String hasta) {
        return reporteService.summary(empresaId, parseDate(desde, "desde"), parseDate(hasta, "hasta"));
    }

    @Tool(description = "Ranking de productos (modelos de prenda) de un periodo, por unidades vendidas o por ganancia.")
    public List<ProductRankingDTO> topProducts(
            @ToolParam(description = "Fecha inicial, formato AAAA-MM-DD") String desde,
            @ToolParam(description = "Fecha final (inclusive), formato AAAA-MM-DD") String hasta,
            @ToolParam(description = "UNIDADES = lo que mas se vende; GANANCIA = lo que mas ganancia deja") RankingCriterion criterio,
            @ToolParam(description = "Cuantos productos devolver, de 1 a 20. Por defecto 5", required = false) Integer limite) {
        int n = limite == null ? 5 : Math.min(Math.max(limite, 1), MAX_ROWS);
        return reporteService.topProducts(empresaId, parseDate(desde, "desde"), parseDate(hasta, "hasta"), criterio, n);
    }

    @Tool(description = "Ventas de un periodo agrupadas por medio de pago (EFECTIVO, YAPE, PLIN...), canal de venta "
            + "(TIENDA, WHATSAPP, INSTAGRAM, TIKTOK...), categoria de producto, dia de la semana u hora del dia. "
            + "Incluye numero de ventas, unidades, total en soles y porcentaje del total.")
    public List<GroupedSalesDTO> groupedSales(
            @ToolParam(description = "Fecha inicial, formato AAAA-MM-DD") String desde,
            @ToolParam(description = "Fecha final (inclusive), formato AAAA-MM-DD") String hasta,
            @ToolParam(description = "Como agrupar las ventas") SalesGrouping por) {
        return reporteService.groupedSales(empresaId, parseDate(desde, "desde"), parseDate(hasta, "hasta"), por);
    }

    @Tool(description = "Tallas y colores con stock que no se venden hace al menos N dias (mercaderia parada), "
            + "ordenados por capital inmovilizado (stock por costo). Sirve para decidir que rematar o dejar de comprar.")
    public List<StaleProductDTO> staleStock(
            @ToolParam(description = "Minimo de dias sin vender, por ejemplo 30 o 60. Por defecto 60", required = false) Integer dias) {
        return reporteService.staleStock(empresaId, dias == null ? 60 : dias).stream().limit(MAX_ROWS).toList();
    }

    private LocalDate parseDate(String valor, String nombre) {
        try {
            return LocalDate.parse(valor.strip());
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new InvalidOperationException("Fecha '" + nombre + "' invalida (" + valor + "): usa el formato AAAA-MM-DD");
        }
    }
}
