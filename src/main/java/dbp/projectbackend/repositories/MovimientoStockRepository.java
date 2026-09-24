package dbp.projectbackend.repositories;

import dbp.projectbackend.models.MovimientoStockModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStockModel, Long> {
    // Kardex de un producto, del mas reciente al mas antiguo
    List<MovimientoStockModel> findByProductoIdOrderByFechaDesc(Long productoId);

    // Todos los movimientos de una empresa
    List<MovimientoStockModel> findByProductoEmpresaIdOrderByFechaDesc(Long empresaId);
}
