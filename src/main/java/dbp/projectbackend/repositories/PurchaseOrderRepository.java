package dbp.projectbackend.repositories;

import dbp.projectbackend.enums.OrderStatus;
import dbp.projectbackend.models.PurchaseOrderLineModel;
import dbp.projectbackend.models.PurchaseOrderModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrderModel, Long> {

    Page<PurchaseOrderModel> findByEmpresaId(Long empresaId, Pageable pageable);

    Page<PurchaseOrderModel> findByEmpresaIdAndFechaEmisionGreaterThanEqualAndFechaEmisionLessThan(
            Long empresaId, LocalDateTime desde, LocalDateTime hasta, Pageable pageable);

    /** Lineas de compra de las variantes indicadas, de la mas reciente a la mas antigua, con su proveedor. */
    @Query("SELECT d FROM PurchaseOrderLineModel d " +
           "JOIN FETCH d.ordenCompra o " +
           "JOIN FETCH o.proveedor " +
           "WHERE o.empresa.id = :empresaId AND o.estado <> :estadoExcluido " +
           "AND d.variante.id IN :varianteIds " +
           "ORDER BY o.fechaEmision DESC")
    List<PurchaseOrderLineModel> findComprasDeVariantes(@Param("empresaId") Long empresaId,
                                                        @Param("estadoExcluido") OrderStatus estadoExcluido,
                                                        @Param("varianteIds") List<Long> varianteIds);
}
