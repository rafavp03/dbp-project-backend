package dbp.projectbackend.repositories;

import dbp.projectbackend.models.ProductVariantModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariantModel, Long> {
    List<ProductVariantModel> findByProductoEmpresaId(Long empresaId);
    Optional<ProductVariantModel> findByProductoIdAndTallaAndColor(Long productoId, String talla, String color);

    @Query("SELECT v FROM ProductVariantModel v " +
           "WHERE v.producto.empresa.id = :empresaId AND v.activo = true AND v.stock <= v.stockMinimo")
    List<ProductVariantModel> findStockBajo(@Param("empresaId") Long empresaId);

    @Query("SELECT v FROM ProductVariantModel v JOIN FETCH v.producto WHERE v.id IN :ids")
    List<ProductVariantModel> findAllByIdInWithProducto(@Param("ids") List<Long> ids);

    Page<ProductVariantModel> findByProductoId(Long productoId, Pageable pageable);

    @Query("SELECT v FROM ProductVariantModel v " +
           "WHERE v.producto.empresa.id = :empresaId AND v.activo = true AND v.stock <= v.stockMinimo")
    Page<ProductVariantModel> findStockBajo(@Param("empresaId") Long empresaId, Pageable pageable);
}
