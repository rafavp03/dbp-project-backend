package dbp.projectbackend.repositories;

import dbp.projectbackend.models.AiQueryModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AiQueryRepository extends JpaRepository<AiQueryModel, Long> {
    long countByUsuarioIdAndExitosaTrueAndFechaGreaterThanEqual(Long usuarioId, LocalDateTime desde);

    Page<AiQueryModel> findByUsuarioId(Long usuarioId, Pageable pageable);
}
