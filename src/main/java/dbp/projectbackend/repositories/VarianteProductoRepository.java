package dbp.projectbackend.repositories;

import dbp.projectbackend.models.VarianteProductoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VarianteProductoRepository extends JpaRepository<VarianteProductoModel, Long> {
    List<VarianteProductoModel> findByProductoId(Long productoId);
    List<VarianteProductoModel> findByProductoEmpresaId(Long empresaId);
    Optional<VarianteProductoModel> findByProductoIdAndTallaAndColor(Long productoId, String talla, String color);

    // Variantes activas de una empresa que llegaron a su stock minimo (para reponer)
    @Query("SELECT v FROM VarianteProductoModel v " +
           "WHERE v.producto.empresa.id = :empresaId AND v.activo = true AND v.stock <= v.stockMinimo")
    List<VarianteProductoModel> findStockBajo(@Param("empresaId") Long empresaId);
}
