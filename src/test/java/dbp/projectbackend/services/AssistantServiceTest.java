package dbp.projectbackend.services;

import dbp.projectbackend.enums.Role;
import dbp.projectbackend.exceptions.AssistantUnavailableException;
import dbp.projectbackend.exceptions.QueryLimitExceededException;
import dbp.projectbackend.models.EnterpriseModel;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.repositories.AiQueryRepository;
import dbp.projectbackend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.ai.chat.client.ChatClient;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AssistantServiceTest {

    private static final int LIMITE = 30;

    @Mock private ChatClient.Builder chatClientBuilder;
    @Mock private ChatClient chatClient;
    @Mock private ReportService reportService;
    @Mock private AiQueryAuditService auditService;
    @Mock private AiQueryRepository consultaRepository;
    @Mock private UserRepository userRepository;

    private UserModel usuario;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        usuario = new UserModel("Rafael", "rafael@tienda.pe", "hash", Role.ADMIN,
                new EnterpriseModel("12345678901", "Tienda Clara SAC"));
    }

    private AssistantService serviceConClave(String apiKey) {
        return new AssistantService(chatClientBuilder, reportService, auditService,
                consultaRepository, userRepository, LIMITE, apiKey);
    }

    private void cuotaConsumida(long consultasHoy) {
        when(userRepository.findByIdForUpdate(any())).thenReturn(Optional.of(usuario));
        when(consultaRepository.countByUsuarioIdAndExitosaTrueAndFechaGreaterThanEqual(any(), any(LocalDateTime.class)))
                .thenReturn(consultasHoy);
    }

    @Test
    void askFallaSiNoHayClaveDeIaConfigurada() {
        AssistantService service = serviceConClave("");

        assertThatThrownBy(() -> service.ask(usuario, "¿Qué repongo?"))
                .isInstanceOf(AssistantUnavailableException.class);

        verify(userRepository, never()).findByIdForUpdate(any());
        verify(auditService, never()).audit(any(), any(), any(), any(), anyBoolean());
    }

    @Test
    void askFallaAlAlcanzarLaCuotaDiaria() {
        AssistantService service = serviceConClave("una-clave");
        cuotaConsumida(LIMITE);

        assertThatThrownBy(() -> service.ask(usuario, "¿Cuánto vendí ayer?"))
                .isInstanceOf(QueryLimitExceededException.class)
                .hasMessageContaining(String.valueOf(LIMITE));
    }

    @Test
    void askTomaElLockPesimistaAntesDeContarLaCuota() {
        AssistantService service = serviceConClave("una-clave");
        cuotaConsumida(LIMITE);

        assertThatThrownBy(() -> service.ask(usuario, "¿Cuánto vendí ayer?"))
                .isInstanceOf(QueryLimitExceededException.class);

        verify(userRepository).findByIdForUpdate(any());
    }

    @Test
    void askNoRegistraAuditoriaCuandoSeRechazaPorCuota() {
        AssistantService service = serviceConClave("una-clave");
        cuotaConsumida(LIMITE);

        assertThatThrownBy(() -> service.ask(usuario, "¿Cuánto vendí ayer?"))
                .isInstanceOf(QueryLimitExceededException.class);

        verify(auditService, never()).audit(any(), any(), any(), any(), anyBoolean());
    }
}
