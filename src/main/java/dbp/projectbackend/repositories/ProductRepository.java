package dbp.projectbackend.repositories;

import dbp.projectbackend.models.ProductModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductModel, Long> {
    Optional<ProductModel> findByEmpresaIdAndCodigo(Long empresaId, String codigo);
    boolean existsByEmpresaIdAndCodigo(Long empresaId, String codigo);

    Page<ProductModel> findByEmpresaId(Long empresaId, Pageable pageable);
    Page<ProductModel> findByEmpresaIdAndActivoTrue(Long empresaId, Pageable pageable);
}
