package dbp.projectbackend.repositories;

import dbp.projectbackend.models.ClientModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<ClientModel, Long> {
    Optional<ClientModel> findByEmpresaIdAndDocumento(Long empresaId, String documento);
    boolean existsByEmpresaIdAndDocumento(Long empresaId, String documento);

    Page<ClientModel> findByEmpresaId(Long empresaId, Pageable pageable);

    @Query("""
            SELECT c FROM ClientModel c
            WHERE c.empresa.id = :empresaId
              AND (LOWER(c.nombre) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR c.documento LIKE CONCAT('%', :search, '%'))
            """)
    Page<ClientModel> searchByEmpresaId(@Param("empresaId") Long empresaId,
                                        @Param("search") String search,
                                        Pageable pageable);
}
