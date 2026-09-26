package dbp.projectbackend.repositories;

import dbp.projectbackend.models.AiQueryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AiQueryRepository extends JpaRepository<AiQueryModel, Long> {
    long countByUsuarioIdAndExitosaTrueAndFechaGreaterThanEqual(Long usuarioId, LocalDateTime desde);

    List<AiQueryModel> findTop20ByUsuarioIdOrderByFechaDesc(Long usuarioId);
}
