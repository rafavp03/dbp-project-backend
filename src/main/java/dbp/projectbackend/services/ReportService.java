package dbp.projectbackend.services;

import dbp.projectbackend.dtos.response.report.*;
import dbp.projectbackend.enums.SalesGrouping;
import dbp.projectbackend.enums.RankingCriterion;
import dbp.projectbackend.enums.OrderStatus;
import dbp.projectbackend.exceptions.InvalidOperationException;
import dbp.projectbackend.models.SalesOrderLineModel;
import dbp.projectbackend.models.ProductModel;
import dbp.projectbackend.models.ProductVariantModel;
import dbp.projectbackend.repositories.SalesOrderLineRepository;
import dbp.projectbackend.repositories.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ReportService {

    private static final int MAX_RANGE_DAYS = 366;
    private static final int MAX_LIMIT = 50;
    private static final Locale ES_PE = Locale.of("es", "PE");

    private final SalesOrderLineRepository detalleRepository;
    private final ProductVariantRepository varianteRepository;

    public SalesSummaryDTO summary(Long empresaId, LocalDate desde, LocalDate hasta) {
        List<SalesOrderLineModel> lineas = periodLines(empresaId, desde, hasta);

        long numeroVentas = countSales(lineas);
        BigDecimal total = sum(lineas, SalesOrderLineModel::getSubtotal);
        BigDecimal costo = sum(lineas, this::lineCost);
        BigDecimal ganancia = total.subtract(costo);

        return new SalesSummaryDTO(
                desde,
                hasta,
                numeroVentas,
                sumUnits(lineas),
                total,
                costo,
                ganancia,
                percentage(ganancia, total),
                sum(lineas, SalesOrderLineModel::getDiscount),
                numeroVentas == 0 ? BigDecimal.ZERO : total.divide(BigDecimal.valueOf(numeroVentas), 2, RoundingMode.HALF_UP)
        );
    }

    public List<ProductRankingDTO> topProducts(Long empresaId, LocalDate desde, LocalDate hasta,
                                                 RankingCriterion criterio, int limite) {
        validateLimit(limite);
        Map<Long, List<SalesOrderLineModel>> porProducto = periodLines(empresaId, desde, hasta).stream()
                .collect(Collectors.groupingBy(d -> d.getVariante().getProducto().getId()));

        Comparator<ProductRankingDTO> orden = criterio == RankingCriterion.GANANCIA
                ? Comparator.comparing(ProductRankingDTO::ganancia)
                : Comparator.comparingLong(ProductRankingDTO::unidades);

        return porProducto.values().stream()
                .map(this::toRanking)
                .sorted(orden.reversed())
                .limit(limite)
                .toList();
    }

    public List<GroupedSalesDTO> groupedSales(Long empresaId, LocalDate desde, LocalDate hasta,
                                                    SalesGrouping agrupacion) {
        List<SalesOrderLineModel> lineas = periodLines(empresaId, desde, hasta);
        BigDecimal totalGeneral = sum(lineas, SalesOrderLineModel::getSubtotal);

        return switch (agrupacion) {
            case MEDIO_PAGO -> sortByTotal(groupBy(lineas, d -> d.getOrdenVenta().getMedioPago().name()), totalGeneral);
            case CANAL -> sortByTotal(groupBy(lineas, d -> d.getOrdenVenta().getCanal().name()), totalGeneral);
            case CATEGORIA -> sortByTotal(groupBy(lineas, d -> categoryName(d.getVariante().getProducto())), totalGeneral);
            case DIA_SEMANA -> inOrder(groupBy(lineas, d -> d.getOrdenVenta().getFechaEmision().getDayOfWeek()),
                    this::dayName, totalGeneral);
            case HORA -> inOrder(this.<Integer>groupBy(lineas, d -> d.getOrdenVenta().getFechaEmision().getHour()),
                    (Integer h) -> "%02d:00-%02d:00".formatted(h, (h + 1) % 24), totalGeneral);
        };
    }

    public List<LowStockDTO> lowStock(Long empresaId) {
        return varianteRepository.findStockBajo(empresaId).stream()
                .filter(v -> Boolean.TRUE.equals(v.getProducto().getActivo()))
                .sorted(Comparator.comparingInt(ProductVariantModel::getStock))
                .map(v -> new LowStockDTO(v.getId(), v.getProducto().getNombre(), v.getTalla(), v.getColor(),
                        v.getStock(), v.getStockMinimo()))
                .toList();
    }

    public List<StaleProductDTO> staleStock(Long empresaId, int dias) {
        if (dias < 1) {
            throw new InvalidOperationException("Los dias deben ser mayores a 0");
        }

        Map<Long, LocalDateTime> ultimaVenta = new HashMap<>();
        for (Object[] fila : detalleRepository.findUltimaVentaPorVariante(empresaId, OrderStatus.COMPLETADA)) {
            ultimaVenta.put((Long) fila[0], (LocalDateTime) fila[1]);
        }

        LocalDateTime ahora = LocalDateTime.now();
        return varianteRepository.findByProductoEmpresaId(empresaId).stream()
                .filter(v -> Boolean.TRUE.equals(v.getActivo())
                        && Boolean.TRUE.equals(v.getProducto().getActivo())
                        && v.getStock() > 0)
                .map(v -> {
                    LocalDateTime ultima = ultimaVenta.get(v.getId());
                    LocalDateTime referencia = ultima != null ? ultima : v.getProducto().getFechaRegistro();
                    long diasSinVender = ChronoUnit.DAYS.between(referencia, ahora);
                    BigDecimal costo = Optional.ofNullable(v.getProducto().getPrecioCompra()).orElse(BigDecimal.ZERO);
                    return new StaleProductDTO(
                            v.getId(),
                            v.getProducto().getNombre(),
                            v.getTalla(),
                            v.getColor(),
                            v.getStock(),
                            ultima != null ? ultima.toLocalDate() : null,
                            diasSinVender,
                            costo.multiply(BigDecimal.valueOf(v.getStock()))
                    );
                })
                .filter(p -> p.diasSinVender() >= dias)
                .sorted(Comparator.comparing(StaleProductDTO::capitalInmovilizado).reversed())
                .toList();
    }

    private List<SalesOrderLineModel> periodLines(Long empresaId, LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new InvalidOperationException("Debes indicar las fechas 'desde' y 'hasta'");
        }
        if (desde.isAfter(hasta)) {
            throw new InvalidOperationException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        if (ChronoUnit.DAYS.between(desde, hasta) > MAX_RANGE_DAYS) {
            throw new InvalidOperationException("El rango maximo de un reporte es de un año");
        }
        return detalleRepository.findVendidosEnPeriodo(empresaId, OrderStatus.COMPLETADA,
                desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
    }

    private void validateLimit(int limite) {
        if (limite < 1 || limite > MAX_LIMIT) {
            throw new InvalidOperationException("El limite debe estar entre 1 y " + MAX_LIMIT);
        }
    }

    private ProductRankingDTO toRanking(List<SalesOrderLineModel> lineasDeUnProducto) {
        ProductModel producto = lineasDeUnProducto.getFirst().getVariante().getProducto();
        return new ProductRankingDTO(
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                categoryName(producto),
                sumUnits(lineasDeUnProducto),
                sum(lineasDeUnProducto, SalesOrderLineModel::getSubtotal),
                sum(lineasDeUnProducto, SalesOrderLineModel::getProfit)
        );
    }

    private <K extends Comparable<K>> TreeMap<K, List<SalesOrderLineModel>> groupBy(
            List<SalesOrderLineModel> lineas, Function<SalesOrderLineModel, K> clave) {
        return lineas.stream().collect(Collectors.groupingBy(clave, TreeMap::new, Collectors.toList()));
    }

    private List<GroupedSalesDTO> sortByTotal(Map<String, List<SalesOrderLineModel>> grupos, BigDecimal totalGeneral) {
        return grupos.entrySet().stream()
                .map(e -> toGroup(e.getKey(), e.getValue(), totalGeneral))
                .sorted(Comparator.comparing(GroupedSalesDTO::total).reversed())
                .toList();
    }

    private <K> List<GroupedSalesDTO> inOrder(Map<K, List<SalesOrderLineModel>> grupos,
                                                 Function<K, String> etiqueta, BigDecimal totalGeneral) {
        return grupos.entrySet().stream()
                .map(e -> toGroup(etiqueta.apply(e.getKey()), e.getValue(), totalGeneral))
                .toList();
    }

    private GroupedSalesDTO toGroup(String grupo, List<SalesOrderLineModel> lineas, BigDecimal totalGeneral) {
        BigDecimal total = sum(lineas, SalesOrderLineModel::getSubtotal);
        return new GroupedSalesDTO(grupo, countSales(lineas), sumUnits(lineas), total,
                percentage(total, totalGeneral));
    }

    private long countSales(List<SalesOrderLineModel> lineas) {
        return lineas.stream().map(d -> d.getOrdenVenta().getId()).distinct().count();
    }

    private long sumUnits(List<SalesOrderLineModel> lineas) {
        return lineas.stream().mapToLong(SalesOrderLineModel::getCantidad).sum();
    }

    private BigDecimal sum(List<SalesOrderLineModel> lineas, Function<SalesOrderLineModel, BigDecimal> valor) {
        return lineas.stream().map(valor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal lineCost(SalesOrderLineModel d) {
        return d.getCostoUnitario().multiply(BigDecimal.valueOf(d.getCantidad()));
    }

    private BigDecimal percentage(BigDecimal parte, BigDecimal total) {
        if (total.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return parte.multiply(BigDecimal.valueOf(100)).divide(total, 1, RoundingMode.HALF_UP);
    }

    private String categoryName(ProductModel producto) {
        return producto.getCategoria() != null ? producto.getCategoria().getNombre() : "Sin categoria";
    }

    private String dayName(DayOfWeek dia) {
        return dia.getDisplayName(TextStyle.FULL, ES_PE).toUpperCase(ES_PE);
    }
}
