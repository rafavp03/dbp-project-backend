package dbp.projectbackend.repositories;

import dbp.projectbackend.models.DetalleOrdenVentaModel;
import dbp.projectbackend.models.EstadoOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetalleOrdenVentaRepository extends JpaRepository<DetalleOrdenVentaModel, Long> {

    // Lineas vendidas por una empresa en un periodo [desde, hasta).
    // JOIN FETCH trae la venta, la variante y el producto en una sola consulta.
    @Query("SELECT d FROM DetalleOrdenVentaModel d " +
           "JOIN FETCH d.ordenVenta o " +
           "JOIN FETCH d.variante v " +
           "JOIN FETCH v.producto p " +
           "WHERE o.empresa.id = :empresaId AND o.estado = :estado " +
           "AND o.fechaEmision >= :desde AND o.fechaEmision < :hasta")
    List<DetalleOrdenVentaModel> findVendidosEnPeriodo(@Param("empresaId") Long empresaId,
                                                        @Param("estado") EstadoOrden estado,
                                                        @Param("desde") LocalDateTime desde,
                                                        @Param("hasta") LocalDateTime hasta);

    // Fecha de la ultima venta de cada variante de la empresa. Cada fila: [varianteId, fecha]
    @Query("SELECT d.variante.id, MAX(d.ordenVenta.fechaEmision) FROM DetalleOrdenVentaModel d " +
           "WHERE d.ordenVenta.empresa.id = :empresaId AND d.ordenVenta.estado = :estado " +
           "GROUP BY d.variante.id")
    List<Object[]> findUltimaVentaPorVariante(@Param("empresaId") Long empresaId,
                                              @Param("estado") EstadoOrden estado);
}
