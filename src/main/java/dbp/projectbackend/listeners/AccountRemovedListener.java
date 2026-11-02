package dbp.projectbackend.listeners;

import dbp.projectbackend.config.AsyncConfig;
import dbp.projectbackend.events.UserDeletedEvent;
import dbp.projectbackend.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

/**
 * Avisa al empleado que su acceso fue retirado, para que no piense que es un error del sistema.
 */
@Component
@RequiredArgsConstructor
public class AccountRemovedListener {

    private final EmailService emailService;

    @Async(AsyncConfig.NOTIFICATIONS_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserDeleted(UserDeletedEvent event) {
        emailService.send(
                event.getEmail(),
                "Tu acceso a Tienda Clara fue retirado",
                "email/cuenta-eliminada",
                Map.of(
                        "nombre", event.getNombre(),
                        "empresa", event.getEmpresa(),
                        "eliminadoPor", event.getEliminadoPor()
                )
        );
    }
}
