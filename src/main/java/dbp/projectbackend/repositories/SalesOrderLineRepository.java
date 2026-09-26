package dbp.projectbackend.repositories;

import dbp.projectbackend.enums.OrderStatus;
import dbp.projectbackend.models.SalesOrderLineModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SalesOrderLineRepository extends JpaRepository<SalesOrderLineModel, Long> {

    @Query("SELECT d FROM SalesOrderLineModel d " +
           "JOIN FETCH d.ordenVenta o " +
           "JOIN FETCH d.variante v " +
           "JOIN FETCH v.producto p " +
           "WHERE o.empresa.id = :empresaId AND o.estado = :estado " +
           "AND o.fechaEmision >= :desde AND o.fechaEmision < :hasta")
    List<SalesOrderLineModel> findVendidosEnPeriodo(@Param("empresaId") Long empresaId,
                                                        @Param("estado") OrderStatus estado,
                                                        @Param("desde") LocalDateTime desde,
                                                        @Param("hasta") LocalDateTime hasta);

    @Query("SELECT d.variante.id, MAX(d.ordenVenta.fechaEmision) FROM SalesOrderLineModel d " +
           "WHERE d.ordenVenta.empresa.id = :empresaId AND d.ordenVenta.estado = :estado " +
           "GROUP BY d.variante.id")
    List<Object[]> findUltimaVentaPorVariante(@Param("empresaId") Long empresaId,
                                              @Param("estado") OrderStatus estado);
}
