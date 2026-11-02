package dbp.projectbackend.repositories;

import dbp.projectbackend.models.SalesOrderModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrderModel, Long> {

    Page<SalesOrderModel> findByEmpresaId(Long empresaId, Pageable pageable);

    Page<SalesOrderModel> findByEmpresaIdAndFechaEmisionGreaterThanEqualAndFechaEmisionLessThan(
            Long empresaId, LocalDateTime desde, LocalDateTime hasta, Pageable pageable);
}
