package dbp.projectbackend.repositories;

import dbp.projectbackend.models.ConsultaIAModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConsultaIARepository extends JpaRepository<ConsultaIAModel, Long> {
    long countByUsuarioIdAndExitosaTrueAndFechaGreaterThanEqual(Long usuarioId, LocalDateTime desde);

    List<ConsultaIAModel> findTop20ByUsuarioIdOrderByFechaDesc(Long usuarioId);
}
