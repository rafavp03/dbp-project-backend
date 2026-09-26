package dbp.projectbackend.repositories;

import dbp.projectbackend.models.StockMovementModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovementModel, Long> {
    List<StockMovementModel> findByVarianteIdOrderByFechaDesc(Long varianteId);

    List<StockMovementModel> findByVarianteProductoIdOrderByFechaDesc(Long productoId);

    List<StockMovementModel> findByVarianteProductoEmpresaIdOrderByFechaDesc(Long empresaId);
}
