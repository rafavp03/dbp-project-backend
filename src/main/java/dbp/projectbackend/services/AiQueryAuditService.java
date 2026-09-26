package dbp.projectbackend.services;

import dbp.projectbackend.models.AiQueryModel;
import dbp.projectbackend.repositories.AiQueryRepository;
import dbp.projectbackend.repositories.EnterpriseRepository;
import dbp.projectbackend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AiQueryAuditService {

    private static final int MAX_ANSWER_LENGTH = 4000;

    private final AiQueryRepository consultaRepository;
    private final UserRepository userRepository;
    private final EnterpriseRepository enterpriseRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void audit(Long usuarioId, Long empresaId, String pregunta, String respuesta, boolean exitosa) {
        String recortada = respuesta != null && respuesta.length() > MAX_ANSWER_LENGTH
                ? respuesta.substring(0, MAX_ANSWER_LENGTH)
                : respuesta;

        consultaRepository.save(new AiQueryModel(
                userRepository.getReferenceById(usuarioId),
                enterpriseRepository.getReferenceById(empresaId),
                pregunta, recortada, exitosa));
    }
}
