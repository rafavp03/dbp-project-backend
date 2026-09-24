package dbp.projectbackend.repositories;

import dbp.projectbackend.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {
    Optional<UserModel> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmpresaId(Long empresaId);
    List<UserModel> findByEmpresaId(Long empresaId);
}
