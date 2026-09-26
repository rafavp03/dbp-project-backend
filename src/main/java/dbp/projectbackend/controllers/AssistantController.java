package dbp.projectbackend.controllers;

import dbp.projectbackend.dtos.request.QuestionDTO;
import dbp.projectbackend.dtos.response.AssistantAnswerResponseDTO;
import dbp.projectbackend.dtos.response.AiQueryResponseDTO;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.services.AssistantService;
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
public class AssistantController {

    private final AssistantService service;

    @PostMapping
    public ResponseEntity<AssistantAnswerResponseDTO> ask(@AuthenticationPrincipal UserModel user,
                                                           @Valid @RequestBody QuestionDTO dto) {
        return ResponseEntity.ok(service.ask(user, dto.pregunta()));
    }

    @GetMapping("/historial")
    public ResponseEntity<List<AiQueryResponseDTO>> history(@AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(service.history(user));
    }
}
