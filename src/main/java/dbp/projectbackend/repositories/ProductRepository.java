package dbp.projectbackend.repositories;

import dbp.projectbackend.models.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductModel, Long> {
    List<ProductModel> findByEmpresaId(Long empresaId);
    List<ProductModel> findByEmpresaIdAndActivoTrue(Long empresaId);
    Optional<ProductModel> findByEmpresaIdAndCodigo(Long empresaId, String codigo);
    boolean existsByEmpresaIdAndCodigo(Long empresaId, String codigo);
}
