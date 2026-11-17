package dbp.projectbackend.services;

import dbp.projectbackend.dtos.request.ChartSpecDTO;
import dbp.projectbackend.dtos.response.ChartPointDTO;
import dbp.projectbackend.dtos.response.ChartResponseDTO;
import dbp.projectbackend.dtos.response.report.GroupedSalesDTO;
import dbp.projectbackend.dtos.response.report.ProductRankingDTO;
import dbp.projectbackend.enums.ChartMetric;
import dbp.projectbackend.enums.ChartSource;
import dbp.projectbackend.enums.ChartType;
import dbp.projectbackend.enums.RankingCriterion;
import dbp.projectbackend.enums.SalesGrouping;
import dbp.projectbackend.exceptions.InvalidOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;

/**
 * Convierte una receta de grafico en datos reales del negocio. Solo usa los reportes de lectura de
 * ReportService, siempre filtrados por la empresa del usuario: la IA nunca toca la base de datos.
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ChartService {

    private static final int DEFAULT_LIMIT = 8;
    private static final int MAX_LIMIT = 20;
    private static final int STALE_DAYS = 60;
    private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("dd/MM");

    private final ReportService reporteService;

    public ChartResponseDTO build(Long empresaId, ChartSpecDTO pedido) {
        if (pedido == null || pedido.fuente() == null) {
            throw new InvalidOperationException("No se pudo determinar que datos graficar. Reformula tu pedido.");
        }

        LocalDate hasta = parseDate(pedido.hasta(), LocalDate.now());
        LocalDate desde = parseDate(pedido.desde(), hasta.minusDays(29));
        int limite = pedido.limite() == null ? DEFAULT_LIMIT : Math.min(Math.max(pedido.limite(), 1), MAX_LIMIT);
        ChartSource fuente = pedido.fuente();
        ChartType tipo = pedido.tipo() == null ? ChartType.BARRAS : pedido.tipo();
        ChartMetric metrica = pedido.metrica() == null ? ChartMetric.TOTAL : pedido.metrica();
        SalesGrouping agrupacion = pedido.agrupacion();

        List<ChartPointDTO> puntos;
        switch (fuente) {
            case VENTAS_POR_DIA -> {
                if (metrica == ChartMetric.GANANCIA) {
                    metrica = ChartMetric.TOTAL;
                }
                if (tipo == ChartType.TORTA) {
                    tipo = ChartType.LINEA;
                }
                agrupacion = null;
                ChartMetric m = metrica;
                puntos = reporteService.salesByDay(empresaId, desde, hasta).stream()
                        .map(g -> new ChartPointDTO(LocalDate.parse(g.grupo()).format(DAY_LABEL), groupValue(g, m)))
                        .toList();
            }
            case VENTAS_AGRUPADAS -> {
                if (metrica == ChartMetric.GANANCIA) {
                    metrica = ChartMetric.TOTAL;
                }
                if (agrupacion == null) {
                    agrupacion = SalesGrouping.MEDIO_PAGO;
                }
                ChartMetric m = metrica;
                puntos = reporteService.groupedSales(empresaId, desde, hasta, agrupacion).stream()
                        .map(g -> new ChartPointDTO(g.grupo(), groupValue(g, m)))
                        .toList();
            }
            case TOP_PRODUCTOS -> {
                if (metrica == ChartMetric.VENTAS) {
                    metrica = ChartMetric.UNIDADES;
                }
                agrupacion = null;
                ChartMetric m = metrica;
                RankingCriterion criterio = metrica == ChartMetric.GANANCIA ? RankingCriterion.GANANCIA : RankingCriterion.UNIDADES;
                puntos = reporteService.topProducts(empresaId, desde, hasta, criterio, MAX_LIMIT).stream()
                        .map(p -> new ChartPointDTO(p.producto(), rankingValue(p, m)))
                        .sorted(Comparator.comparing(ChartPointDTO::valor).reversed())
                        .limit(limite)
                        .toList();
            }
            case STOCK_PARADO -> {
                metrica = ChartMetric.TOTAL;
                agrupacion = null;
                puntos = reporteService.staleStock(empresaId, STALE_DAYS).stream()
                        .limit(limite)
                        .map(p -> new ChartPointDTO(p.producto() + " " + p.talla() + " " + p.color(), p.capitalInmovilizado()))
                        .toList();
            }
            case REPOSICION -> {
                metrica = ChartMetric.UNIDADES;
                agrupacion = null;
                puntos = reporteService.restockSuggestions(empresaId, 30, 21).stream()
                        .limit(limite)
                        .map(s -> new ChartPointDTO(s.producto() + " " + s.talla() + " " + s.color(),
                                BigDecimal.valueOf(s.cantidadSugerida())))
                        .toList();
            }
            default -> throw new InvalidOperationException("Fuente de datos no soportada");
        }

        String unidad = switch (metrica) {
            case TOTAL, GANANCIA -> "SOLES";
            case UNIDADES -> "UNIDADES";
            case VENTAS -> "VENTAS";
        };
        String titulo = pedido.titulo() == null || pedido.titulo().isBlank() ? defaultTitle(fuente) : pedido.titulo().strip();
        if (titulo.length() > 80) {
            titulo = titulo.substring(0, 80);
        }

        ChartSpecDTO spec = new ChartSpecDTO(titulo, tipo, fuente, desde.toString(), hasta.toString(),
                agrupacion, metrica, limite);
        return new ChartResponseDTO(spec, unidad, puntos, summarize(puntos, unidad, fuente), null);
    }

    private BigDecimal groupValue(GroupedSalesDTO grupo, ChartMetric metrica) {
        return switch (metrica) {
            case UNIDADES -> BigDecimal.valueOf(grupo.unidades());
            case VENTAS -> BigDecimal.valueOf(grupo.numeroVentas());
            default -> grupo.total();
        };
    }

    private BigDecimal rankingValue(ProductRankingDTO producto, ChartMetric metrica) {
        return switch (metrica) {
            case GANANCIA -> producto.ganancia();
            case TOTAL -> producto.totalVendido();
            default -> BigDecimal.valueOf(producto.unidades());
        };
    }

    /** Frase corta con las cifras clave. Se calcula aqui, no la redacta la IA, para que nunca invente numeros. */
    private String summarize(List<ChartPointDTO> puntos, String unidad, ChartSource fuente) {
        BigDecimal total = puntos.stream().map(ChartPointDTO::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (puntos.isEmpty() || total.signum() == 0) {
            return "No hay datos para este grafico en el periodo elegido.";
        }
        ChartPointDTO mayor = puntos.stream().max(Comparator.comparing(ChartPointDTO::valor)).orElseThrow();
        BigDecimal porcentaje = mayor.valor().multiply(BigDecimal.valueOf(100)).divide(total, 0, RoundingMode.HALF_UP);
        String destacado = fuente == ChartSource.VENTAS_POR_DIA ? "Mejor dia" : "Mayor";
        return "Total: %s. %s: %s con %s (%s%% del total).".formatted(
                format(total, unidad), destacado, mayor.etiqueta(), format(mayor.valor(), unidad), porcentaje.toPlainString());
    }

    private String format(BigDecimal valor, String unidad) {
        if ("SOLES".equals(unidad)) {
            return "S/ " + valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
        }
        return valor.setScale(0, RoundingMode.HALF_UP).toPlainString() + ("VENTAS".equals(unidad) ? " ventas" : " unidades");
    }

    private String defaultTitle(ChartSource fuente) {
        return switch (fuente) {
            case VENTAS_POR_DIA -> "Ventas por dia";
            case VENTAS_AGRUPADAS -> "Ventas agrupadas";
            case TOP_PRODUCTOS -> "Productos mas vendidos";
            case STOCK_PARADO -> "Capital en stock parado";
            case REPOSICION -> "Unidades por reponer";
        };
    }

    private LocalDate parseDate(String valor, LocalDate porDefecto) {
        if (valor == null || valor.isBlank()) {
            return porDefecto;
        }
        try {
            return LocalDate.parse(valor.strip());
        } catch (DateTimeParseException ex) {
            throw new InvalidOperationException("Fecha invalida: usa el formato AAAA-MM-DD");
        }
    }
}
