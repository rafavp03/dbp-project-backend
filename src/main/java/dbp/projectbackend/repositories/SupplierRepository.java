package dbp.projectbackend.repositories;

import dbp.projectbackend.models.SupplierModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<SupplierModel, Long> {
    Optional<SupplierModel> findByRuc(String ruc);

    Page<SupplierModel> findByEmpresasId(Long empresaId, Pageable pageable);
}
