package dbp.projectbackend.repositories;

import dbp.projectbackend.models.ConsultaIAModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConsultaIARepository extends JpaRepository<ConsultaIAModel, Long> {
    // Preguntas respondidas desde una fecha (para el limite diario)
    long countByUsuarioIdAndExitosaTrueAndFechaGreaterThanEqual(Long usuarioId, LocalDateTime desde);

    // Ultimas 20 preguntas del usuario (historial)
    List<ConsultaIAModel> findTop20ByUsuarioIdOrderByFechaDesc(Long usuarioId);
}
