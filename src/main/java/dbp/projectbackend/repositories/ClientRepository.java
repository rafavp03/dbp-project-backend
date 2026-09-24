package dbp.projectbackend.repositories;

import dbp.projectbackend.models.ClientModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<ClientModel, Long> {
    List<ClientModel> findByEmpresaId(Long empresaId);
    Optional<ClientModel> findByEmpresaIdAndDocumento(Long empresaId, String documento);
    boolean existsByEmpresaIdAndDocumento(Long empresaId, String documento);
}
