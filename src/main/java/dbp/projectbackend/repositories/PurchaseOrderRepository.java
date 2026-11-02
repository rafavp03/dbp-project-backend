package dbp.projectbackend.repositories;

import dbp.projectbackend.models.PurchaseOrderModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrderModel, Long> {

    Page<PurchaseOrderModel> findByEmpresaId(Long empresaId, Pageable pageable);

    Page<PurchaseOrderModel> findByEmpresaIdAndFechaEmisionGreaterThanEqualAndFechaEmisionLessThan(
            Long empresaId, LocalDateTime desde, LocalDateTime hasta, Pageable pageable);
}
