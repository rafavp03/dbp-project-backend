package dbp.projectbackend.listeners;

import dbp.projectbackend.config.AsyncConfig;
import dbp.projectbackend.enums.MovementType;
import dbp.projectbackend.enums.Role;
import dbp.projectbackend.events.StockMovementRegisteredEvent;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.repositories.UserRepository;
import dbp.projectbackend.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

/**
 * Un ajuste por conteo físico cambia el stock sin una compra o venta que lo respalde,
 * así que el dueño debe enterarse: puede ser merma, robo o un error de registro.
 */
@Component
@RequiredArgsConstructor
public class StockAdjustmentListener {

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Async(AsyncConfig.NOTIFICATIONS_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStockMovementRegistered(StockMovementRegisteredEvent event) {
        if (event.getTipo() != MovementType.AJUSTE) {
            return;
        }
        String motivo = event.getMotivo() == null || event.getMotivo().isBlank()
                ? "Sin motivo registrado"
                : event.getMotivo();

        for (UserModel admin : userRepository.findByEmpresaIdAndRole(event.getEmpresaId(), Role.ADMIN)) {
            emailService.send(
                    admin.getEmail(),
                    "Ajuste de inventario: " + event.getVarianteDescripcion(),
                    "email/ajuste-stock",
                    Map.of(
                            "nombre", admin.getNombre(),
                            "variante", event.getVarianteDescripcion(),
                            "stockAnterior", event.getStockAnterior(),
                            "stockResultante", event.getStockResultante(),
                            "diferencia", event.getDiferencia(),
                            "motivo", motivo,
                            "registradoPor", event.getRegistradoPor()
                    )
            );
        }
    }
}
