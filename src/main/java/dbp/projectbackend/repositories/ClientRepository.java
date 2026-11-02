package dbp.projectbackend.repositories;

import dbp.projectbackend.models.ClientModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<ClientModel, Long> {
    Optional<ClientModel> findByEmpresaIdAndDocumento(Long empresaId, String documento);
    boolean existsByEmpresaIdAndDocumento(Long empresaId, String documento);

    Page<ClientModel> findByEmpresaId(Long empresaId, Pageable pageable);
}
