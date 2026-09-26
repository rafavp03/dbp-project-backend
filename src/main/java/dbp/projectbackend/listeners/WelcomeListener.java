package dbp.projectbackend.listeners;

import dbp.projectbackend.config.AsyncConfig;
import dbp.projectbackend.events.UserRegisteredEvent;
import dbp.projectbackend.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WelcomeListener {

    private final EmailService emailService;

    @Async(AsyncConfig.NOTIFICATIONS_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        emailService.send(
                event.getEmail(),
                "Bienvenido a Tienda Clara",
                "email/bienvenida",
                Map.of(
                        "nombre", event.getNombre(),
                        "empresa", event.getEmpresa(),
                        "rol", event.getRol().name()
                )
        );
    }
}
