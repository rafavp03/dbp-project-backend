package dbp.projectbackend.repositories;

import dbp.projectbackend.models.StockMovementModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovementModel, Long> {

    List<StockMovementModel> findByVarianteProductoIdOrderByFechaDesc(Long productoId);

    List<StockMovementModel> findByVarianteProductoEmpresaIdOrderByFechaDesc(Long empresaId);

    Page<StockMovementModel> findByVarianteId(Long varianteId, Pageable pageable);
}
