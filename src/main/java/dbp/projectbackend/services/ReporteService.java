package dbp.projectbackend.services;

import dbp.projectbackend.dtos.reportes.*;
import dbp.projectbackend.exceptions.InvalidOperationException;
import dbp.projectbackend.models.DetalleOrdenVentaModel;
import dbp.projectbackend.models.EstadoOrden;
import dbp.projectbackend.models.ProductModel;
import dbp.projectbackend.models.VarianteProductoModel;
import dbp.projectbackend.repositories.DetalleOrdenVentaRepository;
import dbp.projectbackend.repositories.VarianteProductoRepository;
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
public class ReporteService {

    private static final int MAX_DIAS_RANGO = 366;
    private static final int MAX_LIMITE = 50;
    private static final Locale ES_PE = Locale.of("es", "PE");

    private final DetalleOrdenVentaRepository detalleRepository;
    private final VarianteProductoRepository varianteRepository;

    public ResumenVentasDTO resumen(Long empresaId, LocalDate desde, LocalDate hasta) {
        List<DetalleOrdenVentaModel> lineas = lineasDelPeriodo(empresaId, desde, hasta);

        long numeroVentas = contarVentas(lineas);
        BigDecimal total = sumar(lineas, DetalleOrdenVentaModel::getSubtotal);
        BigDecimal costo = sumar(lineas, this::costoLinea);
        BigDecimal ganancia = total.subtract(costo);

        return new ResumenVentasDTO(
                desde,
                hasta,
                numeroVentas,
                sumarUnidades(lineas),
                total,
                costo,
                ganancia,
                porcentaje(ganancia, total),
                sumar(lineas, DetalleOrdenVentaModel::getDescuento),
                numeroVentas == 0 ? BigDecimal.ZERO : total.divide(BigDecimal.valueOf(numeroVentas), 2, RoundingMode.HALF_UP)
        );
    }

    public List<ProductoRankingDTO> topProductos(Long empresaId, LocalDate desde, LocalDate hasta,
                                                 CriterioRanking criterio, int limite) {
        validarLimite(limite);
        Map<Long, List<DetalleOrdenVentaModel>> porProducto = lineasDelPeriodo(empresaId, desde, hasta).stream()
                .collect(Collectors.groupingBy(d -> d.getVariante().getProducto().getId()));

        Comparator<ProductoRankingDTO> orden = criterio == CriterioRanking.GANANCIA
                ? Comparator.comparing(ProductoRankingDTO::ganancia)
                : Comparator.comparingLong(ProductoRankingDTO::unidades);

        return porProducto.values().stream()
                .map(this::toRanking)
                .sorted(orden.reversed())
                .limit(limite)
                .toList();
    }

    public List<VentasAgrupadasDTO> ventasAgrupadas(Long empresaId, LocalDate desde, LocalDate hasta,
                                                    AgrupacionVentas agrupacion) {
        List<DetalleOrdenVentaModel> lineas = lineasDelPeriodo(empresaId, desde, hasta);
        BigDecimal totalGeneral = sumar(lineas, DetalleOrdenVentaModel::getSubtotal);

        return switch (agrupacion) {
            case MEDIO_PAGO -> ordenarPorTotal(agrupar(lineas, d -> d.getOrdenVenta().getMedioPago().name()), totalGeneral);
            case CANAL -> ordenarPorTotal(agrupar(lineas, d -> d.getOrdenVenta().getCanal().name()), totalGeneral);
            case CATEGORIA -> ordenarPorTotal(agrupar(lineas, d -> nombreCategoria(d.getVariante().getProducto())), totalGeneral);
            case DIA_SEMANA -> enOrden(agrupar(lineas, d -> d.getOrdenVenta().getFechaEmision().getDayOfWeek()),
                    this::nombreDia, totalGeneral);
            case HORA -> enOrden(this.<Integer>agrupar(lineas, d -> d.getOrdenVenta().getFechaEmision().getHour()),
                    (Integer h) -> "%02d:00-%02d:00".formatted(h, (h + 1) % 24), totalGeneral);
        };
    }

    public List<StockBajoDTO> stockBajo(Long empresaId) {
        return varianteRepository.findStockBajo(empresaId).stream()
                .filter(v -> Boolean.TRUE.equals(v.getProducto().getActivo()))
                .sorted(Comparator.comparingInt(VarianteProductoModel::getStock))
                .map(v -> new StockBajoDTO(v.getId(), v.getProducto().getNombre(), v.getTalla(), v.getColor(),
                        v.getStock(), v.getStockMinimo()))
                .toList();
    }

