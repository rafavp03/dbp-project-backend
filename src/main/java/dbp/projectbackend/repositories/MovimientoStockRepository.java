package dbp.projectbackend.repositories;

import dbp.projectbackend.models.MovimientoStockModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStockModel, Long> {
    List<MovimientoStockModel> findByVarianteIdOrderByFechaDesc(Long varianteId);

    List<MovimientoStockModel> findByVarianteProductoIdOrderByFechaDesc(Long productoId);

    List<MovimientoStockModel> findByVarianteProductoEmpresaIdOrderByFechaDesc(Long empresaId);
}
