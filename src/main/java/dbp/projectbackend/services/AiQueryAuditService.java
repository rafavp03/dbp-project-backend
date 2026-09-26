package dbp.projectbackend.services;

import dbp.projectbackend.models.ConsultaIAModel;
import dbp.projectbackend.repositories.ConsultaIARepository;
import dbp.projectbackend.repositories.EnterpriseRepository;
import dbp.projectbackend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AiQueryAuditService {

    private static final int MAX_RESPUESTA = 4000;

    private final ConsultaIARepository consultaRepository;
    private final UserRepository userRepository;
    private final EnterpriseRepository enterpriseRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void audit(Long usuarioId, Long empresaId, String pregunta, String respuesta, boolean exitosa) {
        String recortada = respuesta != null && respuesta.length() > MAX_RESPUESTA
                ? respuesta.substring(0, MAX_RESPUESTA)
                : respuesta;

        consultaRepository.save(new ConsultaIAModel(
                userRepository.getReferenceById(usuarioId),
                enterpriseRepository.getReferenceById(empresaId),
                pregunta, recortada, exitosa));
    }
}
