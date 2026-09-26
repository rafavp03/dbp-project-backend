package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.request.PreguntaDTO;
import dbp.projectbackend.dtos.response.AssistantAnswerResponseDTO;
import dbp.projectbackend.dtos.response.ConsultaIAResponseDTO;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.services.AsistenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/asistente")
@PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
public class AsistenteController {

    private final AsistenteService service;

    @PostMapping
    public ResponseEntity<AssistantAnswerResponseDTO> preguntar(@AuthenticationPrincipal UserModel user,
                                                           @Valid @RequestBody PreguntaDTO dto) {
        return ResponseEntity.ok(service.preguntar(user, dto.pregunta()));
    }

    @GetMapping("/historial")
    public ResponseEntity<List<ConsultaIAResponseDTO>> historial(@AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(service.historial(user));
    }
}
