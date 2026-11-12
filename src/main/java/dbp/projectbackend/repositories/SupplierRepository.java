package dbp.projectbackend.repositories;

import dbp.projectbackend.models.SupplierModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<SupplierModel, Long> {
    Optional<SupplierModel> findByRuc(String ruc);

    Page<SupplierModel> findByEmpresasId(Long empresaId, Pageable pageable);

    @Query("""
            SELECT s FROM SupplierModel s JOIN s.empresas e
            WHERE e.id = :empresaId
              AND (LOWER(s.razonSocial) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR s.ruc LIKE CONCAT('%', :search, '%'))
            """)
    Page<SupplierModel> searchByEmpresasId(@Param("empresaId") Long empresaId,
                                           @Param("search") String search,
                                           Pageable pageable);
}