    public List<ProductoParadoDTO> stockParado(Long empresaId, int dias) {
        if (dias < 1) {
            throw new InvalidOperationException("Los dias deben ser mayores a 0");
        }

        Map<Long, LocalDateTime> ultimaVenta = new HashMap<>();
        for (Object[] fila : detalleRepository.findUltimaVentaPorVariante(empresaId, EstadoOrden.COMPLETADA)) {
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
                    return new ProductoParadoDTO(
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
                .sorted(Comparator.comparing(ProductoParadoDTO::capitalInmovilizado).reversed())
                .toList();
    }

    private List<DetalleOrdenVentaModel> lineasDelPeriodo(Long empresaId, LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new InvalidOperationException("Debes indicar las fechas 'desde' y 'hasta'");
        }
        if (desde.isAfter(hasta)) {
            throw new InvalidOperationException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        if (ChronoUnit.DAYS.between(desde, hasta) > MAX_DIAS_RANGO) {
            throw new InvalidOperationException("El rango maximo de un reporte es de un año");
        }
        return detalleRepository.findVendidosEnPeriodo(empresaId, EstadoOrden.COMPLETADA,
                desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
    }

    private void validarLimite(int limite) {
        if (limite < 1 || limite > MAX_LIMITE) {
            throw new InvalidOperationException("El limite debe estar entre 1 y " + MAX_LIMITE);
        }
    }

    private ProductoRankingDTO toRanking(List<DetalleOrdenVentaModel> lineasDeUnProducto) {
        ProductModel producto = lineasDeUnProducto.getFirst().getVariante().getProducto();
        return new ProductoRankingDTO(
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                nombreCategoria(producto),
                sumarUnidades(lineasDeUnProducto),
                sumar(lineasDeUnProducto, DetalleOrdenVentaModel::getSubtotal),
                sumar(lineasDeUnProducto, DetalleOrdenVentaModel::getGanancia)
        );
    }

    private <K extends Comparable<K>> TreeMap<K, List<DetalleOrdenVentaModel>> agrupar(
            List<DetalleOrdenVentaModel> lineas, Function<DetalleOrdenVentaModel, K> clave) {
        return lineas.stream().collect(Collectors.groupingBy(clave, TreeMap::new, Collectors.toList()));
    }

    private List<VentasAgrupadasDTO> ordenarPorTotal(Map<String, List<DetalleOrdenVentaModel>> grupos, BigDecimal totalGeneral) {
        return grupos.entrySet().stream()
                .map(e -> toGrupo(e.getKey(), e.getValue(), totalGeneral))
                .sorted(Comparator.comparing(VentasAgrupadasDTO::total).reversed())
                .toList();
    }

    private <K> List<VentasAgrupadasDTO> enOrden(Map<K, List<DetalleOrdenVentaModel>> grupos,
                                                 Function<K, String> etiqueta, BigDecimal totalGeneral) {
        return grupos.entrySet().stream()
                .map(e -> toGrupo(etiqueta.apply(e.getKey()), e.getValue(), totalGeneral))
                .toList();
    }

    private VentasAgrupadasDTO toGrupo(String grupo, List<DetalleOrdenVentaModel> lineas, BigDecimal totalGeneral) {
        BigDecimal total = sumar(lineas, DetalleOrdenVentaModel::getSubtotal);
        return new VentasAgrupadasDTO(grupo, contarVentas(lineas), sumarUnidades(lineas), total,
                porcentaje(total, totalGeneral));
    }

    private long contarVentas(List<DetalleOrdenVentaModel> lineas) {
        return lineas.stream().map(d -> d.getOrdenVenta().getId()).distinct().count();
    }

    private long sumarUnidades(List<DetalleOrdenVentaModel> lineas) {
        return lineas.stream().mapToLong(DetalleOrdenVentaModel::getCantidad).sum();
    }

    private BigDecimal sumar(List<DetalleOrdenVentaModel> lineas, Function<DetalleOrdenVentaModel, BigDecimal> valor) {
        return lineas.stream().map(valor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal costoLinea(DetalleOrdenVentaModel d) {
        return d.getCostoUnitario().multiply(BigDecimal.valueOf(d.getCantidad()));
    }

    private BigDecimal porcentaje(BigDecimal parte, BigDecimal total) {
        if (total.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return parte.multiply(BigDecimal.valueOf(100)).divide(total, 1, RoundingMode.HALF_UP);
    }

    private String nombreCategoria(ProductModel producto) {
        return producto.getCategoria() != null ? producto.getCategoria().getNombre() : "Sin categoria";
    }

    private String nombreDia(DayOfWeek dia) {
        return dia.getDisplayName(TextStyle.FULL, ES_PE).toUpperCase(ES_PE);
    }
}
