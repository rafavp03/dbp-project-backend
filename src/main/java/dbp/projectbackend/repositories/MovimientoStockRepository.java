package dbp.projectbackend.repositories;

import dbp.projectbackend.models.MovimientoStockModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStockModel, Long> {
    // Kardex de una variante (talla + color), del mas reciente al mas antiguo
    List<MovimientoStockModel> findByVarianteIdOrderByFechaDesc(Long varianteId);

    // Kardex de un producto (todas sus tallas y colores)
    List<MovimientoStockModel> findByVarianteProductoIdOrderByFechaDesc(Long productoId);

    // Todos los movimientos de una empresa
    List<MovimientoStockModel> findByVarianteProductoEmpresaIdOrderByFechaDesc(Long empresaId);
}
