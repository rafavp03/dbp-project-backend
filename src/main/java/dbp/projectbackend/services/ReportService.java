package dbp.projectbackend.services;

import dbp.projectbackend.dtos.response.report.*;
import dbp.projectbackend.enums.SalesGrouping;
import dbp.projectbackend.enums.RankingCriterion;
import dbp.projectbackend.enums.OrderStatus;
import dbp.projectbackend.exceptions.InvalidOperationException;
import dbp.projectbackend.models.PurchaseOrderLineModel;
import dbp.projectbackend.models.SalesOrderLineModel;
import dbp.projectbackend.models.ProductModel;
import dbp.projectbackend.models.ProductVariantModel;
import dbp.projectbackend.repositories.PurchaseOrderRepository;
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
    private final PurchaseOrderRepository compraRepository;

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

    /** Ventas de cada dia del periodo (grupo = fecha AAAA-MM-DD), incluidos los dias sin ventas. */
    public List<GroupedSalesDTO> salesByDay(Long empresaId, LocalDate desde, LocalDate hasta) {
        List<SalesOrderLineModel> lineas = periodLines(empresaId, desde, hasta);
        BigDecimal totalGeneral = sum(lineas, SalesOrderLineModel::getSubtotal);
        Map<LocalDate, List<SalesOrderLineModel>> porDia = lineas.stream()
                .collect(Collectors.groupingBy(d -> d.getOrdenVenta().getFechaEmision().toLocalDate()));

        List<GroupedSalesDTO> dias = new ArrayList<>();
        for (LocalDate dia = desde; !dia.isAfter(hasta); dia = dia.plusDays(1)) {
            dias.add(toGroup(dia.toString(), porDia.getOrDefault(dia, List.of()), totalGeneral));
        }
        return dias;
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

    /**
     * Reposicion inteligente: para cada talla y color activa calcula su ritmo de venta en los ultimos
     * {@code dias}, en cuantos dias se agota y cuanto comprar para cubrir {@code cobertura} dias de venta
     * mas el stock minimo. Tambien propone el ultimo proveedor y el ultimo costo de compra.
     */
    public List<RestockSuggestionDTO> restockSuggestions(Long empresaId, int dias, int cobertura) {
        if (dias < 7 || dias > 180) {
            throw new InvalidOperationException("El periodo de analisis debe estar entre 7 y 180 dias");
        }
        if (cobertura < 1 || cobertura > 120) {
            throw new InvalidOperationException("La cobertura debe estar entre 1 y 120 dias");
        }

        LocalDateTime ahora = LocalDateTime.now();
        Map<Long, Integer> unidadesPorVariante = new HashMap<>();
        for (SalesOrderLineModel linea : detalleRepository.findVendidosEnPeriodo(
                empresaId, OrderStatus.COMPLETADA, ahora.minusDays(dias), ahora.plusSeconds(1))) {
            unidadesPorVariante.merge(linea.getVariante().getId(), linea.getCantidad(), Integer::sum);
        }

        List<RestockCalculation> calculos = new ArrayList<>();
        for (ProductVariantModel variante : varianteRepository.findByProductoEmpresaId(empresaId)) {
            if (!Boolean.TRUE.equals(variante.getActivo()) || !Boolean.TRUE.equals(variante.getProducto().getActivo())) {
                continue;
            }
            int unidades = unidadesPorVariante.getOrDefault(variante.getId(), 0);
            int stock = variante.getStock();
            int stockMinimo = variante.getStockMinimo();

            // Un producto registrado hace menos dias que el periodo se mide solo sobre los dias que existe.
            LocalDateTime registro = variante.getProducto().getFechaRegistro();
            long diasConHistoria = registro == null ? dias : ChronoUnit.DAYS.between(registro, ahora) + 1;
            long diasEfectivos = Math.max(1, Math.min(dias, diasConHistoria));
            double ventaDiaria = (double) unidades / diasEfectivos;

            Integer diasRestantes = ventaDiaria > 0 ? (int) Math.floor(stock / ventaDiaria) : null;
            int objetivo = (int) Math.ceil(ventaDiaria * cobertura) + stockMinimo;
            int cantidadSugerida = objetivo - stock;

            boolean seAgotaPronto = diasRestantes != null && diasRestantes <= cobertura;
            boolean bajoMinimo = stock < stockMinimo;
            if (cantidadSugerida > 0 && (seAgotaPronto || bajoMinimo)) {
                calculos.add(new RestockCalculation(variante, unidades, ventaDiaria, diasRestantes, cantidadSugerida));
            }
        }
        if (calculos.isEmpty()) {
            return List.of();
        }

        // La consulta viene de la compra mas reciente a la mas antigua: la primera linea de cada variante es la ultima compra.
        Map<Long, PurchaseOrderLineModel> ultimaCompra = new HashMap<>();
        List<Long> varianteIds = calculos.stream().map(c -> c.variante().getId()).toList();
        for (PurchaseOrderLineModel linea : compraRepository.findComprasDeVariantes(empresaId, OrderStatus.ANULADA, varianteIds)) {
            ultimaCompra.putIfAbsent(linea.getVariante().getId(), linea);
        }

        LocalDate hoy = ahora.toLocalDate();
        return calculos.stream()
                .map(c -> toRestockSuggestion(c, ultimaCompra.get(c.variante().getId()), hoy))
                .sorted(Comparator.comparingInt((RestockSuggestionDTO s) -> urgencyOrder(s.urgencia()))
                        .thenComparingInt(s -> s.diasRestantes() == null ? Integer.MAX_VALUE : s.diasRestantes())
                        .thenComparing(Comparator.comparing(RestockSuggestionDTO::ventaEnRiesgoDiaria).reversed()))
                .toList();
    }

    private record RestockCalculation(ProductVariantModel variante, int unidades, double ventaDiaria,
                                      Integer diasRestantes, int cantidadSugerida) {}

    private RestockSuggestionDTO toRestockSuggestion(RestockCalculation c, PurchaseOrderLineModel ultimaCompra, LocalDate hoy) {
        ProductVariantModel variante = c.variante();
        ProductModel producto = variante.getProducto();

        BigDecimal costoUnitario = ultimaCompra != null
                ? ultimaCompra.getPrecioUnitario()
                : Optional.ofNullable(producto.getPrecioCompra()).orElse(BigDecimal.ZERO);
        BigDecimal ventaDiaria = BigDecimal.valueOf(c.ventaDiaria()).setScale(2, RoundingMode.HALF_UP);

        String urgencia;
        if (c.diasRestantes() == null) {
            urgencia = "PLANIFICAR";
        } else if (variante.getStock() <= 0) {
            urgencia = "AGOTADO";
        } else if (c.diasRestantes() <= 7) {
            urgencia = "CRITICO";
        } else if (c.diasRestantes() <= 14) {
            urgencia = "PRONTO";
        } else {
            urgencia = "PLANIFICAR";
        }

        return new RestockSuggestionDTO(
                variante.getId(),
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                variante.getTalla(),
                variante.getColor(),
                variante.getStock(),
                variante.getStockMinimo(),
                c.unidades(),
                ventaDiaria,
                c.diasRestantes(),
                c.diasRestantes() != null ? hoy.plusDays(c.diasRestantes()) : null,
                urgencia,
                c.cantidadSugerida(),
                costoUnitario,
                costoUnitario.multiply(BigDecimal.valueOf(c.cantidadSugerida())),
                producto.getPrecioVenta().multiply(ventaDiaria).setScale(2, RoundingMode.HALF_UP),
                ultimaCompra != null ? ultimaCompra.getOrdenCompra().getProveedor().getId() : null,
                ultimaCompra != null ? ultimaCompra.getOrdenCompra().getProveedor().getRazonSocial() : null
        );
    }

    private int urgencyOrder(String urgencia) {
        return switch (urgencia) {
            case "AGOTADO" -> 0;
            case "CRITICO" -> 1;
            case "PRONTO" -> 2;
            default -> 3;
        };
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
