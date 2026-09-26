package dbp.projectbackend.repositories;

import dbp.projectbackend.models.SupplierModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<SupplierModel, Long> {
    Optional<SupplierModel> findByRuc(String ruc);
    List<SupplierModel> findByEmpresasId(Long empresaId);
}
